package com.project.homeautomation;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class fan extends Activity {
	TextView fan_kwh,fan_w,fan_v,fan_a;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fan);
		fan_kwh =(TextView)findViewById(R.id.fan_kwh);
		fan_w =(TextView)findViewById(R.id.fan_w);
		fan_v =(TextView)findViewById(R.id.fan_v);
		fan_a =(TextView)findViewById(R.id.fan_a);
	}
}