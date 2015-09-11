package com.example.mad;

import java.net.URL;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;


import android.support.v7.app.ActionBarActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	
	List<S3ObjectSummary> summaries;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
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
		protected String doInBackground(URL... params) {


			// Initialize the Amazon Cognito credentials provider
		CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
			    getApplicationContext(),
			    Utils.POOL_ID, // Identity Pool ID
			    Regions.US_EAST_1 // Region
			);
			credentialsProvider.refresh();
			
			Log.d("cred provider check",""+credentialsProvider.getIdentityId());
		
			AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
			//TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
			try{
				//Log.d("s3 check","");
				ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(Utils.BUCKET)
				/*.withPrefix("")*/;
				
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
		}
		
		@Override
		protected void onPostExecute(String result) {
			publishProgress(false);

			
			try {
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

			
			
		}
	
			
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

}
