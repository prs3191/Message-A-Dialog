package com.example.mad;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class about extends AppCompatActivity
{

	private ActionBar actionBar;
	public String LOG_TAG="About Activity";
	private Toolbar mtoolbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		Intent i =getIntent();
		setContentView(R.layout.about);
		mtoolbar=(Toolbar) findViewById(R.id.about_toolbar);
		Log.d(LOG_TAG,"mtoolbar:"+mtoolbar.toString());
		setSupportActionBar(mtoolbar);
		actionBar= getSupportActionBar();
		actionBar.setTitle("About");
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		Log.d(LOG_TAG,"onResume of About activity");
	}
}