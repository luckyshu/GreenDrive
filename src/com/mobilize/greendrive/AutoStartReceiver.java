package com.mobilize.greendrive;

import android.content.BroadcastReceiver;  
import android.content.Context;  
import android.content.Intent;  
import android.util.Log;
  
public class AutoStartReceiver extends BroadcastReceiver {  
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	
    // Debugging
    private static final String TAG = "AutoStartReceiver";
    private static final boolean D = true;
    
    @Override  
    public void onReceive(Context context, Intent intent) {  
    		Intent startIntent = new Intent(context, GreenDrive.class);  
    		startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		context.startActivity(startIntent);
            if(D) Log.e(TAG, "+ start GreenDrive +");
    }  
}     
