package com.example.mad;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SearchResultsActivity extends Activity {
	
	public String LOG_TAG="SearchResultsActivity";
	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Log.d(LOG_TAG,"Inside oncreate of serachresults activity");
		handleIntent(getIntent());
		
	}
	
	@Override
	protected void onNewIntent(Intent intent){
		handleIntent(intent);
		
	}
	
	private void handleIntent(Intent intent){
		
		if(Intent.ACTION_SEARCH.equals(intent.getAction())){
			String query = intent.getStringExtra(SearchManager.QUERY);
			Log.d(LOG_TAG,"queried string"+query);
			
		}
	}

}
