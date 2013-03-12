package de.vogella.android.ownservice.local;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.format.Time;
import android.util.Log;
import android.content.pm.ApplicationInfo;

public class LocalWordService extends Service {
  private final IBinder mBinder = new MyBinder();
  private ArrayList<String> list = new ArrayList<String>();
  private static final String CURRENTTASKFILENAME = "CurrentTask.txt";
  private static final String TASKLOG = "TaskLog.txt";
  private String currentTask = "";

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    
	Log.d("aaa", "LocalWordService:onStartCommand ");
	try
	{
		getRunningApp();		  
	}
	catch (NameNotFoundException e)
	{
	 
	}
    return Service.START_NOT_STICKY;
  }
  
  public void getRunningApp() throws NameNotFoundException
  {
  	  String  newcurrentTask = "";
  	  boolean updateCurrentTask = false;
  	  boolean closeTask = false;
  	  boolean openTask = false;
  	  boolean logtofile = true;
  	  boolean godark = false;
  	  
	  ActivityManager actvityManager = (ActivityManager)this.getSystemService( ACTIVITY_SERVICE );
	  /*
	  List<RunningAppProcessInfo> procInfos = actvityManager.getRunningAppProcesses();
     
      Log.d("aaa", "getRunningApp");
      for (RunningAppProcessInfo pi : procInfos) 
      {
          boolean inForeground = pi.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
          if (inForeground) 
          {
        	if(!pi.processName.startsWith("com.android") && !pi.processName.startsWith("system")
        			&& !pi.processName.endsWith(".launcher") && !pi.processName.startsWith("de.vogella"))
        		Log.d("aaa", "App in focus: " + pi.processName);

          	//break;
          }
      }
      */
		//Ignore cases where the screen is off because we are in a call
	  Context context = getApplicationContext();	
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
					godark = true;
					Log.d("aaa", "Terminate Alarm: Screen is off and not in a call");
		    	}
		    }
		}

      List< ActivityManager.RunningTaskInfo > taskInfo = actvityManager.getRunningTasks(1); 
      ComponentName componentInfo = taskInfo.get(0).topActivity;
      String packageName = componentInfo.getPackageName();
      //Log.d("aaa", "Running Package Name: " + packageName);
      PackageManager pm = getApplicationContext().getPackageManager();
      ApplicationInfo appinf = pm.getApplicationInfo(packageName, 128);
      String label = (String)pm.getApplicationLabel(appinf);
      Log.d("aaa", "Running Package Name: " + packageName + " - " + label);
      
      String[] startignoreList = { "com.android.", "system", "de.vogella" };
      for (int n = 0; n < startignoreList.length; n++)
      {
    	  if( (packageName.startsWith(startignoreList[n])) )
		  {
    		  packageName = " ";
    	      //Log.d("aaa", "startignoreList" );
    		  break;
		  }
      }
      String[] endignoreList = {".launcher"};
      for (int n = 0; n < endignoreList.length; n++)
      {
    	  if( (packageName.endsWith(endignoreList[n])) )
		  {
    		  packageName = " ";
    	      //Log.d("aaa", "endignoreList" );
    		  break;
		  }
      }
      if (godark)
      {
    	  packageName = " ";
      }

      //Log.e("aaa", "Progress: logtofile");
      if(logtofile)
      {
	      //check if currentTask is valid (not "") and update it from file if needed
	      if(currentTask.equals(""))
	      {
	          try
	          {
	              BufferedReader fr = new BufferedReader(new FileReader(new File("/mnt/sdcard/currenttask.txt")));
	              String receiveString = " ";
	              
	              receiveString = fr.readLine();
	              fr.close();
	    		  if(receiveString == null )
	    		  {
	    			  currentTask = " ";
	    		  }
	    		  else
	    		  {
	    			  currentTask = receiveString;
	    		  }
	          } 
	          catch (IOException e) 
	          {
	                  Log.e("aaa", "Could not write file " + e.getMessage());
	                  currentTask = " ";
	          }    	  
	    	  //If not valid, read current task from the current file
	          /*
	    	  try
	    	  {
	    		  InputStream inputStream = openFileInput("/mnt/sdcard/currenttask.txt");
	    		  InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	    		  BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	    		  String receiveString = bufferedReader.readLine();
	    		  //If file is empty, init currentTask by placing " ". Otherwise get task;
	    		  if(receiveString.equals(""))
	    		  {
	    			  currentTask = " ";
	    		  }
	    		  else
	    		  {
	    			  currentTask = receiveString;
	    		  }
	
	    	  }
	    	  catch (IOException e)
	    	  {
	    		  //If no file, just init currentTask
	    		  currentTask = " ";
	    	  }
	    	  */
	      }
	      
	      //Log.e("aaa", "Progress: 1");
	      //No app in progress before and nothing changed (both equals " ") or if both are the same, do nothing
    	  if( currentTask.equalsIgnoreCase(packageName) )
    		  return;
    	      	  
	      //Log.e("aaa", "Progress: 2");
    	  //If current task is empty and a new app is running, update currentTask and start a new entry in the log
    	  if(currentTask.equalsIgnoreCase(" ") && !packageName.equalsIgnoreCase(" "))
    	  {
    		  currentTask = packageName;
    		  updateCurrentTask = true;
    		  openTask = true;
    	  }
    	  
	      //Log.e("aaa", "Progress: 3");
    	  //If current task is not null and no valid app is running now, close the entry and write "" in current task.
    	  if(!currentTask.equalsIgnoreCase(" ") && packageName.equalsIgnoreCase(" "))
    	  {
    		  currentTask = "";
    		  updateCurrentTask = true;
    		  closeTask = true;
    	  }
    	  
	      //Log.e("aaa", "Progress: 4");
   	  //If both are valid and not the same, it means the app switched. Close the entry, start a new one and update the current
    	  if( !currentTask.equalsIgnoreCase(packageName) && (!updateCurrentTask||!closeTask) )
    	  {
    		  currentTask = packageName;
    		  updateCurrentTask = true;
    		  openTask = true;
    		  closeTask = true;
    	  }
    	  
	      //Log.e("aaa", "Progress: 5");
    	  if(updateCurrentTask)
    	  {
	          try
	          {
	              BufferedWriter fw = new BufferedWriter(new FileWriter(new File("/mnt/sdcard/currenttask.txt"),false));
	              fw.write(currentTask);
	              fw.flush();
	              fw.close();
	          } 
	          catch (IOException e) 
	          {
	                  Log.e("aaa", "Could not write file " + e.getMessage());
	          }    	  
    	  }
    	  
    	  String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
    	  
	      //Log.e("aaa", "Progress: 6");
    	  if(closeTask)
    	  {
	          try
	          {
	              BufferedWriter fw = new BufferedWriter(new FileWriter(new File("/mnt/sdcard/task_log_file.txt"),true));
	              fw.append(";"+ mydate + "\n");
	              fw.close();
	          } 
	          catch (IOException e) 
	          {
                  Log.e("aaa", "Could not write file " + e.getMessage());
	          }    	  
    		  
    	  }

    	  if(openTask)
    	  {
	          try
	          {
	              BufferedWriter fw = new BufferedWriter(new FileWriter(new File("/mnt/sdcard/task_log_file.txt"),true));
	              fw.append( label + ";" +packageName + ";" +  mydate );
	              fw.close();
	          } 
	          catch (IOException e) 
	          {
                  Log.e("aaa", "Could not write file " + e.getMessage());
	          }    	  
    		  
    	  }
    	  
    	  //If task did not change, dont do anything
	      //If task changed, update the file
    	  /*
	      if(!packageName.equalsIgnoreCase(currentTask))
	      {
	    	  //end current log entry and open a new one with the new task
	    	  String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
	    	  
	    	  File root = Environment.getExternalStorageDirectory();
	          try{
	              BufferedWriter fw = new BufferedWriter(new FileWriter(new File("/mnt/sdcard/task_log_file.txt"),true));
	
	              if (root.canWrite()){
	                  fw.append(";"+ mydate + "\n"+ packageName);
	                  fw.close();
	                  }
	              } catch (IOException e) {
	                  Log.e("aaa", "Could not write file " + e.getMessage());
	              }    	  
	          currentTask = packageName;
	      }
	      */
      }
  }
  @Override
  public IBinder onBind(Intent arg0) {
    return mBinder;
  }

  public class MyBinder extends Binder {
    LocalWordService getService() {
      return LocalWordService.this;
    }
  }

  public List<String> getWordList() {
    return list;
  }

} 


