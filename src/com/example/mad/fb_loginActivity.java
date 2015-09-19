package com.example.mad;


import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class fb_loginActivity extends Activity {

	private LoginButton loginButton ;
	private CallbackManager callbackManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		FacebookSdk.sdkInitialize(this.getApplicationContext());
		callbackManager = CallbackManager.Factory.create();

		setContentView(R.layout.fb_login);

		loginButton = (LoginButton) findViewById(R.id.login_button);


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

				Intent i=new Intent(fb_loginActivity.this,MainActivity.class);
				i.putExtra("user_access_token",result.getAccessToken().getToken());
				//i.putExtra("ds", d);

				//startActivity(i);
				startActivityForResult(i, 1);
			}

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				Log.d("FB_Login","login_activity:Oncancel()");
			}

			@Override
			public void onError(FacebookException error) {
				// TODO Auto-generated method stub
				Log.d("FB_Login","login_activity:"+error);
			}

		});    
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

}
