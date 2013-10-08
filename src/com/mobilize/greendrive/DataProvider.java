package com.mobilize.greendrive;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class DataProvider extends ContentProvider {
	private static final String myURI = 
		"content://com.mobilize.provider.greendrive/speed";
	public static final Uri CONTENT_URI = Uri.parse(myURI);
	
	//The Database
	private SQLiteDatabase speedDB;
	private static final String TAG = "DataProvider";
	private static final String DATABASE_NAME = "speed.db";
	private static final int DATABASE_VERSION = 1;
	private static final String SPEED_TABLE = "speed";
	
	//Column Names
	public static final String KEY_ID = "_id";
	public static final String KEY_TIME = "time";
	public static final String KEY_VELOCITY = "velocity";
	public static final String KEY_LAT = "latitude";
	public static final String KEY_LNG = "longtitude";

	//Column Indexes
	public static final int TIME_COLUMN = 1;
	public static final int VELOCITY_COLUMN = 2;
	public static final int LONGTITUDE_COLUMN = 3;
	public static final int LATITUDE_COLUMN = 4;

	private int version = DATABASE_VERSION;
	
	public int getVersion(){
		return version;
	}
	
	//Helper class 
	private static class SpeedometerDBHelper extends SQLiteOpenHelper {
		private static final String DATABASE_CREATE = 
			"create table " + SPEED_TABLE + "("
			 + KEY_ID + " integer primary key autoincrement,"
			 + KEY_TIME + " DATETIME,"
			 + KEY_VELOCITY + " DOUBLE,"
			 + KEY_LNG + " DOUBLE," 
			 + KEY_LAT + " DOUBLE)"; 
		
		public SpeedometerDBHelper (Context context, String name, 
										CursorFactory factory, int version)
		{
			super (context, name, factory, version);	
		}
		
		@Override
		public void onCreate(SQLiteDatabase db){
			db.execSQL(DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
		    Log.w(TAG, "Upgrading database from version " + oldVersion + " to " 
	                + newVersion + ", which will destroy all old data"); 	            
	        db.execSQL("DROP TABLE IF EXISTS " + SPEED_TABLE); 
		    onCreate(db); 
		}
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		/*
		Context context = getContext();
		ApproxMonitorDBHelper dbHelper;
		dbHelper = new ApproxMonitorDBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		dbHelper.onUpgrade(approxmonitorDB, DATABASE_VERSION, DATABASE_VERSION+1);
		getContext().getContentResolver().notifyChange(uri, null);*/
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri _uri, ContentValues values) {
		long rowID = speedDB.insert(SPEED_TABLE, null, values);
		if (rowID > 0)
		{
			Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;			
		}
		else	
			throw new SQLException("Failed insert row into " + _uri);
	}

    public void dropTable(){
    	Context context = getContext();
    	SpeedometerDBHelper dbHelper;
    	dbHelper = new SpeedometerDBHelper(context, DATABASE_NAME, null, version);
    	dbHelper.onUpgrade(speedDB, version, version+1);
    	version++;
    }
    
	@Override
	public boolean onCreate() {
		Context context = getContext();
		SpeedometerDBHelper dbHelper;
		dbHelper = new SpeedometerDBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		speedDB = dbHelper.getWritableDatabase();
		return (speedDB == null)?false:true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(SPEED_TABLE);
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder))
			orderBy = KEY_TIME;
		else 
			orderBy = sortOrder;
		
		Cursor c = builder.query(speedDB, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
