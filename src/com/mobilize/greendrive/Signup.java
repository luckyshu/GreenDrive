package com.mobilize.greendrive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

public class Signup extends Activity {
	//private EditText etUsername;
	private EditText etPassword;
	private EditText etEmail;
	private EditText etRePass;
	private EditText etPhone;
	private EditText etCar;
	private RadioButton rbYes;
	private RadioButton rbNo;
	private Spinner spinner;
	private int addressID = 1;
	private String address;
	
	private Button btnSignup;
	private Button btnBack;
	final String tag = "Signup:";
	private SharedPreferences settings;
	public static final String PREFS_NAME = "CURREENTUSER";
	private ProgressDialog progressDialog;
	Thread t;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		setContentView(R.layout.signup);

		//etUsername = (EditText) findViewById(R.id.username);
		etPassword = (EditText) findViewById(R.id.password);
		etEmail = (EditText) findViewById(R.id.email);
		etRePass = (EditText) findViewById(R.id.repass);
		etPhone = (EditText) findViewById(R.id.phone);
		etCar = (EditText) findViewById(R.id.car);
		rbYes = (RadioButton)findViewById(R.id.optionYes);
		rbNo = (RadioButton)findViewById(R.id.optionNo);
		spinner = (Spinner)findViewById(R.id.address);
		btnSignup = (Button) findViewById(R.id.signup_button);
		btnBack = (Button) findViewById(R.id.backbutton);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.address_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		    	addressID = pos + 1;
		    	address = parent.getItemAtPosition(pos).toString();
		    }
		    public void onNothingSelected(AdapterView parent) {
		    	// Do nothing.
		    }
		});

		btnSignup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String email = etEmail.getText().toString();
				String password = etPassword.getText().toString();
				String repass = etRePass.getText().toString();
				String fuel = etCar.getText().toString();
				//String phone = etPhone.getText().toString();
				if(email == null || email.length()==0) {
					Context context = getApplicationContext();
					CharSequence text = "Please Input Your Email!";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
					toast.show();
				} else if(password ==null || password.length()==0) {
					Context context = getApplicationContext();
					CharSequence text = "Please Input Your Password!";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
					toast.show();
				} else if(password.length() < 6) {
					Context context = getApplicationContext();
					CharSequence text = "Password should be at least 6 characters!";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
					toast.show();
				} else if(repass ==null || repass.length()==0) {
					Context context = getApplicationContext();
					CharSequence text = "Please Confirm Your Password!";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
					toast.show();
				} else if(!password.equals(repass)) {
					Context context = getApplicationContext();
					CharSequence text = "Please Input The Same Password!";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
					toast.show();
				} else if(!Utility.isDouble(fuel)) {
					Context context = getApplicationContext();
					CharSequence text = "Please Input a Number in MPG Field!";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
					toast.show();
				} else {
					showDialog(0);
					t = new Thread() {
						public void run() {
							callJsonSignup();
						}
					};
					t.start();
				}
			}
		});
		
		btnBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), GreenDrive.class);
				startActivity(intent);
				finish();
			}
		});
	}
	
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case 0: {
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("Please wait while Signing up...");
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
				CharSequence text = "Sign up fail! Please try again!";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
				toast.show();
			}
		}
	};
	
	public void callJsonSignup() {
		String email = etEmail.getText().toString();
		String password = etPassword.getText().toString();
		//String username = etUsername.getText().toString();
		String repass = etRePass.getText().toString();
		String phone = etPhone.getText().toString();
		String car = etCar.getText().toString();
		int isPool=1;
		if(rbYes.isChecked() == true)
			isPool=1;
		if(rbNo.isChecked() == true)
			isPool=0;
		if(phone==null) {
			phone="";
		}
		
		DefaultHttpClient hc = new DefaultHttpClient();
		HttpResponse response;
		JSONObject json = new JSONObject();
		try {
			//HttpPost postMethod = new HttpPost("http://192.168.72.128:3000/users.json");
			HttpPost postMethod = new HttpPost("http://greendrive.heroku.com/users.json");
	
			postMethod.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));	
			StringBuilder jsonString = new StringBuilder("{email:\"" + email +
					"\",password:\"" + password + "\",password_confirmation:\"" + 
					repass + "\",phone:\"" + phone + "\",ispool:\"" + isPool + 
					"\", address_id:\"" + addressID + "\",carengine:\"" + car + "\"}");
			//Log.d(tag, jsonString.toString());
			JSONObject signupDetail = new JSONObject(jsonString.toString());
			json.put("user", signupDetail);
			StringEntity se = new StringEntity(json.toString());
			//se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			postMethod.setEntity(se);
			response = hc.execute(postMethod);
			if ( (response!=null) && (response.getStatusLine().getStatusCode() == 200) ) {
				InputStream in = response.getEntity().getContent();
				String result = Utility.convertStreamToString(in);
				Log.d(tag, result);
				
				JSONObject resultJson = new JSONObject(result);
				JSONObject userObject = resultJson.getJSONObject("user");
				String uid = null;
				if (userObject!=null && userObject.has("id")) {
					uid = userObject.getString("id");
				}
				if (uid == null || uid.equalsIgnoreCase("-1")) {
					//Intent intent = new Intent(getApplicationContext(),LoginError.class);
					//Log.d(tag, "sign up fail");
					//intent.putExtra("LoginMessage", parsedLoginDataSet.getMessage());
					//startActivity(intent);
					//removeDialog(0);
					Message myMessage = new Message();
                    myMessage.obj = "FAIL";
                    handler.sendMessage(myMessage);
				} else {
					Log.d(tag, "sign up succeed!");
					// Store user information in SharedPreferences after the successful login
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("email", email);
                    editor.putString("password", password);
					editor.putString("id", uid);
					editor.putString("address", address);
					editor.putString("fuel", car);
					
					if (userObject.has("username")) {
						editor.putString("username", userObject.getString("username"));
					}
					if (userObject.has("phone")) {
						editor.putString("phone", userObject.getString("phone"));
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
			Message myMessage = new Message();
            myMessage.obj = "FAIL";
            handler.sendMessage(myMessage);
			e.printStackTrace();
		}
	}
}
