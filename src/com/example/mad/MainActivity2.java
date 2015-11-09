package com.example.mad;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.Dataset.SyncCallback;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.mad.MyApp;
import com.example.mad.DataObject;
import com.example.mad.MyRecyclerViewAdapter;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.ShareButton;

import com.facebook.share.widget.SendButton;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger.LogLevel;



import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity2 extends AppCompatActivity implements MediaPlayerControl {


	List<S3ObjectSummary> summaries;
	static ArrayList results = new ArrayList<DataObject>();

	private RecyclerView mRecyclerView;
	private RecyclerView.Adapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private static String LOG_TAG = "MainActivity2";
	//	private static boolean DOWNLOAD_CLICKED=false;
	//	private static int send_button_position=-1;
	private String link="file:///storage/emulated/0/mad/";

	// This is the request code that the SDK uses for startActivityForResult. See the code below
	// that references it. Messenger currently doesn't return any data back to the calling
	// application.
	private static final int REQUEST_CODE_SHARE_TO_MESSENGER = 1;

	private View mMessengerButton;
	static MessengerThreadParams mThreadParams;

	private boolean mPicking;
	private boolean isReply, isCompose;
	private String threadToken;

	private CallbackManager callbackManager;

	static TransferUtility transferUtility  ;
	private CognitoCredentialsProvider  credentialsProvider;
	public static String mBucket;
	public static String mlink;
	private static boolean  transfer_complete=false;
	private static String user_access_token;
	private Dataset dataset;
	private String user_id;
	private String user_name;

	private Map<String, String> times_sent = new HashMap<String, String>();
	static Map<String,String> all_files_wnots = new HashMap<String,String>();
	//	private  DefaultSyncCallback syncCallback;

	private DrawerLayout mDrawerLayout;
	//	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private static CharSequence mTitle;
	private String[] mdrawerItemTitles;

	private NavigationView mNavigationView; 
	private Toolbar mtoolbar;
	private MenuItem mMenuItem;
	static ActionBar actionBar;

	private ProgressDialog progress;

	private MediaController mMediaController;
	private MediaPlayer mMediaPlayer;

	static boolean gotaccesstoken=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*	try {
	        PackageInfo info = getPackageManager().getPackageInfo(
	                "com.example.mad", 
	                PackageManager.GET_SIGNATURES);
	        for (Signature signature : info.signatures) {
	            MessageDigest md = MessageDigest.getInstance("SHA");
	            md.update(signature.toByteArray());
	            Log.d("YourKeyHash :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
	            System.out.println("YourKeyHash: "+ Base64.encodeToString(md.digest(), Base64.DEFAULT));
	            }
	    } catch (NameNotFoundException e) {

	    } catch (NoSuchAlgorithmException e) {

	    }*/
		mMediaPlayer = new MediaPlayer();
		if(gotaccesstoken==true)
			buildui();
		else
			login();

	}

	public void login(){


		if( (!FacebookSdk.isInitialized()) || (AccessToken.getCurrentAccessToken()==null || gotaccesstoken==false) )
		{
			Log.d(LOG_TAG,"fbsdk not init: so inting fbsdk");
			/*FacebookSdk.sdkInitialize(getApplicationContext());

			//Intent i=new Intent(MainActivity.this,fb_loginActivity.class);
			//Intent i = getPackageManager().getLaunchIntentForPackage("com.example.mad");
			//startActivity(i);
			//finish();
			//fb_loginActivity.login();
			callbackManager = CallbackManager.Factory.create();
			LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
				@Override
				public void onSuccess(LoginResult result) {
					// TODO Auto-generated method stub
					Log.d(LOG_TAG,"login_activity:"+result);
					Log.d(LOG_TAG,"login_activity:"+
							"User ID: "
							+ result.getAccessToken().getUserId() 
							+ "\n" +
							"Auth Token: "
							+ result.getAccessToken().getToken()
							);

					user_access_token=result.getAccessToken().getToken();
					user_id=result.getAccessToken().getUserId();

					//graph api to get user name after getting Accesstoken
					GraphRequest request = GraphRequest.newMeRequest(
							result.getAccessToken(),
							new GraphRequest.GraphJSONObjectCallback() {

								@Override
								public void onCompleted(JSONObject jsonObject,
										GraphResponse response) {
									Log.d("after sdk init","onCompleted jsonObject: "+jsonObject);
									Log.d("after sdk init","onCompleted response: "+response);
									try {
										user_name=(String)jsonObject.getString("name");
										Log.d("MainActivity Graph api",""+user_name);

									} catch (JSONException e) {
										// TODO Auto-generated catch block
										Log.d("Graph api json error",""+e);
									}
									if(user_name!=null && gotaccesstoken==false){
										gotaccesstoken=true;
										buildui();
									}
								}

							});
					Bundle parameters = new Bundle();
					parameters.putString("fields", "name"/*,id,link,cover,email/);
					request.setParameters(parameters);
					request.executeAsync();


				}

				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					Log.d(LOG_TAG,"login_activity:Oncancel()");
					Toast.makeText(getApplicationContext(), "FB Login Cancelled. Try Again !", 
							Toast.LENGTH_SHORT).show();

				}

				@Override
				public void onError(FacebookException error) {
					// TODO Auto-generated method stub

					Log.d(LOG_TAG,"login_activity:"+error);
					Toast.makeText(getApplicationContext(), "FB Login Failed. Try Again !", 
							Toast.LENGTH_SHORT).show();
				}

			});
			LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("read_insights"));
			Log.d(LOG_TAG,"loginwithread permi called....");*/

		}
		else{
			gotaccesstoken=true;
			buildui();
		}
	}


	public void buildui()
	{

		if(gotaccesstoken)
		{




			progress = new ProgressDialog(this);
			progress.setCancelable(false);


			new File("/storage/emulated/0/"+"mad").mkdirs();
			setContentView(R.layout.activity_card_view2);
			mtoolbar=(Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(mtoolbar);
			actionBar= getSupportActionBar();

			mBucket=Utils.BUCKET;
			mlink=Utils.LINK+mBucket+"/";

			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


			// set a custom shadow that overlays the main content when the drawer opens
			mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);


			mNavigationView=(NavigationView) findViewById(R.id.nav_view);

			// set up the drawer's list view with items and click listener
			mNavigationView.setNavigationItemSelectedListener(new DrawerItemClickListener());

			mTitle = mDrawerTitle = mNavigationView.getMenu().getItem(0)+"";
			setTitle(mTitle);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);

			mNavigationView.setItemTextColor(new ColorStateList(
					new int [] [] {
							new int [] {android.R.attr.state_checked},
							new int [] {-android.R.attr.state_checked},
							new int [] {}
					},
					new int [] {
							ContextCompat.getColor(getApplicationContext(), R.color.selected_text),
							ContextCompat.getColor(getApplicationContext(), R.color.black),
							ContextCompat.getColor(getApplicationContext(), R.color.black)
					}
					));

			mNavigationView.setItemIconTintList(new ColorStateList(
					new int [] [] {
							new int [] {android.R.attr.state_checked},
							new int [] {-android.R.attr.state_checked},
							new int [] {}
					},
					new int [] {
							ContextCompat.getColor(getApplicationContext(), R.color.selected_text),
							ContextCompat.getColor(getApplicationContext(), R.color.black),
							ContextCompat.getColor(getApplicationContext(), R.color.black)
					}
					));

			// ActionBarDrawerToggle ties together the the proper interactions
			// between the sliding drawer and the action bar app icon
			mDrawerToggle = new ActionBarDrawerToggle(
					this,                  /* host Activity */
					mDrawerLayout,         /* DrawerLayout object */
					//R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
					mtoolbar,
					R.string.drawer_open,  /* "open drawer" description for accessibility */
					R.string.drawer_close  /* "close drawer" description for accessibility */
					) {
				public void onDrawerClosed(View view) {

					Log.d(LOG_TAG,"onDrawerClosed b4 setTitle:\nmTitle:"+mTitle+" mDrawerTitle"+mDrawerTitle);
					//actionBar.setTitle(mTitle);
					Log.d(LOG_TAG,"onDrawerClosed after setTitle:\nmTitle:"+mTitle+" mDrawerTitle"+mDrawerTitle);
					invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				}

				public void onDrawerOpened(View drawerView) {
					Log.d(LOG_TAG,"onDrawerOpened b4 setTitle:\nmTitle:"+mTitle+" mDrawerTitle"+mDrawerTitle);
					//actionBar.setTitle(mDrawerTitle);
					Log.d(LOG_TAG,"onDrawerOpened after setTitle:\nmTitle:"+mTitle+" mDrawerTitle"+mDrawerTitle);
					invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				}
			};

			mDrawerToggle.setDrawerIndicatorEnabled(true);
			mDrawerLayout.setDrawerListener(mDrawerToggle);


			mDrawerToggle.syncState();


			mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
			mRecyclerView.setHasFixedSize(true);


			mLayoutManager = new LinearLayoutManager(MainActivity2.this);
			mRecyclerView.setLayoutManager(mLayoutManager);


			results.clear();
			mAdapter = new MyRecyclerViewAdapter(results);
			mRecyclerView.setAdapter(mAdapter);


			mMediaController = new MediaController(MainActivity2.this){
				@Override
				public void show(int timeout) {
					super.show(0);
				}
			};
			mMediaController.setMediaPlayer(MainActivity2.this);
			mMediaController.setAnchorView(findViewById(R.id.drawer_layout));

			//final Handler mHandler = new Handler();


			mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					Log.d("m","Media PLayer onPrepared:");
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
					Log.d("m","onCompletion and resetting media player");
					mMediaController.hide();
					mMediaPlayer.reset();
					setTitle(mTitle);
				}
			});
			mMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

				@Override
				public void onBufferingUpdate(MediaPlayer mp, int percent) {
					// TODO Auto-generated method stub
					Log.d("m","buffered percent:"+percent);
				}
			});

			mMediaPlayer.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					// TODO Auto-generated method stub
					Log.d(LOG_TAG,"error code what:"+what+" error code extra:"+extra);
					//mp.pause();
					return false;
				}
			});
			
			

			Intent intent = getIntent();
			Log.d("MainActivity","What is intent action received:\n"+intent.getAction());
			if (Intent.ACTION_PICK.equals(intent.getAction())) {


				mThreadParams = MessengerUtils.getMessengerThreadParamsForIntent(intent);
				mPicking = true;

				user_access_token=AccessToken.getCurrentAccessToken().getToken();
				Log.d("MainActivity","access token after hit reply button:\n"+user_access_token);
				user_id=intent.getStringExtra("user_id");
				user_name=intent.getStringExtra("user_name");

				// Note, if mThreadParams is non-null, it means the activity was launched from Messenger.
				// It will contain the metadata associated with the original content, if there was content.
			}
			/*else if(Intent.ACTION_SEARCH.equals(intent.getAction())){
			handleIntent(getIntent());

		}*/
			else{
				user_access_token=intent.getStringExtra("user_access_token");
				user_id=intent.getStringExtra("user_id");
				user_name=intent.getStringExtra("user_name");
				MainActivity.user_id=this.user_id;
				MainActivity.user_name=this.user_name;

				// You only need to set User ID on a tracker once. By setting it on the tracker, the ID will be
				// sent with all subsequent hits.
				Log.d("MainActivity","access token after login button:\n"+user_access_token);
				Log.d("MainActivity","accessing google tracker:"+MyApp.tracker().getClass());
				MyApp.tracker().set("&uid", user_id);

				MyApp.tracker().send(new HitBuilders.EventBuilder()
				.setCategory("UX")
				.setAction("User Sign In").build());
				GoogleAnalytics.getInstance(this).getLogger()
				.setLogLevel(LogLevel.VERBOSE);

			}


			TextView usrname=(TextView)this.findViewById(R.id.header_layout_username);
			usrname.setText(user_name);
			progress.setTitle("Loading");
			progress.setMessage("Wait while loading...");
			progress.show();
			getbucketlist();


		}

	}

	private void getbucketlist(){

		try{
			new HttpTask().execute();
		}
		catch(Exception e){
			Log.d("MainActivity","Http_async_mainact:\n"+e);
		}

	}


	public final class HttpTask extends AsyncTask<URL , Boolean /* Progress */, String /* Result */>
	{
		@Override
		protected String doInBackground(URL... params)
		{

			results.clear();
			Map<String, String> logins = new HashMap<String, String>();


			logins.put("graph.facebook.com", user_access_token/*AccessToken.getCurrentAccessToken().getToken()*/);

			for (Map.Entry entry : logins.entrySet()) {
				Log.d("MainActivity","Access Token from fb to aws:\n"+entry.getKey() + ", " + entry.getValue());
			}

			// Initialize the Amazon Cognito credentials provider
			if (credentialsProvider==null){
				credentialsProvider = new CognitoCredentialsProvider(
						//getApplicationContext(),
						Utils.POOL_ID, // Identity Pool ID
						Regions.US_EAST_1 // Region
						);
				credentialsProvider.setLogins(logins);
				credentialsProvider.refresh();
				Log.d("MainActivity","cred provider check:\n"+credentialsProvider.getIdentityId());
			}
			//CognitoCachingCredentialsProvider not updating properly when new user logs in.
			//Including it because 3rd param of CognitoSyncManager requires it

			//			CognitoCachingCredentialsProvider credentialscachProvider = new CognitoCachingCredentialsProvider(
			//					getApplicationContext(),
			//					Utils.POOL_ID, // Identity Pool ID
			//					Regions.US_EAST_1 // Region
			//					);
			//			
			//			CognitoSyncManager client_cognitosync = new CognitoSyncManager(
			//				    getApplicationContext(),
			//				    Regions.US_EAST_1, 
			//				    credentialscachProvider);
			//			 dataset = client_cognitosync.openOrCreateDataset(
			//		                Utils.DATASET_NAME);
			//			 dataset.put(user_id,user_name);
			//			
			//			dataset.synchronize(syncCallback);

			AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

			//transferManager = new TransferManager(credentialsProvider);
			transferUtility= new TransferUtility(s3, getApplicationContext());
			try{
				//Log.d("s3 check","");
				ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(mBucket)
				//.withPrefix("gou/")
				.withDelimiter("/")
				;

				ObjectListing listing = s3.listObjects(listObjectsRequest);
				Log.d("MainActivity","after listobjects..Bucket accessed");

				summaries = listing.getObjectSummaries();

				while (listing.isTruncated()) {
					listing = s3.listNextBatchOfObjects (listing);
					summaries.addAll (listing.getObjectSummaries());
				}
				//list_files(summaries);
			}
			catch(AmazonServiceException  e){
				Log.d("MainActivity","s3_lisiting_se:\n"+e);
			}
			catch (AmazonClientException e) {
				Log.d("MainActivity","s3_lisiting_ce:\n"+e);
			}

			Bundle parameters = new Bundle();
			parameters.putString("fields", " ");
			parameters.putString("breakdowns[0]", "fb_description");
			parameters.putString("aggregateBy", "SUM");
			parameters.putString("event_name", "fb_mobile_rate");
			parameters.putString("period", "range");
			parameters.putString("since", "2015-01-01");


			Calendar c = Calendar.getInstance();
			System.out.println("Current time => " + c.getTime());

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String formattedDate = df.format(c.getTime());
			System.out.println("current date"+formattedDate);
			parameters.putString("until", formattedDate);
			/* make the API call */
			new GraphRequest(
					AccessToken.getCurrentAccessToken(),
					"/"+Utils.fb_app_id+"/app_insights/app_event",
					parameters,
					HttpMethod.GET,
					new GraphRequest.Callback() {
						public void onCompleted(GraphResponse response) {
							/* handle the result */

							Log.d("MainActivity","fb analytics response:\n"+response);
							try{
								JSONObject jsonobj = response.getJSONObject();
								JSONArray jarray = jsonobj.getJSONArray("data");
								for(int i = 0; i < jarray.length(); i++)
								{
									JSONObject getval = jarray.getJSONObject(i);
									//get your values
									String val=getval.getString("value"); // this will return you total sends.
									String music_key=getval.getJSONObject("breakdowns").getString("fb_description");
									times_sent.put(music_key,val);
									Log.d("MainActivity","music_key:\n"+music_key+"\nval:"+val);
								}
							}
							catch(Exception e){
								Log.d("MainActivity","err in gettin times send:\n"+e);
							}
						}
					}
					).executeAndWait();

			return null;	

		}

		@Override
		protected void onPostExecute(String result) {
			publishProgress(false);

			mAdapter = new MyRecyclerViewAdapter(getDataSet());
			mRecyclerView.setAdapter(mAdapter);

			Log.d(LOG_TAG,"mAdapter is set with data");
			if(mPicking==true){
				onResume();
			}

		}

	}



	private ArrayList<DataObject> getDataSet() {

		int index=0;
		String key;
		for(S3ObjectSummary summary : summaries)
		{
			Log.d("Mainactivity","times_sent from hashmap:"+summary.getKey().toString()+"="+times_sent.get(summary.getKey().toString()));
			key=summary.getKey().toString();
			Log.d(LOG_TAG,"keysplit:"+key.substring(key.lastIndexOf('/')+1));
			//if(key.substring(key.lastIndexOf('/')+1)!= ""){
			DataObject obj = new DataObject(key, times_sent.get(summary.getKey().toString()));
			results.add(index, obj);
			index++;
			//}
		}
		// To dismiss the dialog
		progress.dismiss();
		return results;
	}



	@Override
	protected void onResume()
	{
		super.onResume();
		AppEventsLogger.activateApp(this); 
		if(gotaccesstoken==false)
		{	Log.d(LOG_TAG,"gotacesstoken=false and inside onresume()");}//login();}
		//mDrawerToggle.syncState();
		else{
			Log.d(LOG_TAG,"gotacesstoken=true and inside onresume()");			
			adapterlistener();	
		}
	}	

	private void adapterlistener(){

		Log.d(LOG_TAG,"inside adapterlistener()");
		mDrawerToggle.syncState();
		((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter.MyClickListener()
		{
			@Override
			public void onItemClick(int position, View v, SendButton sendbutton)
			{
				Log.i(LOG_TAG, " Clicked on Item: " + position);
				Log.i(LOG_TAG, " View ID: " + v.getId());
				//Log.i(LOG_TAG, " Sendbutton ID " + sendbutton.getId());


				String music_file_key=((ArrayList<DataObject>)results).get(position).getmText1();
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

					TransferObserver observer=transferUtility.download(mBucket, music_file_key, local_stored_file);
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
			public void onCardClick(int position, View v) {
				// TODO Auto-generated method stub
				Log.d(LOG_TAG,"onCardClick");
				//String audioFile = "/storage/emulated/0/mad/Aiio_Raaama.mp3" ; 
				//String audioFile ="http://www.stephaniequinn.com/Music/The%20Irish%20Washerwoman.mp3";
				actionBar.setTitle("Fetching Clip");
				String audioFilename=((ArrayList<DataObject>)results).get(position).getmText1();
				String audioFile =mlink+audioFilename;
				mMediaPlayer.reset();
				try 
				{	

					mMediaPlayer.setDataSource(MainActivity2.this,Uri.parse(audioFile));
					//mMediaPlayer.setDataSource(audioFile);
					mMediaPlayer.prepareAsync();
					mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

					AppEventsLogger logger = AppEventsLogger.newLogger(v.getContext());
					Bundle parameters = new Bundle();
					parameters.putString("fields", " ");
					parameters.putString(AppEventsConstants.EVENT_PARAM_MAX_RATING_VALUE, "1");
					parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "mp3");
					parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, user_id);
					parameters.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, audioFilename);
					//parameters.putString("app_event_parameter1", music_file_key);
					//logger.logEvent("Custom_Rating2", 1,parameters);
					logger.logEvent("fb_mobile_music_played", 1,parameters);

				} catch (IOException e) {
					Log.e("PlayAudioDemo", "Could not open file " + audioFile + " for playback.", e);
				}

			}
		},getApplicationContext());
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
		{	mMediaPlayer.pause();
		    
		}

	}
	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		//return -1;
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
		if (!mMediaPlayer.isPlaying())
			setTitle(mTitle);
		else
			actionBar.setTitle("Playing clip");
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
		mMediaPlayer.stop();
		mMediaPlayer.release();
		//LoginManager.getInstance().logOut();
		Log.d(LOG_TAG,"User logged out");

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
		//	Uri suri=Uri.parse("content://");

		String music_file_key=((ArrayList<DataObject>)results).get(position).getmText1();
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


			//track events
			//working in all flows
			AppEventsLogger logger = AppEventsLogger.newLogger(v.getContext());
			Bundle parameters = new Bundle();
			parameters.putString("fields", " ");
			parameters.putString(AppEventsConstants.EVENT_PARAM_MAX_RATING_VALUE, "1");
			parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "mp3");
			parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, user_id);
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
				MessengerUtils.finishShareToMessenger(this, shareToMessengerParams);
				Log.d("Main_Activity","After send button clicked reply flow");

			} else {
				// Otherwise, we were launched directly (for example, user clicked the launcher icon). We
				// initiate the broadcast flow in Messenger. If Messenger is not installed or Messenger needs
				// to be upgraded, this will direct the user to the play store.

				MessengerUtils.shareToMessenger(
						this,
						REQUEST_CODE_SHARE_TO_MESSENGER,
						shareToMessengerParams);
				Log.d("Main_Activity","After send button clicked normal flow");
			}
		}

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		Log.d(LOG_TAG,"onCreateOptionsMenu() searchViewid:"+menu.findItem(R.id.search).toString());

		// Associate searchable configuration with the SearchView
		SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		Log.d(LOG_TAG,"onCreateOptionsMenu() searchView:"+searchView.toString());


		SearchView.SearchAutoComplete searchAutoComplete = 
				(SearchView.SearchAutoComplete)searchView.
				findViewById(android.support.v7.appcompat.R.id.search_src_text);
		searchAutoComplete.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.hint_grey));
		searchAutoComplete.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

		View searchplate = (View)searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
		searchplate.setBackgroundResource(R.drawable.mysearchview_edit_text_holo_light);
		//searchplate.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

		//ImageView searchIcon = (ImageView)searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
		//searchIcon.setImageResource(R.drawable.hint_search);

		//binds search string and starts intent with ACTION_SEARCH
		SearchableInfo searchableInfo = (searchManager.getSearchableInfo(getComponentName()/*new ComponentName(MainActivity.this,SearchResultsActivity.class)*/));

		Log.d(LOG_TAG,"onCreateOptionsMenu() searchableInfo:"+searchableInfo.toString());

		searchView.setSearchableInfo(searchableInfo);

		Log.d(LOG_TAG,"onCreateOptionsMenu() searchableInfo:"+searchManager.getSearchableInfo(getComponentName())); //always gives null ??
		Log.d(LOG_TAG,"onCreateOptionsMenu() getcomponent name:"+getComponentName()); //always gives null ??
		Log.d(LOG_TAG,"onCreateOptionsMenu() seacrh in:"+searchableInfo.getSearchActivity().toString());
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//			if (id == R.id.action_settings) {
		//				return true;
		//			}
		//			else
		if (id == R.id.search) {
			Log.d("MainActivity","search icon clicked");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		boolean drawerOpen = isNavDrawerOpen();
		menu.findItem(R.id.search).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	protected boolean isNavDrawerOpen() {
		return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
	}

	protected void closeNavDrawer() {
		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
		}
	}
	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener
	implements NavigationView.OnNavigationItemSelectedListener {
		@Override
		public boolean onNavigationItemSelected(MenuItem menuitem) {
			//selectItem(position);
			menuitem.setChecked(true);

			setTitle(menuitem.getTitle());
			Log.d(LOG_TAG,"Clicked title in Navig View:"+menuitem.getTitle().toString());
			if(menuitem.getTitle().toString().contains("Tamil")){
				Log.d(LOG_TAG,"Loading bucket Tamil");
				mBucket=Utils.BUCKET;
				mlink=Utils.LINK+mBucket+"/";
				progress.setTitle("Loading");
				progress.setMessage("Wait while loading...");
				progress.show();
				getbucketlist();
			}
			else if(menuitem.getTitle().toString().contains("English")){
				Log.d(LOG_TAG,"Loading bucket English");
				mBucket=Utils.BUCKET2;
				mlink=Utils.LINK2+mBucket+"/";
				progress.setTitle("Loading");
				progress.setMessage("Wait while loading...");
				progress.show();
				getbucketlist();
			}
			else if(menuitem.getTitle().toString().contains("About")){
				Log.d(LOG_TAG,"Switching to About_activity");
				// Application code
				Intent i=new Intent(MainActivity2.this,about.class);
				startActivity(i);
			}
			closeNavDrawer();
			return true;
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		Log.d(LOG_TAG,"setting title:"+mTitle);
		actionBar.setTitle("MAD > "+mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		Log.d(LOG_TAG,"onpostcreate...");
		//mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		Log.d(LOG_TAG,"onconfigchanged...");
		mDrawerToggle.onConfigurationChanged(newConfig);
	}



	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		//The CallbackManager manages the callbacks into the FacebookSdk from an 
		//  Activity's 
		//  or Fragment's onActivityResult() method.
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}*/


	@Override
	public void onBackPressed()
	{
	     // code here to show dialog
		// Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
        builder.setMessage(R.string.dialog_message)
        	   .setTitle(R.string.dialog_title)
               
        	   .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                      finish();
                   }
               })
               
               .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               }).show();

		
		
	    // super.onBackPressed();  // optional depending on your needs
	}
	
}
