package com.mobilize.greendrive;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class HomeActivity extends Activity {
    /** Called when the activity is first created. */
	LocationManager locationManager;
	ArrayList<MonitorData> datas = new ArrayList<MonitorData>();
	UpdateReceiver receiver;
	
	static final private int DAY_RECORD = 1; 
	static final private int WEEK_RECORD = 2; 
	static final private int MONTH_RECORD = 3; 
	static final private int LAST_RECORD = 4;
	static final private int ALL_RECORD = 5;
	
	public static final String ACTIVITY_ON_PAUSE = "Activity_On_Pause";
	public static final String PREFS_NAME = "CURREENTUSER";
	private SharedPreferences settings;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("GreenDrive");
        actionBar.setHomeAction(new IntentAction(this, createIntent(this), R.drawable.ic_title_home_default));
        //final Action shareAction = new IntentAction(this, createShareIntent(), R.drawable.ic_title_share_default);
        //actionBar.addAction(shareAction);
        final Action recoCarpoolAction = new IntentAction(this, new Intent(this, RecommendCarpool.class), R.drawable.ic_title_export_default);
        actionBar.addAction(recoCarpoolAction);
        
        Button dailyButton = (Button)findViewById(R.id.daily);
    	Button weeklyButton = (Button)findViewById(R.id.weekly);
    	Button monthlyButton = (Button)findViewById(R.id.monthly);
    	Button greenButton = (Button)findViewById(R.id.begreen);
    	
		dailyButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
		    	loadDataFromProvider(DAY_RECORD);
		    	Intent achartIntent = new SpeedHistoryChart().execute(getBaseContext(), datas, DAY_RECORD);
		    	startActivity(achartIntent);
			}
		});
		
		weeklyButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
		    	loadDataFromProvider(WEEK_RECORD);
		    	Intent achartIntent = new SpeedHistoryChart().execute(getBaseContext(), datas, WEEK_RECORD);
		    	startActivity(achartIntent);
			}
		});
		
		monthlyButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
		    	loadDataFromProvider(MONTH_RECORD);
		    	Intent achartIntent = new SpeedHistoryChart().execute(getBaseContext(), datas, MONTH_RECORD);
		    	startActivity(achartIntent);
			}
		});
		
		greenButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
		        loadDataFromProvider(ALL_RECORD);
		        String fuel = settings.getString("fuel", "30");
		        double fuelD = Double.valueOf(fuel);
		        int count = datas.size(); 
		        long time = 0l, lasttime = 0l, duration = 0l;
		        int money = 0;
		        double speed = 0, footprint = 0;
		    	if (count > 0){
		    		 for (int k = 0; k < count; k++) {
				    	 speed = datas.get(k).getVelocity();
		    			 if (speed > 1)
		    			 {
		    				 if (k == 0)
		    					 lasttime = datas.get(k).getTime();
		    				 time = datas.get(k).getTime();
		    				 if ((time - lasttime) < 30000)
		    					 duration = duration + (time - lasttime);
		    				 lasttime = time;
		    			 }
		    			 else
		    				 lasttime = datas.get(k).getTime();		    			  
		    		 }
		    		 double distance = duration/1000.0/3600.0 * 15;
		    		 //a = MPG b= distance(mile)
		    		 //co2(ton) = a/0.425*b*1.609344*2.7/1000
		    		 footprint = distance * 1.609344 * 2.7 * fuelD /0.425 /1000.0;	
   		    	  	 DecimalFormat df = new DecimalFormat("##.00");
   		    	  	 footprint = Double.parseDouble(df.format(footprint));
		    		 // 10 dollar for 1 ton co2
		    		 money = (int) (footprint * 10);

		    	}	
				Intent intent = new Intent(getApplicationContext(), GasTree.class);
				if (duration != 0 && money != 0)
				{
					Bundle gas = new Bundle();
					gas.putString("gas", Double.toString(footprint));
					intent.putExtras(gas);
			    
					Bundle tree = new Bundle();
					tree.putString("tree", Integer.toString(money));
					intent.putExtras(tree);
				}
				
		    	startActivity(intent);
			}
		});
    	
    	startService(new Intent(this, MonitorService.class));
    }
    
    public void onResume(){
		loadDataFromProvider(LAST_RECORD);
		
        TextView tv_info = (TextView)findViewById(R.id.info);
        StringBuilder sb = new StringBuilder();
        
        int count = datas.size();
        double lat = 0.0, lng = 0.0, speed = 0.0;      
        long time = 0l;
    	if (count > 0){
    		 for (int k = 0; k < count; k++) {
    			  time = datas.get(k).getTime();
		    	  lat = datas.get(k).getLatitude();
		    	  lng = datas.get(k).getLongtitude();
		    	  speed = datas.get(k).getVelocity();
		    	  DecimalFormat df = new DecimalFormat("##.00");
		    	  lat = Double.parseDouble(df.format(lat));
		    	  lng = Double.parseDouble(df.format(lng));
		    	  speed = Double.parseDouble(df.format(speed));
		     }
    		 
    		 Date last_update = new Date(time);
    		 sb.append("Lastest update information: \n\n");
    		 DateFormat d = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    		 sb.append("  Time: ").append(d.format(last_update)).append("\n");
    		 sb.append("  Latitude: ").append(lat).append("  ");
    		 sb.append("Longitude: ").append(lng).append("\n");
    		 sb.append("  Speed: ").append(speed).append(" mph");
    	}
    	else
			sb.append("No location available!");   
    	tv_info.setText(sb);
    	
    	IntentFilter filter = new IntentFilter (MonitorService.NEW_DATA_FOUND);
    	receiver = new UpdateReceiver();
    	registerReceiver(receiver, filter);
    		
    	super.onResume();
    }
    
    public void onPause(){
    	unregisterReceiver(receiver);
    	
    	Intent intent = new Intent(ACTIVITY_ON_PAUSE);
    	sendBroadcast(intent);
    	super.onPause();
    }
    
    public class UpdateReceiver extends BroadcastReceiver{
    	@Override
    	public void onReceive(Context context, Intent intent){
    		loadDataFromProvider(LAST_RECORD);
    		
            TextView tv_info = (TextView)findViewById(R.id.info);
            StringBuilder sb = new StringBuilder();
            
            int count = datas.size();
            double lat = 0.0, lng = 0.0, speed = 0.0;      
            long time = 0l;
        	if (count > 0){
        		 for (int k = 0; k < count; k++) {
        			  time = datas.get(k).getTime();
    		    	  lat = datas.get(k).getLatitude();
    		    	  lng = datas.get(k).getLongtitude();
    		    	  speed = datas.get(k).getVelocity();
    		    	  DecimalFormat df = new DecimalFormat("##.00");
    		    	  lat = Double.parseDouble(df.format(lat));
    		    	  lng = Double.parseDouble(df.format(lng));
    		    	  speed = Double.parseDouble(df.format(speed));
    		     }
        		 
           		 Date last_update = new Date(time);
        		 sb.append("Lastest update information: \n\n");
        		 DateFormat d = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        		 sb.append("  Time: ").append(d.format(last_update)).append("\n");
        		 sb.append("  Latitude: ").append(lat).append("  ");
        		 sb.append("Longitude: ").append(lng).append("\n");
        		 sb.append("  Speed: ").append(speed).append(" mph");
        	}
        	else
    			sb.append("No location available!");   
        	tv_info.setText(sb);
    	}
    }
   
    private void loadDataFromProvider(int id){
    	datas.clear();
    	ContentResolver cr = getContentResolver();
    	long current = System.currentTimeMillis();
    	Date now = new Date(current);
   		
    	switch (id)
    	{
    	case(DAY_RECORD):    		   		
    		long today = current - 1000 * (now.getHours()*3600 + now.getMinutes()*60 + now.getSeconds());
    		
    		final String SELECT_TODAY = 
    			"time > " + today;    		
    		
    		Cursor c_day = cr.query(DataProvider.CONTENT_URI, null, SELECT_TODAY, null, null);
    		
    		if (c_day.moveToFirst()){
    			do {
    				long time = c_day.getLong(DataProvider.TIME_COLUMN);
    				double speed = c_day.getDouble(DataProvider.VELOCITY_COLUMN);
    				double lat = c_day.getDouble(DataProvider.LATITUDE_COLUMN);
    				double lng = c_day.getDouble(DataProvider.LONGTITUDE_COLUMN);
    				
    				MonitorData d = new MonitorData(time, lat, lng, speed);
    				datas.add(d);
    			}while (c_day.moveToNext());
    		}
    	
    		c_day.close();
    		break;
    		
    	case(WEEK_RECORD): 
    		long pass_week = (now.getDay()-1)*24*3600 + now.getHours()*3600 + now.getMinutes()*60 + now.getSeconds();
    		long week = current - 1000 * pass_week;
    		
    		final String SELECT_WEEK = 
    			"time > " + week; 
    		
    		Cursor c_week = cr.query(DataProvider.CONTENT_URI, null, SELECT_WEEK, null, null);
    		
    		if (c_week.moveToFirst()){
    			do {
    				long time = c_week.getLong(DataProvider.TIME_COLUMN);
    				double speed = c_week.getDouble(DataProvider.VELOCITY_COLUMN);
    				double lat = c_week.getDouble(DataProvider.LATITUDE_COLUMN);
    				double lng = c_week.getDouble(DataProvider.LONGTITUDE_COLUMN);
    				
    				MonitorData d = new MonitorData(time, lat, lng, speed);
    				datas.add(d);
    			}while (c_week.moveToNext());
    		}
    	
    		c_week.close();
    		break;
    		
    	case(MONTH_RECORD): 
    		long pass_month = (now.getDate()-1)*24*3600 + now.getHours()*3600 + now.getMinutes()*60 + now.getSeconds();
    		long month = current - 1000 * pass_month;
		
			final String SELECT_MONTH = 
			"time > " + month; 
		
			Cursor c_month = cr.query(DataProvider.CONTENT_URI, null, SELECT_MONTH, null, null);
		
			if (c_month.moveToFirst()){
				do {
					long time = c_month.getLong(DataProvider.TIME_COLUMN);
					double speed = c_month.getDouble(DataProvider.VELOCITY_COLUMN);
					double lat = c_month.getDouble(DataProvider.LATITUDE_COLUMN);
					double lng = c_month.getDouble(DataProvider.LONGTITUDE_COLUMN);
				
					MonitorData d = new MonitorData(time, lat, lng, speed);
					datas.add(d);
				}while (c_month.moveToNext());
			}
	
			c_month.close();
			break;
    		
    	case(LAST_RECORD): 
    		Cursor c_last = cr.query(DataProvider.CONTENT_URI, null, null, null, null);
    	
    		if (c_last.moveToLast()){
    			long time = c_last.getLong(DataProvider.TIME_COLUMN);
				double speed = c_last.getDouble(DataProvider.VELOCITY_COLUMN);
				double lat = c_last.getDouble(DataProvider.LATITUDE_COLUMN);
				double lng = c_last.getDouble(DataProvider.LONGTITUDE_COLUMN);
	
				MonitorData d = new MonitorData(time, lat, lng, speed);
				datas.add(d);
    		}
    		c_last.close();
    		break;
    		
    	case(ALL_RECORD): 
    		Cursor c_all = cr.query(DataProvider.CONTENT_URI, null, null, null, null);
    	
    		if(c_all.moveToFirst()){
    			do{
    				long time = c_all.getLong(DataProvider.TIME_COLUMN);
    				double speed = c_all.getDouble(DataProvider.VELOCITY_COLUMN);
    				double lat = c_all.getDouble(DataProvider.LATITUDE_COLUMN);
    				double lng = c_all.getDouble(DataProvider.LONGTITUDE_COLUMN);
	
    				MonitorData d = new MonitorData(time, lat, lng, speed);
    				datas.add(d);
    			}while(c_all.moveToNext());
    		}
    		c_all.close();
    		break;
    	}
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.logout:
			//Toast.makeText(this, "Logout", Toast.LENGTH_LONG).show();
			SharedPreferences.Editor editor = settings.edit();
			editor.remove("id");
			editor.remove("email");
			editor.remove("password");
			editor.remove("phone");
			editor.remove("address");
			editor.remove("fuel");
			editor.commit();
			Intent intent = new Intent(getApplicationContext(), GreenDrive.class);
			startActivity(intent);
			finish();
			break;
		case R.id.about:
			Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
			startActivity(aboutIntent);
			break;
			
		}
		return true;
	}
	
	public static Intent createIntent(Context context) {
        Intent i = new Intent(context, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    private Intent createShareIntent() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Shared from the ActionBar widget.");
        return Intent.createChooser(intent, "Share");
    }
}