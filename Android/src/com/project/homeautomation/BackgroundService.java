package com.project.homeautomation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

public class BackgroundService extends Service {
	private static final String ACTION_STRING_SERVICE = "ToService";
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";
    private static final String ACTION_STRING_BULB = "ToBulbActivity";
    		private static final String ACTION_STRING_FAN = "ToFanActivity";
	private final IBinder myBinder = new MyLocalBinder();
	NetworkTask networktask;
	Boolean connected=false;//stores the connectionstatus
	private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        	String msg= intent.getStringExtra("msg");
        	networktask.SendDataToNetwork(msg);
           
           
            
        }
    };
    
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return myBinder;
	}
	public class MyLocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }
	 @Override
	    public void onCreate() {
	        super.onCreate();
	        
	//STEP2: register the receiver
	        if (serviceReceiver != null) {
	//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_SERVICE"
	            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_SERVICE);
	//Map the intent filter to the receiver
	            registerReceiver(serviceReceiver, intentFilter);
	            networktask = new NetworkTask(); //New instance of NetworkTask
	              networktask.execute();//Start the task
	              
	        }
	    }
	 
	 @Override
	    public void onDestroy() {
		 networktask.closeSocket();
		 networktask.cancel(true);
		 sendBroadcast("log:stopping service");
	        super.onDestroy();
	        
	//STEP3: Unregister the receiver
	        unregisterReceiver(serviceReceiver);
	    }
	 private void sendBroadcast(String data) {
		 if(data.indexOf("Bulb")==0){
     		
	        Intent new_intent = new Intent();
	        new_intent.setAction(ACTION_STRING_BULB);
	        new_intent.putExtra("data", data);
	        sendBroadcast(new_intent);}
		 else if(data.indexOf("Fan")==0){
			 Intent new_intent = new Intent();
		        new_intent.setAction(ACTION_STRING_FAN);
		        new_intent.putExtra("data", data);
		        sendBroadcast(new_intent);
		 }
		 else if(data.indexOf("log:")==0){
			 Intent new_intent = new Intent();
		        new_intent.setAction(ACTION_STRING_ACTIVITY);
		        new_intent.putExtra("data", data);
		        sendBroadcast(new_intent);
		 }
	    }
	public class NetworkTask extends AsyncTask<Void, byte[], Boolean> {
		 
	    
		Socket nsocket; //Network Socket
	    InputStream nis; //Network Input Stream
	    OutputStream nos; //Network Output Stream
	    BufferedReader inFromServer;//Buffered reader to store the incoming bytes
	    
	    @Override
	    protected void onPreExecute() {
	        
	    	//outputText("Starting Network task");
	    	sendBroadcast("log:Starting Network task");
	        
	        
	    }

	    @Override
	    protected Boolean doInBackground(Void... params) { //This runs on a different thread
	        boolean result = false;
	        
	        try {
	        	sendBroadcast("log:In doInBackground");
	        	
	            //create a new socket instance
	            SocketAddress sockaddr = new InetSocketAddress("192.168.240.1", 8888);
	            nsocket = new Socket();
	            nsocket.connect(sockaddr, 5000);//connect and set a 10 second connection timeout
	            if (nsocket.isConnected()) {//when connected
	            	changeConnectionStatus(true);
	                nis = nsocket.getInputStream();//get input
	                nos = nsocket.getOutputStream();//and output stream from the socket
	               
	                inFromServer = new BufferedReader(new InputStreamReader(nis));//"attach the inpustreamreader"
	                while(true){//while connected
	                	
	                	
	                	
	                    String msgFromServer = inFromServer.readLine();
	                    //read the lines coming from the socket
	                  
	                    byte[] theByteArray = msgFromServer.getBytes();//store the bytes in an array
	                   publishProgress(theByteArray);//update the publishProgress
	                   
	                }
	            }
	        //catch exceptions
	        } catch (IOException e) {
	            e.printStackTrace();
	            result = true;
	        } catch (Exception e) {
	            e.printStackTrace();
	            result = true;
	        } finally {
	            closeSocket();
	           
	        }
	        
	       
	        return result;
	    }

	    //Method closes the socket
	    public void closeSocket(){
	        try {
	            nis.close();
	            nos.close();
	            nsocket.close();
	            sendBroadcast("log:Socket Closed");
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    //Method tries to send Strings over the socket connection
	    public void SendDataToNetwork(String cmd) { //You run this from the main thread.
	        try {
	            if (nsocket.isConnected()) {
	            	
	            	sendBroadcast("log:Sending Data");
	                nos.write(cmd.getBytes());
	            } else {
	            	
	            	sendBroadcast("log:SendDataToNetwork: Cannot send message. Socket is closed");
	            }
	        } catch (Exception e) {
	        	sendBroadcast("log:SendDataToNetwork: Message send failed. Caught an exception");
	        	
	        }
	    }

	    //Methods is called everytime a new String is recieved from the socket connection
	    @Override
	    protected void onProgressUpdate(byte[]... values) {
	        if (values.length > 0) {//if the recieved data is at least one byte
	            String command=new String(values[0]);//get the String from the recieved bytes
	           
	            sendBroadcast("log:"+command);
	            sendBroadcast(command);
	           
	            
	            
	        }
	    }

	    //Method is called when task is cancelled
	    @Override
	    protected void onCancelled() {
	    	
	    	
	        changeConnectionStatus(false);//change the connection to "disconnected"
	    }

	    //Method is called after task execution
	    @Override
	    protected void onPostExecute(Boolean result) {
	        if (result) {
	        	
	        	sendBroadcast("log:onPostExecute: Completed with an Error.");
	        } else {
	        	
	        	sendBroadcast("log:onPostExecute: Completed.");
	        }
	        changeConnectionStatus(false);//change connection status to disconnected
	    }
	}
	public void changeConnectionStatus(Boolean isConnected) {
	    connected=isConnected;//change variable
	    
	    if(isConnected){//if connection established
	   	
	    	sendBroadcast("log:successfully connected to server");
	    }else{
	   	
	    	sendBroadcast("log:disconnected from Server!");
	    }
	}
	 
}
