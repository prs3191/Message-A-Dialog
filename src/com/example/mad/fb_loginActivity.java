package com.example.mad;


import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.FacebookSdk.InitializeCallback;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.MessengerUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
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
	private static boolean isConnected=false;
	
	private AccessTokenTracker accessTokenTracker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		intent = getIntent();
		Log.d(LOG_TAG,"What is intent action received:\n"+intent.getAction());
		isConnected=isConnectingToInternet();
		if(isConnected)
		{	
			Log.d(LOG_TAG,"oncreate() calling login");
			login();
		}
		else
			Toast.makeText(this,"Please connect to Internet", Toast.LENGTH_LONG).show();

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if(MainActivity.gotaccesstoken==false && isConnected){
			Log.d(LOG_TAG,"onresume() calling login");
			login();
			
		}

	}

	public void login(){

		FacebookSdk.sdkInitialize(getApplicationContext(),new InitializeCallback() {

			@Override
			public void onInitialized() {
				// TODO Auto-generated method stub
				Log.d(LOG_TAG,"fbsdk init");

			}
		});

		if(AccessToken.getCurrentAccessToken()==null)
		{
			Log.d(LOG_TAG,"access token null-----: so starting fbloginactivity");

		}
		else
		{
			Log.d(LOG_TAG,"access token not null-----: so not starting fbloginactivity");
		}

		accessTokenTracker = new AccessTokenTracker() {
			@Override
			protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
				updateWithToken(newAccessToken);
			}
		};


		callbackManager = CallbackManager.Factory.create();


		setContentView(R.layout.launchscreen);

		//		loginButton = (LoginButton) findViewById(R.id.login_button);
		//		loginButton.setReadPermissions("read_insights");

		// Callback registration
		//loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
		LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
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


								if(user_name!=null){


									// Application code
									Intent i=new Intent(fb_loginActivity.this,MainActivity2.class);
									i.putExtra("user_access_token",user_access_token);
									i.putExtra("user_id",user_id);
									i.putExtra("user_name",user_name);
									//if(MainActivity.mPicking)
									//i.setAction("com.example.mad.PICK");
									MainActivity2.gotaccesstoken=true;
									startActivity(i);
									finish();
								}
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

		LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("read_insights"));

	}

	private void updateWithToken(AccessToken currentAccessToken)
	{

		if(currentAccessToken==null)
		{
			Log.d(LOG_TAG,"access token null: so starting fbloginactivity");

		}
		else
		{
			Log.d(LOG_TAG,"access token not null: so not starting fbloginactivity");
		}

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
	
	public boolean isConnectingToInternet(){
       
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (cm != null) 
          {
        	  
        	  NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
               
              return activeNetwork != null &&
                      activeNetwork.isConnectedOrConnecting();
          }
          return false;
    }


}
