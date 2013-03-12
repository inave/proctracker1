package de.vogella.android.ownservice.local;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.PowerManager;
import android.util.Log;

public class MyStartServiceReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

	WakeLocker.acquire(context);
	Log.d("aaa", "MyStartServiceReceiver: ");
	Intent service = new Intent(context, LocalWordService.class);
    context.startService(service);
    WakeLocker.release();
    
    /*
	//Ignore cases where the screen is off because we are in a call
	AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	if(manager.getMode()!=AudioManager.MODE_IN_CALL)
	{
	    //check if screen is off. If off, kill the alarm.
	    PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
	    if( !pm.isScreenOn())
	    {
	    	//Just verify again to avoid timing issue
	    	if(manager.getMode()!=AudioManager.MODE_IN_CALL)
	    	{
			    AlarmManager AlarmService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				Intent i = new Intent(context, MyStartServiceReceiver.class);
				PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
				AlarmService.cancel(pending);
				Log.d("aaa", "Terminate Alarm: Screen is off and not in a call");
	    	}
	    }
	}
	*/
  }
} 

