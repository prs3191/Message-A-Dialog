package com.example.mad;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;
import com.facebook.share.widget.SendButton;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger.LogLevel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.Toast;

public class SearchResultsActivity extends AppCompatActivity implements MediaPlayerControl	 {

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

	private MediaController mMediaController;
	private MediaPlayer mMediaPlayer;

	private ProgressDialog progress;
	private String callingactivity;
	
	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		Log.d(LOG_TAG,"Inside oncreate of serachresults activity");

		progress = new ProgressDialog(this);
		progress.setCancelable(false);

		setContentView(R.layout.searchactivity_card_view);
		mMediaPlayer = new MediaPlayer();
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
				if(!mMediaPlayer.isPlaying())
				{	
					onBackPressed();
					
				}
				else
				{	
					mMediaController.hide();
					mMediaPlayer.stop();
				}
			}
		});


		mMediaController = new MediaController(SearchResultsActivity.this){
			@Override
			public void show(int timeout) {
				super.show(0);
			}
		};
		mMediaController.setMediaPlayer(SearchResultsActivity.this);
		mMediaController.setAnchorView(findViewById(R.id.search_relative_layout));

		mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.d(LOG_TAG,"Media PLayer onPrepared:");
				/*throws window leaked error*/
				/*mHandler.post(new Runnable() {
					public void run() {
						Log.d("m","runnable:");
						mMediaController.show();
						//	mMediaPlayer.start();
					}
				});*/
				mMediaPlayer.start();
				mMediaController.show();
			}
		});

		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				Log.d(LOG_TAG,"onCompletion and resetting media player");
				mMediaController.hide();
				mMediaPlayer.reset();
			}
		});
		mMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				// TODO Auto-generated method stub
				Log.d(LOG_TAG,"buffered percent:"+percent);
			}
		});

		mMediaPlayer.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				Log.d(LOG_TAG,"error code what:"+what+"error code extra:"+extra);
				//mp.pause();
				return false;
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
			//query = intent.getStringExtra(SearchManager.QUERY);
			query=intent.getStringExtra("query");
			mPicking=intent.getBooleanExtra("mPicking",false);
			Log.d(LOG_TAG,"queried string:"+query);
			Log.d(LOG_TAG,"reply flow?:"+mPicking);
			//Log.d(LOG_TAG,"category size:"+intent.getCategories().size());
			
			callingactivity=intent.getStringExtra("callingact");
			Log.d(LOG_TAG,"callingActivity: "+callingactivity);
			
			mtoolbar=(Toolbar) findViewById(R.id.toolbar);
			Log.d(LOG_TAG,"mtoolbar:"+mtoolbar.toString());
			setSupportActionBar(mtoolbar);
			actionBar= getSupportActionBar();
			
			progress.setTitle("Searching");
			//progress.setMessage("...");
			progress.show();
			//Log.d(LOG_TAG,"actionbar:"+actionBar.toString());

		}
	}

	  @Override
	    public void onBackPressed() {
	            super.onBackPressed();
	            this.finish();
	    }

	private ArrayList<DataObject> getDataSet() {
		if(callingactivity!=null && callingactivity.equals("Replyflowmain"))
			mresults=MainActivity.results;
		else
			mresults=MainActivity2.results;
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
		progress.dismiss();
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


				String music_file_key=((ArrayList<DataObject>)search_results).get(position).getmText1();
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

					TransferObserver observer=MainActivity.transferUtility.download(MainActivity.mBucket, music_file_key, local_stored_file);
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

			@Override
			public void onCardClick(int position,View v) {
				// TODO Auto-generated method stub
				Log.d(LOG_TAG,"onCardClick");
				//String audioFile = "/storage/emulated/0/mad/Aiio_Raaama.mp3" ; 
				//String audioFile ="http://www.stephaniequinn.com/Music/The%20Irish%20Washerwoman.mp3";
				String audioFilename=((ArrayList<DataObject>)search_results).get(position).getmText1();
				String audioFile =MainActivity.mlink+audioFilename;

				mMediaPlayer.reset();
				try 
				{	

					mMediaPlayer.setDataSource(SearchResultsActivity.this,Uri.parse(audioFile));
					//mMediaPlayer.setDataSource(audioFile);
					mMediaPlayer.prepareAsync();
					mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					
					AppEventsLogger logger = AppEventsLogger.newLogger(v.getContext());
					Bundle parameters = new Bundle();
					parameters.putString("fields", " ");
					parameters.putString(AppEventsConstants.EVENT_PARAM_MAX_RATING_VALUE, "1");
					parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "mp3");
					parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, MainActivity.user_id);
					parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, audioFilename);
					//parameters.putString("app_event_parameter1", music_file_key);
					//logger.logEvent("Custom_Rating2", 1,parameters);
					logger.logEvent("fb_mobile_music_played", 1,parameters);

				} catch (IOException e) {
					Log.e("PlayAudioDemo", "Could not open file " + audioFile + " for playback.", e);
				}


			}

		}
		,getApplicationContext());

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

		String music_file_key=((ArrayList<DataObject>)search_results).get(position).getmText1();
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
			parameters.putString("fields", " ");
			parameters.putString(AppEventsConstants.EVENT_PARAM_MAX_RATING_VALUE, "1");
			parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "mp3");
			parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, MainActivity.user_id);
			parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, music_file_key);
			//parameters.putString("app_event_parameter1", music_file_key);
			//logger.logEvent("Custom_Rating2", 1,parameters);
			logger.logEvent(AppEventsConstants.EVENT_NAME_RATED, 1,parameters);


			MyApp.tracker().send(new HitBuilders.EventBuilder(actionBar.getTitle()+"", "send")
			.setLabel(music_file_key)
			//.setValue(1)
			.build()
					);
			GoogleAnalytics.getInstance(this).getLogger()
			.setLogLevel(LogLevel.VERBOSE);

			if (mPicking) {
				// If we were launched from Messenger, we call MessengerUtils.finishShareToMessenger to return
				// the content to Messenger.
				Log.d(LOG_TAG,"parentActivity:"+MainActivity.act);
				MessengerUtils.finishShareToMessenger(MainActivity.act, shareToMessengerParams);
				Log.d(LOG_TAG,"After send button clicked reply flow");
				finish();

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

	@Override
	public void start() {
		// TODO Auto-generated method stub
		mMediaPlayer.start();
	}
	@Override
	public void pause() {
		// TODO Auto-generated method stub
		if(mMediaPlayer.isPlaying())
			mMediaPlayer.pause();

	}
	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		//return 0;
		if(mMediaPlayer!=null)
			return mMediaPlayer.getDuration();
		return -1;

	}
	@Override
	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		//return 0;
		return mMediaPlayer.getCurrentPosition();
	}
	@Override
	public void seekTo(int pos) {
		// TODO Auto-generated method stub
		mMediaPlayer.seekTo(pos);

	}
	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		//return false;
		return mMediaPlayer.isPlaying();
	}
	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		int percentage = (mMediaPlayer.getCurrentPosition() * 100) / mMediaPlayer.getDuration();
		Log.d(LOG_TAG,"getBufferPercentage():"+percentage);
		return percentage;
		//return 0;
	}
	@Override
	public boolean canPause() {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public boolean canSeekBackward() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean canSeekForward() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mMediaPlayer.isPlaying())
			mMediaPlayer.stop();
		mMediaPlayer.release();
		//LoginManager.getInstance().logOut();
		//Log.d(LOG_TAG,"User logged out");

	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		Log.d(LOG_TAG,"onActivityResult");
	    if (resultCode == RESULT_OK) {
	    	Log.d(LOG_TAG,"onActivityResult..Result_OK");
	    	this.setResult(Activity.RESULT_OK, data);
	    }
		if (resultCode == RESULT_CANCELED){
			Log.d(LOG_TAG,"onActivityResult..RESULT_CANCELED");
	    	this.setResult(Activity.RESULT_CANCELED, data);
		}
		
    }
	

}
