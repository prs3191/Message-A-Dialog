/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.example.mad;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.share.widget.ShareDialog;

/**
 * Application object for sample app.
 */
public class MessengerSendSampleApplication extends Application {

	private CallbackManager callbackManager;
	private String FBTAG="after sdk init";
	//@Override
//	public void onCreate() {
	//	super.onCreate();
		
		//FacebookSdk.sdkInitialize(this.getApplicationContext());
		//AccessToken.getCurrentAccessToken();

		//    Map<String, String> logins = new HashMap<String, String>();
		//	logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
		////	credentialsProvider.setLogins(logins);
		//	Log.d("Access Token from fb to aws:",""+logins);
		//
		//	for (Map.Entry entry : logins.entrySet()) {
		//		Log.d("Access Token from fb to aws:",""+entry.getKey() + ", " + entry.getValue());
		//	}
		//    GraphRequest request = GraphRequest.newMeRequest(
		//            AccessToken.getCurrentAccessToken(),
		//            new GraphRequest.GraphJSONObjectCallback() {
		//             	@Override
		//				public void onCompleted(JSONObject jsonObject,
		//						GraphResponse response) {
		//					 Log.d("after sdk init","onCompleted jsonObject: "+jsonObject);
		//	                 Log.d("after sdk init","onCompleted response: "+response);
		//	                    // Application code
		//					
		//				}
		//            });
		//    Bundle parameters = new Bundle();
		//    parameters.putString("fields", "id,name,link,cover,email");
		//    request.setParameters(parameters);
		//    request.executeAsync();
		//
		//  }*/
//		FacebookSdk.sdkInitialize(this.getApplicationContext());
//		callbackManager = CallbackManager.Factory.create();
//		if(AccessToken.getCurrentAccessToken()!=null){
//			Log.d(FBTAG,"facebook already logged in");
//			//isFBLogin = true;
//		}
//		
//	}
}
