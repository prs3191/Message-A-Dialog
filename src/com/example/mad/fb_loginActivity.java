package com.example.mad;


import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.MessengerUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class fb_loginActivity extends Activity {

	private LoginButton loginButton ;
	private CallbackManager callbackManager;
	private String user_access_token;
	private String user_id;
	private String user_name;
	private MessengerThreadParams mThreadParams;
	private boolean mPicking;
	private String LOG_TAG="fbLoginActivity";
	private Intent intent;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		//intent = getIntent();
		/*	Log.d(LOG_TAG,"What is intent action received:\n"+intent.getAction());
		if (Intent.ACTION_PICK.equals(intent.getAction())) {
			
			//mThreadParams = MessengerUtils.getMessengerThreadParamsForIntent(intent);
			//mPicking = true;
			
			if(user_access_token!=null){
				user_access_token=AccessToken.getCurrentAccessToken().getToken();
				Log.d(LOG_TAG,"access_token from reply flow:\n"+user_access_token);
			}
			else
			{
				Log.d(LOG_TAG,"access_token from reply flow:\n"+user_access_token);
				login();
				
			}
			
			// Note, if mThreadParams is non-null, it means the activity was launched from Messenger.
			// It will contain the metadata associated with the original content, if there was content.
		}
		
		*/
		
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		login();
		
	}
	
	private void login(){
		
		
		FacebookSdk.sdkInitialize(this.getApplicationContext());
		callbackManager = CallbackManager.Factory.create();

		
		setContentView(R.layout.fb_login);

		loginButton = (LoginButton) findViewById(R.id.login_button);
		loginButton.setReadPermissions("read_insights");

		// Callback registration
		loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

			@Override
			public void onSuccess(LoginResult result) {
				// TODO Auto-generated method stub
				Log.d("FB_Login","login_activity:"+result);
				Log.d("FB_Login","login_activity:"+
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
									Log.d("Graph api",""+user_name);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									Log.d("Graph api json error",""+e);
								}



								// Application code
								Intent i=new Intent(fb_loginActivity.this,MainActivity.class);
								i.putExtra("user_access_token",user_access_token);
								i.putExtra("user_id",user_id);
								i.putExtra("user_name",user_name);
								startActivity(i);
							}

						});
				Bundle parameters = new Bundle();
				parameters.putString("fields", "name"/*,id,link,cover,email*/);
				request.setParameters(parameters);
				request.executeAsync();

				//				Intent i=new Intent(fb_loginActivity.this,MainActivity.class);
				//				i.putExtra("user_access_token",result.getAccessToken().getToken());
				//				i.putExtra("user_id",result.getAccessToken().getUserId());
				//i.putExtra("user_name",result.getAccessToken().);
				//i.putExtra("ds", d);

				//startActivity(i);
				//startActivityForResult(i, 1);
			}

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				Log.d("FB_Login","login_activity:Oncancel()");
				Toast.makeText(getApplicationContext(), "FB Login Cancelled. Try Again !", 
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(FacebookException error) {
				// TODO Auto-generated method stub
				Log.d("FB_Login","login_activity:"+error);
				Toast.makeText(getApplicationContext(), "FB Login Failed. Try Again !", 
						Toast.LENGTH_SHORT).show();
			}

		});   
		

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		//The CallbackManager manages the callbacks into the FacebookSdk from an 
		//  Activity's 
		//  or Fragment's onActivityResult() method.
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}




}
