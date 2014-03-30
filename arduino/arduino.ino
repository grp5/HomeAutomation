#include <TimerOne.h>          // Avaiable from http://www.arduino.cc/playground/Code/Timer1
#include <Bridge.h>
#include <YunServer.h>
#include <YunClient.h>
#include <Process.h>
#define PORT 8888
YunServer server(PORT); 
volatile int i=0;               // Variable to use as a counter
volatile boolean zero_cross=0;  // Boolean to store a "switch" to tell us if we have crossed zero
int AC_pin = 11; // Output to Opto Triac
int bulb_pin=10;
int fan_pin =9
int dim = 0;                    // Dimming level (0-128)  0 = on, 128 = 0ff
int inc=1;                      // counting up or down, 1=up, -1=down

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
void CommandFromClient(String command);
void bulb(String val);
void setup() {                                      // Begin setup
  pinMode(AC_pin, OUTPUT);     
pinMode(Relay_pin, OUTPUT); 
// Set the Triac pin as output
  attachInterrupt(0, zero_cross_detect, RISING);   // Attach an Interupt to Pin 3 (interupt 0) for Zero Cross Detection
  Timer1.initialize(freqStep);                      // Initialize TimerOne library for the freq we need
  Timer1.attachInterrupt(dim_check, freqStep);
Serial.begin(9600);  
 while(!Serial);
  Bridge.begin();
  server.noListenOnLocalhost();
  server.begin();
  // Use the TimerOne Library to attach an interrupt
  // to the function we use to check to see if it is 
  // the right time to fire the triac.  This function 
  // will now run every freqStep in microseconds.                                            
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
 
  if(client.connected())
  {
    
     String response;
     String command;
    while(client.connected())
    
   {
    
      if(Serial.available()){
        char text =(char)Serial.read();
        if(text== '#')
        {
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
    }
    
     
    
  }
 Serial.println("waiting for client");
  delay(1000);

void CommandFromClient(command){
  if(command==bulbHIGH){
    
  digitalWrite(bulb_pin,HIGH);
}
  else if (command==bulbLOW){
    
  digitalWrite(bulb_pin,LOW);
}
 
  else if(command==fanHIGH){
    
  digitalWrite(fan_pin,HIGH);
}
  else if (command==fanLOW){
    
  digitalWrite(fan_pin,LOW);
}
else if(command.indexOf("Bulb:")==0){
                    command.replace("Bulb:", "");//remove the command
                    bulb(command.toInt());}
                    
        
             }
  

        
                   

      
       void bulb(int val1)
       {
        dim=val1;
        Serial.println(dim);
        }
        

