package com.example.mad;

import java.io.File;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
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
import com.example.mad.DataObject;
import com.example.mad.MyRecyclerViewAdapter;
import com.facebook.CallbackManager;
import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.ShareButton;

import com.facebook.share.widget.SendButton;




import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {


	List<S3ObjectSummary> summaries;
	ArrayList results = new ArrayList<DataObject>();

	private RecyclerView mRecyclerView;
	private RecyclerView.Adapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private static String LOG_TAG = "CardViewActivity";
	//	private static boolean DOWNLOAD_CLICKED=false;
	//	private static int send_button_position=-1;
	private String link="file:///storage/emulated/0/mad/";

	// This is the request code that the SDK uses for startActivityForResult. See the code below
	// that references it. Messenger currently doesn't return any data back to the calling
	// application.
	private static final int REQUEST_CODE_SHARE_TO_MESSENGER = 1;
	private View mMessengerButton;
	private MessengerThreadParams mThreadParams;
	private boolean mPicking;
	private CallbackManager callbackManager;

	private TransferUtility transferUtility  ;
	private static boolean  transfer_complete=false;


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

		new File("/storage/emulated/0/"+"mad").mkdirs();
		setContentView(R.layout.activity_card_view);


		mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mLayoutManager = new LinearLayoutManager(MainActivity.this);
		mRecyclerView.setLayoutManager(mLayoutManager);



		mAdapter = new MyRecyclerViewAdapter(results);
		mRecyclerView.setAdapter(mAdapter);



		callbackManager = CallbackManager.Factory.create();


		// Code to Add an item with default animation
		//((MyRecyclerViewAdapter) mAdapter).addItem(obj, index);

		// Code to remove an item with default animation
		//((MyRecyclerViewAdapter) mAdapter).deleteItem(index);

		// If we received Intent.ACTION_PICK from Messenger, we were launched from a composer shortcut
		// or the reply flow.
		Intent intent = getIntent();
		if (Intent.ACTION_PICK.equals(intent.getAction())) {
			mThreadParams = MessengerUtils.getMessengerThreadParamsForIntent(intent);
			mPicking = true;

			// Note, if mThreadParams is non-null, it means the activity was launched from Messenger.
			// It will contain the metadata associated with the original content, if there was content.
		}



		try{
			new HttpTask().execute();
		}
		catch(Exception e){
			Log.d("Http_async_mainact", ""+e);
		}

	}




	public final class HttpTask extends AsyncTask<URL , Boolean /* Progress */, String /* Result */>
	{
		@Override
		protected String doInBackground(URL... params)
		{

			//		if(!DOWNLOAD_CLICKED)
			//	{


			// Initialize the Amazon Cognito credentials provider
			CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
					getApplicationContext(),
					Utils.POOL_ID, // Identity Pool ID
					Regions.US_EAST_1 // Region
					);
			credentialsProvider.refresh();

			Log.d("cred provider check",""+credentialsProvider.getIdentityId());

			AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
			//transferManager = new TransferManager(credentialsProvider);
			transferUtility= new TransferUtility(s3, getApplicationContext());
			try{
				//Log.d("s3 check","");
				ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(Utils.BUCKET)
				.withDelimiter("/")
				/*.withPrefix("gou/")*/;

				ObjectListing listing = s3.listObjects(listObjectsRequest);
				Log.d("after listobjects","Bucket accessed");

				summaries = listing.getObjectSummaries();

				while (listing.isTruncated()) {
					listing = s3.listNextBatchOfObjects (listing);
					summaries.addAll (listing.getObjectSummaries());
				}
				//list_files(summaries);
			}
			catch(AmazonServiceException  e){
				Log.d("s3_lisiting_se",""+e);
			}
			catch (AmazonClientException e) {
				Log.d("s3_lisiting_ce",""+e);
			}



			return null;	
			//			}
			//		else
			//			{
			//				String music_file_key=((ArrayList<DataObject>)results).get(send_button_position).getmText1();
			//				Log.i(LOG_TAG,"storage loc:"+Environment.getExternalStorageDirectory());
			//
			//				File local_storage_loc=new File(Environment.getExternalStorageDirectory()
			//						+File.separator
			//						+"Music" 
			//						+File.separator
			//						+music_file_key);
			//				Log.i(LOG_TAG,"music file key:"+music_file_key);
			//				TransferObserver observer=transferUtility.download(Utils.BUCKET, music_file_key, local_storage_loc);
			//				observer.setTransferListener(new TransferListener() {
			//
			//					@Override
			//					public void onError(int arg0, Exception arg1) {
			//						// TODO Auto-generated method stub
			//						Log.i(LOG_TAG,"transfer error:"+arg0+" arg1:"+arg1);
			//					}
			//
			//					@Override
			//					public void onProgressChanged(int arg0, long arg1, long arg2) {
			//						// TODO Auto-generated method stub
			//						Log.i(LOG_TAG,"transfer progress:"+arg0+" arg1:"+arg1+" arg2:"+arg2);
			//					}
			//
			//					@Override
			//					public void onStateChanged(int arg0, TransferState arg1) {
			//						// TODO Auto-generated method stub
			//						Log.i(LOG_TAG,"transfer state:"+arg0+" arg1:"+arg1);
			//					}
			//
			//				});
			//
			//				//DOWNLOAD_CLICKED=true;
			//				return null;
			//
			//			}

		}

		@Override
		protected void onPostExecute(String result) {
			publishProgress(false);

			//			if(!DOWNLOAD_CLICKED)
			//		{
			//			setContentView(R.layout.activity_card_view);
			//
			//
			//			mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
			//			mRecyclerView.setHasFixedSize(true);
			//
			//			mLayoutManager = new LinearLayoutManager(MainActivity.this);
			//			mRecyclerView.setLayoutManager(mLayoutManager);
			//
			//
			//
			mAdapter = new MyRecyclerViewAdapter(getDataSet());
			mRecyclerView.setAdapter(mAdapter);



			//
			//			mAdapter = new MyRecyclerViewAdapter(getDataSet());
			//			mRecyclerView.setAdapter(mAdapter);

			/*	try {
				RadioGroup radioGroup = (RadioGroup)findViewById(R.id.rg_mainactivity);
				LinearLayout.LayoutParams layoutParams = new 
						RadioGroup.LayoutParams( 
								RadioGroup.LayoutParams.WRAP_CONTENT, 
								RadioGroup.LayoutParams.WRAP_CONTENT); 

				for(S3ObjectSummary summary : summaries)
				{

						RadioButton radioButton = new RadioButton(MainActivity.this);

						radioButton.setText(summary.getKey().toString());
						radioGroup.addView(radioButton,layoutParams);

					//source.setText(node.text()); 

				}
				//ViewGroup line =(ViewGroup)findViewById(R.id.mainlayout);;
				//line.addView(radioGroup);
			}
			catch(Exception e){
				Log.d("list_files_mainactivity_rg",""+e);
			}
			try
			{
				RadioGroup radioGroup = (RadioGroup)findViewById(R.id.rg_mainactivity);

				radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup rg, int checkedId) {
						// for(int i=0; i<rg.getChildCount(); i++) {
						RadioButton btn = (RadioButton)findViewById(checkedId);
						// if(btn.getId() == checkedId) {

						TextView txt=(TextView)findViewById(R.id.list_files_txtv);
						txt.setText(btn.getText().toString());
						//    break;
						// }
						//  }
					}
				});

			}
			catch(Exception e){
				Log.d("list_files_mainactivity_rb select",""+e);
			}
			 */


			//		}
		}

	}

	private ArrayList<DataObject> getDataSet() {

		int index=0;
		for(S3ObjectSummary summary : summaries)
		{
			DataObject obj = new DataObject(summary.getKey().toString(),"Secondary " + index);
			results.add(index, obj);
			index++;
		}
		return results;
	}

	//	private ArrayList<DataObject> getDataSet2() {
	//        ArrayList results = new ArrayList<DataObject>();
	//        for (int index = 0; index < 20; index++) {
	//            DataObject obj = new DataObject("Some Primary Text " + index,
	//                    "Secondary " + index);
	//            results.add(index, obj);
	//        }
	//        return results;
	//    }

	@Override
	protected void onResume()
	{
		super.onResume();
		((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter.MyClickListener()
		{
			@Override
			public void onItemClick(int position, View v, SendButton sendbutton)
			{
				Log.i(LOG_TAG, " Clicked on Item " + position);
				Log.i(LOG_TAG, " View ID " + v.getId());
				//Log.i(LOG_TAG, " Sendbutton ID " + sendbutton.getId());


				String music_file_key=((ArrayList<DataObject>)results).get(position).getmText1();
				Log.i(LOG_TAG,"storage loc:"+Environment.getExternalStorageDirectory());

				link="file:///storage/emulated/0/mad/"+music_file_key;
				Log.i(LOG_TAG, "file path:" + link);

				File local_stored_file=new File(Environment.getExternalStorageDirectory()
						+File.separator
						+"mad" 
						+File.separator
						+music_file_key);
				Log.i(LOG_TAG,"music file key:"+music_file_key);
				// boolean  transfer_complete=false;

				if(!local_stored_file.exists())
				{

					TransferObserver observer=transferUtility.download(Utils.BUCKET, music_file_key, local_stored_file);
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
								//								Uri uri =Uri.parse(link);
								//								ShareToMessengerParams shareToMessengerParams =
								//										ShareToMessengerParams.newBuilder(uri, "audio/*")
								//										//.setMetaData("{ \"audio\" : \"tre\" }")
								//										//.setExternalUri(uri)
								//										.build();
								//								onMessengerButtonClicked(position,v,sendbutton);
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
					//					// Create the parameters for what we want to send to Messenger.
					//					Log.i(LOG_TAG,"file status:"+"already exists");
					//					Uri uri =Uri.parse(link);
					//					ShareToMessengerParams shareToMessengerParams =
					//							ShareToMessengerParams.newBuilder(uri, "audio/*")
					//							//.setMetaData("{ \"audio\" : \"tre\" }")
					//							//.setExternalUri(uri)
					//							.build();
					//	
					Log.i(LOG_TAG, " File Already EXISTS ?" + "YES");
					onMessengerButtonClicked(position,v,sendbutton);

				}










			}
		}
				);

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




			//String link="android.resource://com.example.mad/drawable/"+R.drawable.sample;
			//		Uri uri =Uri.parse(link);
			/*Uri uri =Uri.parse("https://www.google.co.in/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png");*/
			//Uri.parse("file:///storage/emulated/0/Android/data/com.getsamosa/cache/sample.mp3");
			//Uri.parse(Utils.LINK+((ArrayList<DataObject>)results).get(position).getmText1());

			// Create the parameters for what we want to send to Messenger.
			Uri uri =Uri.parse(link);
			ShareToMessengerParams shareToMessengerParams =
					ShareToMessengerParams.newBuilder(uri, "audio/*")
					//.setMetaData("{ \"audio\" : \"tre\" }")
					//.setExternalUri(uri)
					.build();


			/*Uri videoFileUri = Uri.parse(link);
		ShareVideo video = new ShareVideo.Builder()
									.setLocalUrl(videoFileUri)
									.build();
		ShareVideoContent content = new ShareVideoContent.Builder()
										.setVideo(video)
										.build();
		 Log.i(LOG_TAG,"uri:"+Uri.parse(link));
		 Log.i(LOG_TAG,"video:"+video);
		 Log.i(LOG_TAG,"content:"+content);*/

			/*ShareButton shareButton = (ShareButton)mMessengerButton;
		 shareButton.setShareContent(content);*/


			// MessageDialog messageDialog = new MessageDialog(this);
			// MessageDialog.show(this, content);

			/*ShareLinkContent content = new ShareLinkContent.Builder()
		.setContentUrl(Uri.parse(link))
		.build();*/

			//sendbutton.setShareContent(content);



			if (mPicking) {
				// If we were launched from Messenger, we call MessengerUtils.finishShareToMessenger to return
				// the content to Messenger.
				MessengerUtils.finishShareToMessenger(this, shareToMessengerParams);
			} else {
				// Otherwise, we were launched directly (for example, user clicked the launcher icon). We
				// initiate the broadcast flow in Messenger. If Messenger is not installed or Messenger needs
				// to be upgraded, this will direct the user to the play store.

				MessengerUtils.shareToMessenger(
						this,
						REQUEST_CODE_SHARE_TO_MESSENGER,
						shareToMessengerParams);
			}
		}

	}

	//
	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		// Inflate the menu; this adds items to the action bar if it is present.
	//		getMenuInflater().inflate(R.menu.main, menu);
	//		return true;
	//	}
	//
	//	@Override
	//	public boolean onOptionsItemSelected(MenuItem item) {
	//		// Handle action bar item clicks here. The action bar will
	//		// automatically handle clicks on the Home/Up button, so long
	//		// as you specify a parent activity in AndroidManifest.xml.
	//		int id = item.getItemId();
	//		if (id == R.id.action_settings) {
	//			return true;
	//		}
	//		return super.onOptionsItemSelected(item);
	//	}


}
