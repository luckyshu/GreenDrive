package com.mobilize.greendrive;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MonitorService extends Service {

	public static final String NEW_DATA_FOUND = "New_Data_Found";
	private Timer updateTimer;
	private int pauseTimes = 0;
	PauseReceiver receiver;
	ArrayList<MonitorData> datas = new ArrayList<MonitorData>();

	// Debugging
	private static final String TAG = "MonitorService";
	private static final boolean D = true;

	public static final String PREFS_NAME = "CURREENTUSER";
	private SharedPreferences settings;

	@Override
	public void onStart(Intent intent, int startId) {
		updateTimer = new Timer("ServerUpdate");
		settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

		if (pauseTimes != 0) {
			sendData.cancel();

			TimerTask sendDataNew = new TimerTask() {
				public void run() {
					doSendData();
				}
			};
			updateTimer.scheduleAtFixedRate(sendDataNew, 0,1000*60*2);
		} else {
			updateTimer.scheduleAtFixedRate(sendData, 0, 1000*60*2);
		}
	};

	private TimerTask sendData = new TimerTask() {
		public void run() {
			doSendData();
		}
	};

	private void doSendData() {
		Thread SendThread = new Thread(null, brackgroundSendData,
				"refresh_data");
		SendThread.start();
	}

	private Runnable brackgroundSendData = new Runnable() {
		public void run() {
			String uid = settings.getString("id", "-1");
			if (uid.equals("-1")) {
				return;
			}
			datas.clear();
			ContentResolver cr = getContentResolver();
			Cursor c = cr.query(DataProvider.CONTENT_URI, null, null, null,
					null);
			
			if (c.moveToFirst()) {
				do {
					long time = c.getLong(DataProvider.TIME_COLUMN);
					double speed = c.getDouble(DataProvider.VELOCITY_COLUMN);
					double lat = c.getDouble(DataProvider.LATITUDE_COLUMN);
					double lng = c.getDouble(DataProvider.LONGTITUDE_COLUMN);

					MonitorData d = new MonitorData(time, lat, lng, speed);
					datas.add(d);
				} while (c.moveToNext());
			}
			c.close();
			Log.d(TAG, "before sending ******************************");
			DefaultHttpClient hc = new DefaultHttpClient();
			HttpResponse response;

			try {
				// HttpPost postMethod = new
				// HttpPost("http://192.168.72.128:3000/speeds/saveJsonData?format=json");
				HttpPost postMethod = new HttpPost(
						"http://greendrive.heroku.com/speeds/saveJsonData?format=json");
				
				JSONArray jsonArr = new JSONArray();
				int num = datas.size();
				for (int i = 0; i < 3; ++i) {
					MonitorData md = (MonitorData) datas.get(i);
					StringBuilder speedString = new StringBuilder(
							"{collect_time:\"" + md.getTime() + "\",user_id:\""
									+ uid + "\",velocity:\"" + md.getVelocity()
									+ "\",longtitude:\"" + md.getLongtitude()
									+ "\",latitude:\"" + md.getLatitude() + "\"}");
					JSONObject speedJson = new JSONObject(speedString.toString());
					JSONObject json = new JSONObject();
					json.put("speed", speedJson);
					jsonArr.put(json);
				}
				
				Log.d(TAG, jsonArr.toString());

				StringEntity se = new StringEntity(jsonArr.toString());
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("data", jsonArr.toString()));
				postMethod.setEntity(new UrlEncodedFormEntity(params,
						HTTP.UTF_8));
				postMethod.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,
						"application/json"));
				postMethod.setEntity(se);
				response = hc.execute(postMethod);
				if (response != null
						&& response.getStatusLine().getStatusCode() == 200) {
					InputStream in = response.getEntity().getContent();
					String result = Utility.convertStreamToString(in);
					Log.d(TAG, result);
				} else {
					Log.d(TAG, "send speed data fail");
				}
				hc.getConnectionManager().shutdown();
			} catch (Exception e) {
				Log.d(TAG, "send speed data fail in catch");
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onCreate() {
		LocationManager locationManager;
		String location_context = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) getSystemService(location_context);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(true);
		criteria.setCostAllowed(false);

		String provider = locationManager.getBestProvider(criteria, true);

		Location location = locationManager.getLastKnownLocation(provider);

		if (location != null)
			updateWithNewLocation(location);

		int t = 10000;
		int distance = 135;
		locationManager.requestLocationUpdates(provider, t, distance,
				myLocationListener);

		IntentFilter filter = new IntentFilter(HomeActivity.ACTIVITY_ON_PAUSE);
		receiver = new PauseReceiver();
		registerReceiver(receiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private final LocationListener myLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);

		}

		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null);

		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String Provider, int status, Bundle extras) {
		}
	};

	private void updateWithNewLocation(Location location) {
		float speed = 0.0f;
		double speed_mph = 0.0;

		if (location != null) {
			if (location.hasSpeed()) {
				speed = location.getSpeed();
				speed_mph = speed * 3.28 / 5280 * 3600;
				double lat = location.getLatitude();
				double lng = location.getLongitude();
				MonitorData data = new MonitorData(System.currentTimeMillis(),
						lat, lng, speed_mph);
				addNewData(data);
			}
		}
	}

	private void addNewData(MonitorData _data) {
		ContentResolver cr = getContentResolver();

		ContentValues values = new ContentValues();

		values.put(DataProvider.KEY_TIME, _data.getTime());
		values.put(DataProvider.KEY_VELOCITY, _data.getVelocity());
		values.put(DataProvider.KEY_LNG, _data.getLongtitude());
		values.put(DataProvider.KEY_LAT, _data.getLatitude());

		cr.insert(DataProvider.CONTENT_URI, values);
		Intent intent = new Intent(NEW_DATA_FOUND);
		sendBroadcast(intent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public class PauseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			pauseTimes++;
		}
	}
}
