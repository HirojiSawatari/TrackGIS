package edu.njnu.track;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;

import edu.njnu.track.etc.Configration;
import edu.njnu.track.etc.GetWeather;
import edu.njnu.track.etc.MyLocation;

public class StartService extends Service implements OnGetGeoCoderResultListener {

	SharedPreferences set_info;
	SQLiteDatabase db;
	public boolean serv_b;
	public int spaceTime;
	public String sCode;
	//public static final String TAG = "MyService";
	private int lCode;
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;
	private boolean isStop = false;
	private static int delay = 1000; // 1s
	private static int period = 1000; // 1s
	//private static String strUrl = "http://192.168.1.113:9191/sf/";
	private LocationClient locationClient = null;
	public BDLocationListener myListener = new MyLocationListener();

    MyLocation myLocation;	//获取经纬度完成后由这个获取地点称
    String strlocation = "";	//地点名称
    String weather="";	//天气字段
    GetWeather gw;	//根据地名获取天气
    
    GeoCoder mSearch = null;	//搜索模块
    String poiname = "处理中";
    
    //为了地址监听器最后执行时有值传入，设为公用
    double lat;
    double lng;
    Time time;
    
	@Override
	public void onCreate() {
		super.onCreate();
		
		SDKInitializer.initialize(getApplicationContext());	//百度地图sdk初始化
		
		myLocation = new MyLocation();
		gw=new GetWeather();
		
		//打开SQLite数据库
		db = openOrCreateDatabase("Track_info.db", Context.MODE_PRIVATE, null);
		//若不存在track表新建该表
		db.execSQL("CREATE TABLE IF NOT EXISTS track" +  
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, lat VARCHAR, lon VARCHAR, " +
                "year INTEGER, month INTEGER, day INTEGER, hour INTEGER, minute INTEGER, second INTEGER, weather TEXT, address TEXT)");  
		
		locationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
		locationClient.registerLocationListener(myListener); // 注册监听函数
		//设置定位SDK参数
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);
        option.setOpenGps(true);        
        option.setCoorType("gps");       
        option.setProdName("LocationDemo");
		locationClient.setLocOption(option);
		
		//获取轨迹结点计数值
		SharedPreferences track_info = getSharedPreferences("track", 0);
		lCode = track_info.getInt("loccode", 0);
		if(lCode < 0){
			lCode = 0;
			SharedPreferences.Editor editor = track_info.edit();
			editor.putInt("loccode", lCode);
			editor.commit();
		}
		
		//通知栏
		Boolean isServ = checkOpen();
		if(isServ != false){
			CharSequence name="TrackGIS";
			CharSequence name2="行为轨迹记录中";
			Notification notification = new Notification(R.drawable.icon_start_service, name, System.currentTimeMillis());
			Intent nintent = new Intent(this, MainTabActivity.class);
			notification.flags = Notification.FLAG_ONGOING_EVENT; 
			PendingIntent contextIntent = PendingIntent.getActivity(this, 0, nintent, 0);
			notification.setLatestEventInfo(getApplicationContext(), name, name2, contextIntent);
			//notificationManager.notify(R.string.app_name, notification);
			//保证服务在前台
			startForeground(1, notification);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 触发定时器
		if (!isStop) {
			//Log.i("K", "开始服务");
			startTimer();
		}
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {

		//Log.d("Res", "onDestroy");
		locationClient.stop();
		db.close();
		// 停止定时器
		if (isStop) {
			Log.i("T", "服务停止");
			stopTimer();
		}
		//停止前台服务
		stopForeground(true);
		//广播进程被Kill
		Intent des_intent = new Intent("edu.njnu.track.destroy");  
	    sendBroadcast(des_intent);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 定时器 相应时间段执行一次（默认15分钟）
	 */
	private void startTimer() {
		//定时器
		if (mTimer == null) {
			mTimer = new Timer();
		}
		//Log.i(TAG, "count: " + String.valueOf(count++));
		isStop = true;
		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					Boolean isServ = checkOpen();
					if(isServ != false){
						//Log.i(TAG, "count: " + String.valueOf(count++));
						do {
							try {
								locationClient.start();
								if (locationClient != null && locationClient.isStarted()){
									locationClient.requestLocation();
								}
								else{
									spaceTime = getsCode();
									Thread.sleep(1000*60*spaceTime);//暂停相应时间								
								}
							} catch (InterruptedException e) {
							}
						} while (isStop);
					}
				}
			};
		}

		if (mTimer != null && mTimerTask != null){
			mTimer.schedule(mTimerTask, delay, period);
		}
	}

	//获取设置时间
	public int getsCode() {
		set_info = PreferenceManager.getDefaultSharedPreferences(this);
		sCode = set_info.getString("space", "");
		if(sCode == ""){	//默认设置
			spaceTime = 15;
			SharedPreferences.Editor ed = set_info.edit();
			ed.putString("space", "15");
			ed.commit();
		}
		else{
			spaceTime = Integer.parseInt(sCode);
		}
		return spaceTime;
	}
	
	private void stopTimer() {

		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
		isStop = false;

	}

	/*
	 * 获取定位经纬度 信息
	 */
	@SuppressLint("SimpleDateFormat")
	private void getLocationInfo(BDLocation location) {
		if (location != null) {
			lat = location.getLatitude();
			lng = location.getLongitude();
			
			//gps坐标转化为百度坐标后进行反地理编码
			LatLng sourceLatLng = new LatLng(location.getLatitude(), location.getLongitude());
			CoordinateConverter converter  = new CoordinateConverter(); 
			converter.from(CoordType.COMMON);
			converter.coord(sourceLatLng); 
			LatLng desLatLng = converter.convert();

			//实例化GeoCoder对象
			mSearch = GeoCoder.newInstance();
			//注册检索监听器
			mSearch.setOnGetGeoCodeResultListener(this);
			// 反Geo搜索
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(desLatLng));			
    		
		} else {
			
		}
	}

	/*
	 * 向后台Post定位数据
	 */
	public void PostData(double lat, double lng, Time time, String weather, String poiname) {	//参数全部为公用值
		//保存至本地SQLite数据库
		db.execSQL("INSERT INTO track VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 
				new Object[]{lat, lng, time.year, (time.month) + 1, time.monthDay, 
				time.hour, time.minute, time.second, weather, poiname});
	}
	
	//获取天气数据
	public String getWeather(String strlocation) {
		try {
			weather=gw.getWeather(new Configration()
			.EcodingGB2312(strlocation.substring(0, strlocation.length()-1)));
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return weather;
	}

	//获取定位数据
	public class MyLocationListener implements BDLocationListener {
		public void onReceiveLocation(BDLocation location) {
			if (location == null){
				return;	
			}
			locationClient.stop();
			getLocationInfo(location);
		}
		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
	
	public boolean checkOpen() {
		set_info = PreferenceManager.getDefaultSharedPreferences(this);
		serv_b = set_info.getBoolean("notif", false);
		return serv_b;
	}

	public void onGetGeoCodeResult(GeoCodeResult arg0) {
		// TODO 自动生成的方法存根
		
	}

	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult geoResult) {//反地理编码监听
		// TODO 自动生成的方法存根
		if (geoResult == null
				|| geoResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
			poiname = "未知地点";
		}

		if (geoResult.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回
			poiname = geoResult.getPoiList().get(0).name;
		}
		//获取地点天气
		strlocation =  myLocation.getAddress(lat + "", lng + "");
		weather = getWeather(strlocation);

		//获取系统时间
		Time time = new Time("Asia/Hong_Kong");
		time.setToNow();
		
		PostData(lat, lng, time, weather, poiname);
	}
}


