package com.mobilize.greendrive;

import java.io.InputStream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

public class GreenDrive extends Activity {
	private EditText etUsername;
	private EditText etPassword;
	private Button btnLogin;
	private Button btnCancel;
	private Button btnSignup;
	final String tag = "Login:";
	private SharedPreferences settings;
	public static final String PREFS_NAME = "CURREENTUSER";
	private ProgressDialog progressDialog;
	Thread t;

	private final boolean checkLoginInfo() {
		boolean email_set = settings.contains("email");
		boolean password_set = settings.contains("password");
		if (email_set || password_set) {
			return true;
		}
		return false;
	}

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		setContentView(R.layout.login);

		if (!checkLoginInfo()) {
			etUsername = (EditText) findViewById(R.id.email);
			etPassword = (EditText) findViewById(R.id.password);
			btnLogin = (Button) findViewById(R.id.login_button);
			btnCancel = (Button) findViewById(R.id.cancel_button);
			btnSignup = (Button) findViewById(R.id.signup_button);
			
			btnLogin.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String email = etUsername.getText().toString();
					String password = etPassword.getText().toString();
					if(email == null || email.length()==0) {
						//validateTv.setText("");
						Context context = getApplicationContext();
						CharSequence text = "Please Input Your Email!";
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(context, text, duration);
						toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
						toast.show();
					} else if(password ==null || password.length()==0) {
						//validateTv.setText("Please Input Password");
						Context context = getApplicationContext();
						CharSequence text = "Please Input Your Password!";
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(context, text, duration);
						toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
						toast.show();
					} else {
						showDialog(0);
						t = new Thread() {
							public void run() {
								callJsonLogin();
							}
						};
						t.start();
					}
				}
			});
			
			btnSignup.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(), Signup.class);
					startActivity(intent);
					finish();
				}
			});

			btnCancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Close the application
					finish();
				}
			});
		} else {
			/*
			 * directly open welcome page, if the email and password is already
			 * available in the SharedPreferences
			 */
			Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
			startActivity(intent);
			finish();
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0: {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Please wait while signing in...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			return progressDialog;
		}
		}
		return null;
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String loginmsg = (String) msg.obj;
			if (loginmsg.equals("SUCCESS")) {
				removeDialog(0);
				Intent intent = new Intent(getApplicationContext(),
						HomeActivity.class);
				startActivity(intent);
				finish();
			} else {
				removeDialog(0);
				Context context = getApplicationContext();
				CharSequence text = "Email/Password is not right!";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
				toast.show();
			}
		}
	};

	public void callJsonLogin() {
		String email = etUsername.getText().toString();
		String password = etPassword.getText().toString();
		DefaultHttpClient hc = new DefaultHttpClient();
		HttpResponse response;
		JSONObject json = new JSONObject();
		try {
			//HttpPost postMethod = new HttpPost("http://192.168.72.128:3000/sessions.json?email=" + email + "&password=" + password);
			HttpPost postMethod = new HttpPost("http://greendrive.heroku.com/sessions.json?email="+ email + "&password=" + password);

			StringBuilder jsonString = new StringBuilder("{email:" + email
					+ ",password:" + password + "}");
			JSONObject loginDetail = new JSONObject(jsonString.toString());
			json.put("session", loginDetail);
			StringEntity se = new StringEntity(json.toString());
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			postMethod.setEntity(se);
			response = hc.execute(postMethod);
			if ((response!=null) && (response.getStatusLine().getStatusCode() == 200)) {
				InputStream in = response.getEntity().getContent();
				String result = Utility.convertStreamToString(in);
				Log.d(tag, result);
				JSONObject resultJson = new JSONObject(result);
				JSONObject userObject = resultJson.getJSONObject("user");
				String uid = null;
				if (userObject.has("id")) {
					uid = userObject.getString("id");
				}
				if (uid == null || uid.equalsIgnoreCase("-1")) {
					//Intent intent = new Intent(getApplicationContext(), LoginError.class);
					//intent.putExtra("LoginMessage", parsedLoginDataSet.getMessage());
					//startActivity(intent);
					//removeDialog(0);
					Message myMessage = new Message();
	                myMessage.obj = "FAIL";
	                handler.sendMessage(myMessage);
				} else {
					// Store user information in SharedPreferences after the successful login
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("email", email);
                    editor.putString("password", password);
                    editor.putString("id", uid);
              

					if (userObject.has("phone")) {
						editor.putString("phone", userObject.getString("phone"));
					}
					if (userObject.has("carengine")) {
						editor.putString("fuel", userObject.getString("carengine"));
					}
					
					editor.commit();
                    Message myMessage = new Message();
                    myMessage.obj = "SUCCESS";
                    handler.sendMessage(myMessage);
				}
			} else {
				Message myMessage = new Message();
                myMessage.obj = "FAIL";
                handler.sendMessage(myMessage);
			}
			hc.getConnectionManager().shutdown();
		} catch (Exception e) {
			removeDialog(0);
			e.printStackTrace();
		}
	}
}