package com.project.homeautomation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Bulb extends Activity {
	TextView bulb_kwh, bulb_w,bulb_v,bulb_a;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bulb);
bulb_kwh =(TextView)findViewById(R.id.bulb_kwh);
bulb_w =(TextView)findViewById(R.id.bulb_w);
bulb_v =(TextView)findViewById(R.id.bulb_v);
bulb_a =(TextView)findViewById(R.id.bulb_a);

Intent bulbIntent = getIntent();
bulb_w.setText(bulbIntent.getStringExtra("power")+"   W");

	}

@Override
public void onResume() {
    super.onResume(); 
    
   
}
}