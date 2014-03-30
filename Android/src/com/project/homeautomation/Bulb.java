package com.project.homeautomation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import com.project.homeautomation.BackgroundService.MyLocalBinder;

public class Bulb extends Activity {
	TextView bulb_kwh, bulb_w,bulb_v,bulb_a;
	private static final String ACTION_STRING_BULB = "ToBulbActivity";
	BackgroundService myService;
    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
    	
        @Override
        public void onReceive(Context context, Intent intent) {
        	String msg =intent.getStringExtra("data");
        	CommandFromServer(msg);
        	
        	
            
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bulb);
bulb_kwh =(TextView)findViewById(R.id.bulb_kwh);
bulb_w =(TextView)findViewById(R.id.bulb_w);
bulb_v =(TextView)findViewById(R.id.bulb_v);
bulb_a =(TextView)findViewById(R.id.bulb_a);



}

	

@Override
public void onResume() {
    super.onResume(); 
    
   
}
@Override
protected void onStart() {
    super.onStart();
    
    if (activityReceiver != null) {
    	//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_ACTIVITY"
    	            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_BULB);
    	//Map the intent filter to the receiver
    	            registerReceiver(activityReceiver, intentFilter);
    	        }
    Intent intent = new Intent(this, BackgroundService.class);
    bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
}
private ServiceConnection myConnection = new ServiceConnection() {

    public void onServiceConnected(ComponentName className,
            IBinder service) {
        MyLocalBinder binder = (MyLocalBinder) service;
        myService = binder.getService();
        
    }
    
    public void onServiceDisconnected(ComponentName arg0) {
        
    }
    
   };
@Override
protected void onStop() {
    super.onStop();
  
    unregisterReceiver(activityReceiver);
    unbindService(myConnection); 
    
}
public void CommandFromServer(String command ){
	if(command.indexOf("BulbEnergy:")==0){//if the string starts with "BubPower:"
        command=command.replace("BulbEnergy:", "");//remove the command
        bulb_kwh.setText(command);
	}
	else if(command.indexOf("BulbPower:")==0){//if the string starts with "BubPower:"
        command=command.replace("BulbPower:", "");//remove the command
        bulb_w.setText(command);
	}
	else if(command.indexOf("BulbVolt:")==0){//if the string starts with "BubPower:"
        command=command.replace("BulbVolt:", "");//remove the command
        bulb_v.setText(command);
	}
	else if(command.indexOf("BulbAmp:")==0){//if the string starts with "BubPower:"
        command=command.replace("BulbAmp:", "");//remove the command
        bulb_a.setText(command);
	}
	
	
}
}