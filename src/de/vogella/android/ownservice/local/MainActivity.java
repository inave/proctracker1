package de.vogella.android.ownservice.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MainActivity extends ListActivity {
  private static final Logentry Logentry = null;
private LocalWordService s;
  private MyScheduleReceiver thereciever = new MyScheduleReceiver() ;
  
/** Called when the activity is first created. */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    wordList = new ArrayList<String>();
    adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, android.R.id.text1,
        wordList);
    setListAdapter(adapter);
    doBindService();
    
	IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	//filter.addAction(Intent.ACTION_SCREEN_OFF);
	registerReceiver(thereciever, filter);
	Log.d("aaa", "Service Started" );
	
	Intent intent = new Intent();
	intent.setAction("de.vogella.android.ownservice.local.startup");
	sendBroadcast(intent);
  }
  @Override
  protected void onDestroy() {
      Log.d("aaa", "Reciever Destroyed" );
      unregisterReceiver(thereciever);
      super.onDestroy();
  }

  private ServiceConnection mConnection = new ServiceConnection() {

    public void onServiceConnected(ComponentName className, IBinder binder) {
      s = ((LocalWordService.MyBinder) binder).getService();
      Toast.makeText(MainActivity.this, "Connected",
          Toast.LENGTH_SHORT).show();
    }

    public void onServiceDisconnected(ComponentName className) {
      s = null;
    }
  };
  private ArrayAdapter<String> adapter;
  private List<String> wordList;

  void doBindService() {
    bindService(new Intent(this, LocalWordService.class), mConnection,
        Context.BIND_AUTO_CREATE);
  }

  public void showServiceData(View view) {
    if (s != null) 
    {
    	try
    	{
	        BufferedReader fr = new BufferedReader(new FileReader(new File("/mnt/sdcard/task_log_file.txt")));
	        String receiveString = " ";
	        String[] split;
//	        ArrayList<Logentry> log = new ArrayList<Logentry>();
	        Map<String, Appstat> stat = new HashMap<String, Appstat>();
	        
	        wordList.clear();
	        while ((receiveString = fr.readLine()) != null)
	        {
	        	split = receiveString.split(";");
	        	if(split.length == 4)
	        	{
	    	        Logentry entry = new Logentry();
//	    	        Appstat appstat = new Appstat();
	    	        Appstat tmpstat = null;
	    	        
	        		entry.applabel = split[0];
	        		entry.apppackage = split[1];
	        		entry.start = getParseTime(split[2]);
	        		entry.end = getParseTime(split[3]);
	        		entry.length = Math.abs((entry.start.getTime() - entry.end.getTime())/1000);
	        		tmpstat = (Appstat)stat.get(entry.apppackage);
	        		if (tmpstat == null)
	        		{
	        			//Add new entry
	        			tmpstat = new Appstat();
	        			tmpstat.applabel = entry.applabel;
	        			tmpstat.apppackage = entry.apppackage;
	        			tmpstat.playtime = entry.length;
	        			stat.put(entry.apppackage, tmpstat);
	        		}
	        		else
	        		{
	        			//Add time to existing entry
	        			tmpstat.playtime += entry.length;
	        			stat.put(tmpstat.apppackage, tmpstat);
	        		}
	        		
	        	}	        	
	        }
	        fr.close();

            String line = new String();
	        for (Appstat value : stat.values()) 
	        {
	        	long h = value.playtime/3600;
	        	long m = value.playtime/60 - h*60;
	        	long s = value.playtime - h*3600 - m*60;
	        	
	        	
	            line = String.format("%s - %d:%d:%d", value.applabel, h, m, s);
	        	wordList.add(line);
            }
        	adapter.notifyDataSetChanged();
    	}
    	catch (IOException e)
    	{
    	}
    }
  }

public Date getParseTime(String t)
  {
	Date date1 = null;
	
  	SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss aa"); 
  	try
  	{
  		date1 = dateFormat.parse(t);
  	}
  	catch (ParseException e)
  	{
  		
  	}
  	
  	return date1;
  }
} 

class Logentry
{
	String applabel;
	String apppackage;
	Date start;
	Date end;
	long length;
}

class Appstat
{
	String applabel;
	String apppackage;
	long playtime;
}
