package com.example.mad;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;
import com.facebook.share.widget.SendButton;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger.LogLevel;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class SearchResultsActivity extends AppCompatActivity	 {

	ArrayList search_results = new ArrayList<DataObject>();
	ArrayList mresults = new ArrayList<DataObject>();
	int msize=0;
	String query;
	private static boolean  transfer_complete=false;
	private RecyclerView mRecyclerView;
	private RecyclerView.Adapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private String link="file:///storage/emulated/0/mad/";
	// This is the request code that the SDK uses for startActivityForResult. See the code below
	// that references it. Messenger currently doesn't return any data back to the calling
	// application.
	private static final int REQUEST_CODE_SHARE_TO_MESSENGER = 1;
	private MessengerThreadParams mThreadParams;
	private boolean mPicking;
	public String LOG_TAG="SearchResultsActivity";
	private Toolbar mtoolbar;
	static ActionBar actionBar;

	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		Log.d(LOG_TAG,"Inside oncreate of serachresults activity");
		setContentView(R.layout.searchactivity_card_view);
		handleIntent(getIntent());


		new File("/storage/emulated/0/"+"mad").mkdirs();
		

		mRecyclerView = (RecyclerView) findViewById(R.id.my_searchrecycler_view);
		mRecyclerView.setHasFixedSize(true);

		mLayoutManager = new LinearLayoutManager(SearchResultsActivity.this);
		mRecyclerView.setLayoutManager(mLayoutManager);

		mAdapter = new MyRecyclerViewAdapter(getDataSet());
		mRecyclerView.setAdapter(mAdapter);
		
		if(search_results.size()<=1)
			actionBar.setTitle(search_results.size()+" result for '"+query+"'");
		else
			actionBar.setTitle(search_results.size()+" results for '"+query+"'");	
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		/*mtoolbar.setNavigationIcon(R.drawable.ic_action_back_2);*/
		mtoolbar.setNavigationOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        onBackPressed();
		    }
		});
		
		/*actionBar.setHomeButtonEnabled(true);*/
//		gatrack();
//		getawsauth();

	}

	@Override
	protected void onNewIntent(Intent intent){
		handleIntent(intent);

	}

	private void handleIntent(Intent intent){

		if(Intent.ACTION_SEARCH.equals(intent.getAction())){
			query = intent.getStringExtra(SearchManager.QUERY);
			Log.d(LOG_TAG,"queried string:"+query);
			
			mtoolbar=(Toolbar) findViewById(R.id.toolbar);
			Log.d(LOG_TAG,"mtoolbar:"+mtoolbar.toString());
			setSupportActionBar(mtoolbar);
			actionBar= getSupportActionBar();
			
			//Log.d(LOG_TAG,"actionbar:"+actionBar.toString());

		}
	}



	private ArrayList<DataObject> getDataSet() {
		mresults=MainActivity.results;
		msize=mresults.size();
		
		int index1=0;int index2=0;
		Log.d(LOG_TAG,"inside getdataset\nfile_list_size:"+msize);
		while(index1 < msize){
			String music_file_key= ((ArrayList<DataObject>)mresults).get(index1).getmText1();
			String nots= ((ArrayList<DataObject>)mresults).get(index1).getmText2();
			Log.d(LOG_TAG,"searching:"+music_file_key);
			if(music_file_key.toLowerCase().contains(query.toLowerCase()))
			{
				DataObject obj = new DataObject(music_file_key,nots);
				search_results.add(index2, obj);
				index2++;
				Log.d(LOG_TAG,"matched string:\n"+music_file_key+" nots:"+nots);
			}
			index1++;
		}
		return search_results;
	}


	@Override
	protected void onResume()
	{
		super.onResume();
		AppEventsLogger.activateApp(this); 


		((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter.MyClickListener()
		{
			@Override
			public void onItemClick(int position, View v, SendButton sendbutton)
			{
				Log.i(LOG_TAG, " Clicked on Item: " + position);
				Log.i(LOG_TAG, " View ID: " + v.getId());
				//Log.i(LOG_TAG, " Sendbutton ID " + sendbutton.getId());


				String music_file_key=((ArrayList<DataObject>)mresults).get(position).getmText1();
				Log.i(LOG_TAG,"storage loc:\n"+Environment.getExternalStorageDirectory());

				link="file:///storage/emulated/0/mad/"+music_file_key;
				Log.i(LOG_TAG, "file path:\n" + link);

				File local_stored_file=new File(Environment.getExternalStorageDirectory()
						+File.separator
						+"mad" 
						+File.separator
						+music_file_key);
				Log.i(LOG_TAG,"music file key:\n"+music_file_key);
				// boolean  transfer_complete=false;

				if(!local_stored_file.exists())
				{

					TransferObserver observer=MainActivity.transferUtility.download(Utils.BUCKET, music_file_key, local_stored_file);
					observer.setTransferListener(new TransferListener() {


						@Override
						public void onError(int arg0, Exception arg1) {
							// TODO Auto-generated method stub
							Log.i(LOG_TAG,"transfer error:"+arg0+" arg1:"+arg1);
						}

						@Override
						public void onProgressChanged(int arg0, long arg1, long arg2) {
							// TODO Auto-generated method stub
							Log.i(LOG_TAG,"transfer progress:"+arg0+" arg1:"+arg1+" arg2:"+arg2);
						}

						@Override
						public void onStateChanged(int arg0, TransferState arg1) {
							// TODO Auto-generated method stub
							Log.i(LOG_TAG,"transfer state:"+arg0+" arg1:"+arg1);
							if(arg1.toString()=="COMPLETED"){
								transfer_complete=true;
								Log.i(LOG_TAG,"written to:"+link);
								Toast.makeText(getApplicationContext(), "Tap Again !", 
										Toast.LENGTH_SHORT).show();
							}

						}

					});
					if(transfer_complete){
						transfer_complete=false;
						Log.i(LOG_TAG, " File Already EXISTS ?" + "No and file written");
						onMessengerButtonClicked(position,v,sendbutton);
					}

				}
				else{

					Log.i(LOG_TAG, " File Already EXISTS ?" + "YES");
					onMessengerButtonClicked(position,v,sendbutton);

				}
			}

		}
				);

	}

	@Override
	protected void onPause() { 
		super.onPause(); 
		AppEventsLogger.deactivateApp(this);
	}
	private void onMessengerButtonClicked(int position,View v, SendButton sendbutton)
	{
		// The URI can reference a file://, content://, or android.resource. Here we use
		// android.resource for sample purposes.

		String music_file_key=((ArrayList<DataObject>)mresults).get(position).getmText1();
		File local_stored_file=new File(Environment.getExternalStorageDirectory()
				+File.separator
				+"mad" 
				+File.separator
				+music_file_key);
		Log.i(LOG_TAG,"music file key:"+music_file_key);


		if(local_stored_file.exists())
		{

			// Create the parameters for what we want to send to Messenger.
			Uri uri =Uri.parse(link);
			ShareToMessengerParams shareToMessengerParams =
					ShareToMessengerParams.newBuilder(uri, "audio/*")
					//.setMetaData("{ \"audio\" : \"tre\" }")
					//.setExternalUri(uri)
					.build();
			//logger.logEvent("button_count_"+music_file_key,1);

			//track events
			//working in all flows
			AppEventsLogger logger = AppEventsLogger.newLogger(v.getContext());
			Bundle parameters = new Bundle();
			parameters.putString(AppEventsConstants.EVENT_PARAM_MAX_RATING_VALUE, "1");
			parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "mp3");
			parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, MainActivity.user_id);
			parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, music_file_key);
			//parameters.putString("app_event_parameter1", music_file_key);
			//logger.logEvent("Custom_Rating2", 1,parameters);
			logger.logEvent(AppEventsConstants.EVENT_NAME_RATED, 1,parameters);


			MyApp.tracker().send(new HitBuilders.EventBuilder("tamil", "send")
			.setLabel(music_file_key)
			//.setValue(1)
			.build()
					);
			GoogleAnalytics.getInstance(this).getLogger()
			.setLogLevel(LogLevel.VERBOSE);

			if (mPicking) {
				// If we were launched from Messenger, we call MessengerUtils.finishShareToMessenger to return
				// the content to Messenger.
				MessengerUtils.finishShareToMessenger(this, shareToMessengerParams);
				Log.d(LOG_TAG,"After send button clicked reply flow");

			} else {
				// Otherwise, we were launched directly (for example, user clicked the launcher icon). We
				// initiate the broadcast flow in Messenger. If Messenger is not installed or Messenger needs
				// to be upgraded, this will direct the user to the play store.

				MessengerUtils.shareToMessenger(
						this,
						REQUEST_CODE_SHARE_TO_MESSENGER,
						shareToMessengerParams);
				Log.d(LOG_TAG,"After send button clicked normal flow");
			}
		}

	}


}
