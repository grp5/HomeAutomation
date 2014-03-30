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

public class fan extends Activity {
	TextView fan_kwh,fan_w,fan_v,fan_a;
	BackgroundService myService;
	private static final String ACTION_STRING_FAN = "ToFanActivity";
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
		setContentView(R.layout.activity_fan);
		fan_kwh =(TextView)findViewById(R.id.fan_kwh);
		fan_w =(TextView)findViewById(R.id.fan_w);
		fan_v =(TextView)findViewById(R.id.fan_v);
		fan_a =(TextView)findViewById(R.id.fan_a);
		
        
	}
	@Override
	protected void onStart() {
	    super.onStart();
	    
	    if (activityReceiver != null) {
	    	//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_ACTIVITY"
	    	            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_FAN);
	    	//Map the intent filter to the receiver
	    	            registerReceiver(activityReceiver, intentFilter);
	    	        }
	    Intent intent = new Intent(this, BackgroundService.class);
	    bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
	}
	@Override
	protected void onStop() {
	    super.onStop();
	  
	    unregisterReceiver(activityReceiver);
	    unbindService(myConnection); 
	     
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
	public void CommandFromServer(String command ){
		if(command.indexOf("FanEnergy:")==0){//if the string starts with "BubPower:"
	        command=command.replace("FanEnergy:", "");//remove the command
	        fan_kwh.setText(command);
		}
		else if(command.indexOf("FanPower:")==0){//if the string starts with "BubPower:"
	        command=command.replace("FanPower:", "");//remove the command
	        fan_w.setText(command);
		}
		else if(command.indexOf("FanVolt:")==0){//if the string starts with "BubPower:"
	        command=command.replace("FanVolt:", "");//remove the command
	        fan_v.setText(command);
		}
		else if(command.indexOf("FanAmp:")==0){//if the string starts with "BubPower:"
	        command=command.replace("FanAmp:", "");//remove the command
	        fan_a.setText(command);
		}
		
		
	}
	}
