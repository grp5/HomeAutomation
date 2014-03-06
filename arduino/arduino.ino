#include <Bridge.h>
#include <YunServer.h>
#include <YunClient.h>
#include <Process.h>
#define PORT 8888
YunServer server(PORT); 
 
void setup() {
  Serial.begin(115200);
  while(!Serial);
  Bridge.begin();
  server.noListenOnLocalhost();
  server.begin();
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
        server.println(command);
    command="";  
    }
        else command += String(text);
        
      }
     if(client.available())
     {
        char cmd = client.read();
        if(cmd == '\n')
        {Serial.println(response);
          
          response="";
        } else {
          response += String(cmd);
         }
         
      }
    }
    
     
    
  }
 Serial.println("waiting for client");
  delay(1000);
}
