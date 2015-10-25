package com.example.mad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class dummy extends Activity{
	
	private String LOG_TAG="dummy_Activity";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dummy);
		Intent intent=getIntent();
		Log.d(LOG_TAG,"intent: "+getIntent());
		Log.d(LOG_TAG,"intent received from "+intent.getStringExtra("hello"));
		TextView tv=(TextView) findViewById(R.id.textView1);
		tv.setText(intent.getStringExtra("hello")+" from intent: "+getIntent());
	}

}
