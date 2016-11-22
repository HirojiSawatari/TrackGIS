package edu.njnu.track;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SystemSet extends PreferenceActivity {

	SharedPreferences set_info;
	
	//界面样式
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.system_set);
		//获取preferences
		set_info = PreferenceManager.getDefaultSharedPreferences(this);

		Preference noti = findPreference("notif");
		noti.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			public boolean onPreferenceClick(Preference preference) { 
		    	Boolean noti_code = set_info.getBoolean("notif", false);
		    	if(noti_code == false) {	//关闭后台定位
		    		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		    		notificationManager.cancel(0);
		    		Intent intentService = new Intent(SystemSet.this, StartService.class);
		    	    stopService(intentService);
		    	}
		    	else{	//开启后台定位
		    		Intent intentService = new Intent(SystemSet.this, StartService.class);
		    	    startService(intentService);
		    	}
		        return true;  
		    }  
		}); 
	}
}
