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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.project.homeautomation.BackgroundService.MyLocalBinder;

public class HomeActivity extends Activity implements OnCheckedChangeListener ,OnSeekBarChangeListener, OnClickListener{
	
	ToggleButton FanToggleButton ,BulbToggleButton;
	
	String ipaddress, BulbPower="100";
	TextView fanseekText, bulbseekText,textlog;
	SeekBar fanseekBar, bulbseekBar;
	Button bulb_power,fan_power, connect, clear;
	Boolean connected=false;//stores the connectionstatus
	 EditText address;

    BackgroundService myService;
    boolean isBound = false;
    private static final String ACTION_STRING_SERVICE = "ToService";
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";
    
    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        	String msg =intent.getStringExtra("data");
        	if(msg.indexOf("log:")==0){
        		outputText(msg);
        		
        	}
        	
        	
            
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		BulbToggleButton = (ToggleButton) findViewById(R.id.BulbToggleButton);
		FanToggleButton = (ToggleButton)findViewById(R.id.FanToggleButton);
		 clear = (Button)findViewById(R.id.clear);
		fanseekText=(TextView)findViewById(R.id.fanseektext);
		bulbseekText=(TextView)findViewById(R.id.bulbseektext);
		bulbseekBar= (SeekBar)findViewById (R.id.bulbseekBar);
		fanseekBar= (SeekBar)findViewById (R.id.fanseekBar);
		bulb_power = (Button) findViewById(R.id.bulb_power);
		
		textlog=(TextView)findViewById(R.id.textlog);
		
		fan_power = (Button) findViewById(R.id.fan_power);
		bulbseekBar.setOnSeekBarChangeListener(this);
		fanseekBar.setOnSeekBarChangeListener(this);
		bulb_power.setOnClickListener(this);
		fan_power.setOnClickListener(this);
		
		clear.setOnClickListener(this);
		BulbToggleButton.setOnCheckedChangeListener(this);
		FanToggleButton.setOnCheckedChangeListener(this);
		
		
        
	}
	
	private ServiceConnection myConnection = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        MyLocalBinder binder = (MyLocalBinder) service;
	        myService = binder.getService();
	        isBound = true;
	        outputText("bound");
	    }
	    
	    public void onServiceDisconnected(ComponentName arg0) {
	        isBound = false;
	        outputText("unbound");
	    }
	    
	   };
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	switch(buttonView.getId()){
	case R.id.BulbToggleButton:
		{ if(isChecked){ 
			sendBroadcast("bulbHIGH"+"\n");
		
			}
	else
		sendBroadcast("bulbLOW"+"\n");
	
		} break;
		case R.id.FanToggleButton:
	{ if(isChecked)
		sendBroadcast("fanHIGH"+"\n");
		
		else
			sendBroadcast("fanLOW"+"\n");
			
		} break;
		
			
		}
		}
		// TODO Auto-generated method stub
	@Override
	public void onResume() {
	    super.onResume(); 
	   
	   
	}
	
	
	
	
	

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		switch (seekBar.getId())
		{
		case R.id.bulbseekBar:
			bulbseekText.setText(Integer.toString(progress));
			//sendBroadcast("Bulb:"+Integer.toString(progress)+"\n");
			myService.networktask.SendDataToNetwork("Bulb:"+Integer.toString(progress)+"\n");
			
			
			  
              
			break;
		case R.id.fanseekBar:
			fanseekText.setText(Integer.toString(progress));
			sendBroadcast("Fan:"+Integer.toString(progress)+"\n");
			
			
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
    protected void onStart() {
        super.onStart();
        
       
        if (activityReceiver != null) {
        	//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_ACTIVITY"
        	            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_ACTIVITY);
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
	
	 @Override
	    protected void onDestroy() {
	        super.onDestroy();
	       
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
		
		case R.id.clear:
			textlog.setText("");
			break;
		}
		}
	
private void bulb(){
	Intent bulbIntent = new Intent(this, Bulb.class);
	
	startActivity(bulbIntent);
}
private void fan(){
	Intent fanIntent = new Intent(this, fan.class);
	startActivity(fanIntent);
}

private void sendBroadcast(String data) {
    Intent new_intent = new Intent();
    new_intent.setAction(ACTION_STRING_SERVICE);
    new_intent.putExtra("msg", data);
    sendBroadcast(new_intent);
}


public void outputText(String msg) {
    textlog.append(msg+"\n");
}
	

}

