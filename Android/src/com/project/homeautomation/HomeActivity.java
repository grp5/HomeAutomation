package com.project.homeautomation;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class HomeActivity extends Activity implements OnCheckedChangeListener ,OnSeekBarChangeListener, OnClickListener{
	
	Switch fanbutton ,bulbbutton;
	
	String ipaddress, BulbPower;
	TextView fanseekText, bulbseekText,textlog;
	SeekBar fanseekBar, bulbseekBar;
	Button bulb_power,fan_power, connect, clear;
	Boolean connected=false;//stores the connectionstatus
	 EditText address;
    NetworkTask networktask;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		bulbbutton = (Switch) findViewById(R.id.bulbswitch);
		 fanbutton = (Switch)findViewById(R.id.fanswitch);
		 clear = (Button)findViewById(R.id.clear);
		fanseekText=(TextView)findViewById(R.id.fanseektext);
		bulbseekText=(TextView)findViewById(R.id.bulbseektext);
		bulbseekBar= (SeekBar)findViewById (R.id.bulbseekBar);
		fanseekBar= (SeekBar)findViewById (R.id.fanseekBar);
		bulb_power = (Button) findViewById(R.id.bulb_power);
		connect= (Button) findViewById(R.id.connect);
		textlog=(TextView)findViewById(R.id.textlog);
		address=(EditText)findViewById(R.id.ipaddress);
		fan_power = (Button) findViewById(R.id.fan_power);
		bulbseekBar.setOnSeekBarChangeListener(this);
		fanseekBar.setOnSeekBarChangeListener(this);
		bulb_power.setOnClickListener(this);
		fan_power.setOnClickListener(this);
		connect.setOnClickListener(this);
		clear.setOnClickListener(this);
	     bulbbutton.setOnCheckedChangeListener(this);
		fanbutton.setOnCheckedChangeListener(this);
		
		 networktask = new NetworkTask();
	}
	

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()){
		case R.id.bulbswitch:
		{ if(isChecked){ 
		
		networktask.SendDataToNetwork("bulbHIGH"+"\n");}
		else
			networktask.SendDataToNetwork("bulbLOW"+"\n");
		} break;
		case R.id.fanswitch:
		{ if(isChecked)
		networktask.SendDataToNetwork("fanHIGH"+"\n");
		else
			networktask.SendDataToNetwork("fanLOW"+"\n");
		} break;
		
			
		}
		}
		// TODO Auto-generated method stub
	@Override
	public void onResume() {
	    super.onResume(); 
	    
	   
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
	/*switch(requestCode)
	{
	case settings_code:

		if (resultCode == RESULT_OK){
			 ipaddress = data.getExtras()
		            .getString("ip");
			 networktask = new NetworkTask(); //New instance of NetworkTask
             networktask.execute();
			;}
		break;
		
	
	
	}*/
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		switch (seekBar.getId())
		{
		case R.id.bulbseekBar:
			bulbseekText.setText(Integer.toString(progress));
			networktask.SendDataToNetwork("Bulb:"+Integer.toString(progress)+"\n");
			
			  
              
			break;
		case R.id.fanseekBar:
			fanseekText.setText(Integer.toString(progress));
			networktask.SendDataToNetwork("Fan:"+Integer.toString(progress)+"\n");
			
			break;
			
			
		
		}
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	 @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        if(networktask!=null){//In case the task is currently running
	            networktask.cancel(true);//cancel the task
	        }
	    }

	@Override
	public void onClick(View button) {
		// TODO Auto-generated method stub
		switch(button.getId())
		{
		case R.id.bulb_power:
			bulb();
			break;
		case R.id.fan_power:
			fan();
			break;
		case R.id.connect:
			connect();
			break;
		case R.id.clear:
			textlog.setText("");
			break;
		}
		}
	
private void bulb(){
	Intent bulbIntent = new Intent(this, Bulb.class);
	bulbIntent.putExtra("power",BulbPower);
	startActivity(bulbIntent);
}
private void fan(){
	Intent fanIntent = new Intent(this, fan.class);
	startActivity(fanIntent);
}
private void connect(){
	 outputText("connecting to Server");
	ipaddress=address.getText().toString();
	networktask = new NetworkTask(); //New instance of NetworkTask
    networktask.execute();
}
public class NetworkTask extends AsyncTask<Void, byte[], Boolean> {
	 
    Socket nsocket; //Network Socket
    InputStream nis; //Network Input Stream
    OutputStream nos; //Network Output Stream
    BufferedReader inFromServer;//Buffered reader to store the incoming bytes
    
    @Override
    protected void onPreExecute() {
        //change the connection status to "connected" when the task is started
        changeConnectionStatus(true);
    }

    @Override
    protected Boolean doInBackground(Void... params) { //This runs on a different thread
        boolean result = false;
        try {
        	outputText("Connecting To Yun");
            //create a new socket instance
            SocketAddress sockaddr = new InetSocketAddress("192.168.240.1", 8888);
            nsocket = new Socket();
            nsocket.connect(sockaddr, 5000);//connect and set a 10 second connection timeout
            if (nsocket.isConnected()) {//when connected
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
            	outputText("Sending Data");
                nos.write(cmd.getBytes());
            } else {
            	outputText("SendDataToNetwork: Cannot send message. Socket is closed");
            }
        } catch (Exception e) {
        	 outputText("SendDataToNetwork: Message send failed. Caught an exception");
        }
    }

    //Methods is called everytime a new String is recieved from the socket connection
    @Override
    protected void onProgressUpdate(byte[]... values) {
        if (values.length > 0) {//if the recieved data is at least one byte
            String command=new String(values[0]);//get the String from the recieved bytes
            outputText(command);
            CommandFromServer(command);
            
            
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
        	 outputText("onPostExecute: Completed with an Error.");

        } else {
        	outputText("onPostExecute: Completed.");
        }
        changeConnectionStatus(false);//change connection status to disconnected
    }
}
 
public void CommandFromServer(String command ){
	if(command.indexOf("BulbPower:")==0){//if the string starts with "setPoti"
        command=command.replace("BulbPower:", "");//remove the command
        BulbPower=command;
	}
	
}

public void outputText(String msg) {
    textlog.append(msg+"\n");
}
	
public void changeConnectionStatus(Boolean isConnected) {
    connected=isConnected;//change variable
    
    if(isConnected){//if connection established
   	 outputText("successfully connected to server");
    }else{
   	 outputText("disconnected from Server!");
    }
}
}

