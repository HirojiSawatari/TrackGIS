package edu.njnu.track;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
	
	SharedPreferences set_info;
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {		// 监听开机广播
			//判断是否需要自启动服务	
			set_info = PreferenceManager.getDefaultSharedPreferences(context);
			Boolean start_code = set_info.getBoolean("auto", false);
			if(start_code != false){
				SharedPreferences.Editor editor = set_info.edit();
				editor.putBoolean("notif", true);
				editor.commit();
				Intent intent2 = new Intent(context, StartService.class);
				intent2.setClass(context, StartService.class);
				context.startService(intent2);
			}
		}
		if(intent.getAction().equals("edu.njnu.track.destroy")){	//监听服务Kill广播
			//重新启动后台服务
			set_info = PreferenceManager.getDefaultSharedPreferences(context);
			Boolean start_code = set_info.getBoolean("auto", false);
			if(start_code != false){
				Intent intent2 = new Intent(context, StartService.class);
				intent2.setClass(context, StartService.class);
				context.startService(intent2);
			}
		}
	}
}