#include <TimerOne.h>
#include <Bridge.h>
#include <YunServer.h>
#include <YunClient.h>
#include <Process.h>
#include <FileIO.h>
//
String data;
Process p;
boolean checkdns=true;
boolean BP=false,FP=false;
//server
#define PORT 8888
YunServer server(PORT); 
//zcd
volatile int i=0,j=0;               // Variable to use as a counter
volatile boolean zero_cross=0;  // Boolean to store a "switch" to tell us if we have crossed zero
int AC_pin = 5; // Output to Opto Triac
int bulb_pin=4;
int fan_pin =9;


int dim = 0;                    // Dimming level (0-128)  0 = on, 128 = 0ff
int inc=1;                      // countingup or down, 1=up, -1=down

int freqStep = 75;    // This is the delay-per-brightness step in microseconds.
// It is calculated based on the frequency of your voltage supply (50Hz or 60Hz)
// and the number of brightness steps you want. 
// 
// The only tricky part is that the chopper circuit chops the AC wave twice per
// cycle, once on the positive half and once at the negative half. This meeans
// the chopping happens at 120Hz for a 60Hz supply or 100Hz for a 50Hz supply. 

// To calculate freqStep you divide the length of one full half-wave of the power
// cycle (in microseconds) by the number of brightness steps. 
//
// (1000000 uS / 120 Hz) / 128 brightness steps = 65 uS / brightness step
//
// 1000000 us / 120 Hz = 8333 uS, length of one half-wave.
int temp_pin=A0;
float temp;
unsigned long lastUpdate=0;
//Power monitoring
unsigned long int start;
double realPower,Prms,powerFactor,Vrms,Irms,Ptot;
int lastSampleV,sampleV;   //sample_ holds the raw analog read value, lastSample_ holds the last sample
int lastSampleI,sampleI;                      
double lastFilteredV,filteredV;                  //Filtered_ is the raw analog value minus the DC offset
double lastFilteredI, filteredI;                  
double phaseShiftedV;                             //Holds the calibrated phase shifted voltage.
double sqV,sumV,sqI,sumI,instP,sumP;     
double VCAL=270.19;
double ICAL=9;
double PHASECAL=1.05;
int inPinV=A5,inPinI=A4;
int SUPPLYVOLTAGE=5000;
int ADC_COUNTS=1024;
int numberOfSamples = 0;


void CommandFromClient(String command);
void bulb(String val);
void power();
void DisplayPower();
void ddns();

void setup() {                                      // Begin setup
pinMode(AC_pin, OUTPUT);     
pinMode(bulb_pin, OUTPUT); 
pinMode(fan_pin, OUTPUT);
pinMode(temp_pin,INPUT);
// Set the Triac pin as output
  attachInterrupt(0, zero_cross_detect, RISING);   // Attach an Interupt to Pin 3 (interupt 0) for Zero Cross Detection
  Timer1.initialize(freqStep);                      // Initialize TimerOne library for the freq we need
  Timer1.attachInterrupt(dim_check, freqStep);   // Use the TimerOne Library to attach an interrupt
  // to the function we use to check to see if it is 
  // the right time to fire the triac.  This function 
  // will now run every freqStep in microseconds. 
  
Serial.begin(9600);  
 
  Bridge.begin();
  server.noListenOnLocalhost();
  server.begin();
  FileSystem.begin();
  
                                            
}

void zero_cross_detect() {    
  zero_cross = true;               // set the boolean to true to tell our dimming function that a zero cross has occured
  i=0;
  digitalWrite(AC_pin, LOW);       // turn off TRIAC (and AC)
}                                 

// Turn on the TRIAC at the appropriate time
void dim_check() {                   
  if(zero_cross == true) {              
    if(i>=dim) {                     
      digitalWrite(AC_pin, HIGH); // turn on light       
      i=0;  // reset time step counter                         
      zero_cross = false; //reset zero cross detection
    } 
    else {
      i++; // increment time step counter                     
    }                                
  }                                  
}                                   

void loop() {      
 YunClient client = server.accept(); 
 
 
  //ddns();
 
 
 if(client.connected())
  {
    
     String response;
     String command;
     
   while(client.connected())
   {
     
 if(millis()-lastUpdate>5000){
 temp= (float)((5.0*analogRead(temp_pin)*100.0)/1024);
 server.println("t:"+(String)temp);
lastUpdate=millis();
 }
     numberOfSamples = 0;
  start=millis();
  
  if(BP){
      server.println("BP:"+(String)realPower);
 server.println("BV:"+(String)Vrms);
 server.println("BA:"+(String)Irms);
 }
 while(millis()-start<5000){
    
   
    
    
      
      if(Serial.available()){
        char text =(char)Serial.read();
        if(text== '#')
        {
          if(response=="close"){
          client.stop();}
          
        server.println(response);
    response="";  
    }
        else response += String(text);
        
      }
     if(client.available())
     {
        char cmd = client.read();
        if(cmd == '\n')
        {Serial.println(command);
          CommandFromClient(command);
          command="";
          
        } else {
          command += String(cmd);
         }
         
     }
      power();
      }
      DisplayPower();
      
      

       
    }
    
     
  }
 Serial.println("waiting for client");
  delay(5000);
  
}

void CommandFromClient(String command){
  if(command.indexOf("b")==0){
    command.replace("b", "");//remove the command
 digitalWrite(bulb_pin,command.toInt());
}
else if(command.indexOf("f")==0){
    command.replace("f", "");//remove the command
 digitalWrite(bulb_pin,command.toInt());
}
  
else if(command.indexOf("B:")==0){
                    command.replace("B:", "");//remove the command
                    bulb(command.toInt());}
                    else if(command.indexOf("F:")==0){
                    command.replace("F:", "");//remove the command
                    bulb(command.toInt());}
else if (command=="BP1"){
BP=true;}
else if (command=="BP0"){
  BP=false;}
  else if (command=="FP1"){
FP=true;}
else if (command=="FP0"){
  FP=false;}
        
             }
  

        
                   

      
 void bulb(int val1)
{
 dim=100-val1;
        }
void power ()
        {
 numberOfSamples++;                            //Count number of times looped.

    lastSampleV=sampleV;                          //Used for digital high pass filter
    lastSampleI=sampleI;                          //Used for digital high pass filter
    
    lastFilteredV = filteredV;                    //Used for offset removal
    lastFilteredI = filteredI;                    //Used for offset removal   
    
    //-----------------------------------------------------------------------------
    // A) Read in raw voltage and current samples
    //-----------------------------------------------------------------------------
   
    sampleV = analogRead(inPinV);                 //Read in raw voltage signal
    sampleI = analogRead(inPinI);                 //Read in raw current signal

    //-----------------------------------------------------------------------------
    // B) Apply digital high pass filters to remove 2.5V DC offset (centered on 0V).
    //-----------------------------------------------------------------------------
    filteredV = 0.996*(lastFilteredV+(sampleV-lastSampleV));
    filteredI = 0.996*(lastFilteredI+(sampleI-lastSampleI));
   
    //-----------------------------------------------------------------------------
    // C) Root-mean-square method voltage
    //-----------------------------------------------------------------------------  
    sqV= filteredV * filteredV;                 //1) square voltage values
    sumV += sqV;                                //2) sum
    
    //-----------------------------------------------------------------------------
    // D) Root-mean-square method current
    //-----------------------------------------------------------------------------   
    sqI = filteredI * filteredI;                //1) square current values
    sumI += sqI;                                //2) sum 
    
    //-----------------------------------------------------------------------------
    // E) Phase calibration
    //-----------------------------------------------------------------------------
    phaseShiftedV = lastFilteredV + PHASECAL * (filteredV - lastFilteredV); 
    
    //-----------------------------------------------------------------------------
    // F) Instantaneous power calc
    //-----------------------------------------------------------------------------   
    instP = phaseShiftedV * filteredI;          //Instantaneous Power
    sumP +=instP;       
}


 


 void DisplayPower(){
   double V_RATIO = VCAL *((SUPPLYVOLTAGE/1000.0) / (ADC_COUNTS));
  Vrms = V_RATIO * sqrt(sumV / numberOfSamples); 
  
  double I_RATIO = ICAL *((SUPPLYVOLTAGE/1000.0) / (ADC_COUNTS));
  Irms = I_RATIO * sqrt(sumI / numberOfSamples); 

  //Calculation power values
  realPower = V_RATIO * I_RATIO * sumP / numberOfSamples;
  Prms = Vrms * Irms;
 //powerFactor=realPower / apparentPower;
  

  //Reset accumulators
  sumV = 0;
  sumI = 0;
  sumP = 0;
  
 
 }
 void ddns(){
   
   p.begin("python");  // Linino Command
 
    p.addParameter("/mnt/sda1/noip.py"); // Parametri comando
 
    p.run();  
    
    File nome = FileSystem.open("/mnt/sda1/write.txt", FILE_READ);
        // Read a character from the file
 if(nome){
  while(nome.available()>0){
     
  char dat= (char)nome.read();
    data+=(String)dat;}
   nome.close();
   Serial.println(data);
    data="";
}
 }
        
        

