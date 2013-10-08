package com.mobilize.greendrive;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RecommendCarpool extends ListActivity {

	private SharedPreferences settings;
	public static final String PREFS_NAME = "CURREENTUSER";
	private ProgressDialog progressDialog;
	Thread t;
	final String tag = "RecommendCarpool:";
	private ListView listView;
	private LayoutInflater mInflater;
	private ArrayList<User> data;
	User user;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recommend_carpool);
		settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		
		mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("People in the same pool");
		actionBar.setHomeAction(new IntentAction(this, HomeActivity
				.createIntent(this), R.drawable.ic_title_home_default));
		actionBar.setDisplayHomeAsUpEnabled(true);
		//actionBar.addAction(new IntentAction(this, createShareIntent(),
		//		R.drawable.ic_title_share_default));

		data = new ArrayList<User>();

		showDialog(0);
		t = new Thread() {
			public void run() {
				callJsonRecommendCarpools();
			}
		};
		t.start();
	}

	private void fillListview() {
		CustomAdapter adapter = new CustomAdapter(this,
				R.layout.recommend_carpool, R.id.title, data);
		setListAdapter(adapter);
		getListView().setTextFilterEnabled(true);
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		Toast.makeText(getApplicationContext(),
				"You have selected " + (position + 1) + "th item",
				Toast.LENGTH_SHORT).show();
	}

	private class CustomAdapter extends ArrayAdapter<User> {
		public CustomAdapter(Context context, int resource,
				int textViewResourceId, List<User> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			TextView title = null;
			TextView detail = null;
			ImageView i11 = null;
			User user = getItem(position);
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.recommend_listview,
						null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			}
			holder = (ViewHolder) convertView.getTag();
			title = holder.gettitle();
			title.setText(user.getEmail());
			detail = holder.getdetail();
			detail.setText(user.getPhone());
			i11 = holder.getImage();
			i11.setImageResource(R.drawable.icon);
			return convertView;
		}

		private class ViewHolder {
			private View mRow;
			private TextView title = null;
			private TextView detail = null;
			private ImageView i11 = null;

			public ViewHolder(View row) {
				mRow = row;
			}

			public TextView gettitle() {
				if (null == title) {
					title = (TextView) mRow.findViewById(R.id.title);
				}
				return title;
			}

			public TextView getdetail() {
				if (null == detail) {
					detail = (TextView) mRow.findViewById(R.id.detail);
				}
				return detail;
			}

			public ImageView getImage() {
				if (null == i11) {
					i11 = (ImageView) mRow.findViewById(R.id.img);
				}
				return i11;
			}
		}
	}

	private void callJsonRecommendCarpools() {
		String uid = settings.getString("id", "-1");
		if (uid!=null && uid.equals("-1")) {
			Context context = getApplicationContext();
			CharSequence text = "Please Login!";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}

		DefaultHttpClient hc = new DefaultHttpClient();
		// HttpGet httpget = new HttpGet(
		// "http://192.168.72.128:3000/users/recommendUsers?format=json&user_id="
		// + uid);
		HttpGet httpget = new HttpGet(
				"http://greendrive.heroku.com/users/recommendUsers?format=json&user_id="
						+ uid);
		HttpResponse response;

		try {
			response = hc.execute(httpget);
			if (response != null
					&& response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					String result = Utility.convertStreamToString(instream);
					JSONArray userArr = new JSONArray(result);
					int num = userArr.length();
					Log.d(tag, "get " + num + " user data");
					for (int i = 0; i < num; ++i) {
						JSONObject obj = new JSONObject(userArr
								.getJSONObject(i).getString("user"));
						User user = new User();
						user.setAddress(obj.getString("address_id"));
						user.setEmail(obj.getString("email"));
						user.setPhone(obj.getString("phone"));
						data.add(user);
					}
					Message myMessage = new Message();
	                myMessage.obj = "SUCCESS";
	                handler.sendMessage(myMessage);
				}
			} else {
				Log.d(tag, "get recommend carpool data fail");
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

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String loginmsg = (String) msg.obj;
			if (loginmsg.equals("SUCCESS")) {
				removeDialog(0);
				fillListview();
			} else {
				removeDialog(0);
				Context context = getApplicationContext();
				CharSequence text = "Update fail, please check network connection!";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
				toast.show();
			}
		}
	};

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0: {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Loading...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			return progressDialog;
		}
		}
		return null;
	}

	private Intent createShareIntent() {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, "Shared from the ActionBar widget.");
		return Intent.createChooser(intent, "Share");
	}
}