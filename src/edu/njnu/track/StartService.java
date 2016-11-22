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

    MyLocation myLocation;	//��ȡ��γ����ɺ��������ȡ�ص��
    String strlocation = "";	//�ص�����
    String weather="";	//�����ֶ�
    GetWeather gw;	//���ݵ�����ȡ����
    
    GeoCoder mSearch = null;	//����ģ��
    String poiname = "������";
    
    //Ϊ�˵�ַ���������ִ��ʱ��ֵ���룬��Ϊ����
    double lat;
    double lng;
    Time time;
    
	@Override
	public void onCreate() {
		super.onCreate();
		
		SDKInitializer.initialize(getApplicationContext());	//�ٶȵ�ͼsdk��ʼ��
		
		myLocation = new MyLocation();
		gw=new GetWeather();
		
		//��SQLite���ݿ�
		db = openOrCreateDatabase("Track_info.db", Context.MODE_PRIVATE, null);
		//��������track���½��ñ�
		db.execSQL("CREATE TABLE IF NOT EXISTS track" +  
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, lat VARCHAR, lon VARCHAR, " +
                "year INTEGER, month INTEGER, day INTEGER, hour INTEGER, minute INTEGER, second INTEGER, weather TEXT, address TEXT)");  
		
		locationClient = new LocationClient(getApplicationContext()); // ����LocationClient��
		locationClient.registerLocationListener(myListener); // ע���������
		//���ö�λSDK����
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);
        option.setOpenGps(true);        
        option.setCoorType("gps");       
        option.setProdName("LocationDemo");
		locationClient.setLocOption(option);
		
		//��ȡ�켣������ֵ
		SharedPreferences track_info = getSharedPreferences("track", 0);
		lCode = track_info.getInt("loccode", 0);
		if(lCode < 0){
			lCode = 0;
			SharedPreferences.Editor editor = track_info.edit();
			editor.putInt("loccode", lCode);
			editor.commit();
		}
		
		//֪ͨ��
		Boolean isServ = checkOpen();
		if(isServ != false){
			CharSequence name="TrackGIS";
			CharSequence name2="��Ϊ�켣��¼��";
			Notification notification = new Notification(R.drawable.icon_start_service, name, System.currentTimeMillis());
			Intent nintent = new Intent(this, MainTabActivity.class);
			notification.flags = Notification.FLAG_ONGOING_EVENT; 
			PendingIntent contextIntent = PendingIntent.getActivity(this, 0, nintent, 0);
			notification.setLatestEventInfo(getApplicationContext(), name, name2, contextIntent);
			//notificationManager.notify(R.string.app_name, notification);
			//��֤������ǰ̨
			startForeground(1, notification);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// ������ʱ��
		if (!isStop) {
			//Log.i("K", "��ʼ����");
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
		// ֹͣ��ʱ��
		if (isStop) {
			Log.i("T", "����ֹͣ");
			stopTimer();
		}
		//ֹͣǰ̨����
		stopForeground(true);
		//�㲥���̱�Kill
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
	 * ��ʱ�� ��Ӧʱ���ִ��һ�Σ�Ĭ��15���ӣ�
	 */
	private void startTimer() {
		//��ʱ��
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
									Thread.sleep(1000*60*spaceTime);//��ͣ��Ӧʱ��								
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

	//��ȡ����ʱ��
	public int getsCode() {
		set_info = PreferenceManager.getDefaultSharedPreferences(this);
		sCode = set_info.getString("space", "");
		if(sCode == ""){	//Ĭ������
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
	 * ��ȡ��λ��γ�� ��Ϣ
	 */
	@SuppressLint("SimpleDateFormat")
	private void getLocationInfo(BDLocation location) {
		if (location != null) {
			lat = location.getLatitude();
			lng = location.getLongitude();
			
			//gps����ת��Ϊ�ٶ��������з��������
			LatLng sourceLatLng = new LatLng(location.getLatitude(), location.getLongitude());
			CoordinateConverter converter  = new CoordinateConverter(); 
			converter.from(CoordType.COMMON);
			converter.coord(sourceLatLng); 
			LatLng desLatLng = converter.convert();

			//ʵ����GeoCoder����
			mSearch = GeoCoder.newInstance();
			//ע�����������
			mSearch.setOnGetGeoCodeResultListener(this);
			// ��Geo����
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(desLatLng));			
    		
		} else {
			
		}
	}

	/*
	 * ���̨Post��λ����
	 */
	public void PostData(double lat, double lng, Time time, String weather, String poiname) {	//����ȫ��Ϊ����ֵ
		//����������SQLite���ݿ�
		db.execSQL("INSERT INTO track VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 
				new Object[]{lat, lng, time.year, (time.month) + 1, time.monthDay, 
				time.hour, time.minute, time.second, weather, poiname});
	}
	
	//��ȡ��������
	public String getWeather(String strlocation) {
		try {
			weather=gw.getWeather(new Configration()
			.EcodingGB2312(strlocation.substring(0, strlocation.length()-1)));
		} catch (UnsupportedEncodingException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		return weather;
	}

	//��ȡ��λ����
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
		// TODO �Զ����ɵķ������
		
	}

	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult geoResult) {//������������
		// TODO �Զ����ɵķ������
		if (geoResult == null
				|| geoResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// û���ҵ��������
			poiname = "δ֪�ص�";
		}

		if (geoResult.error == SearchResult.ERRORNO.NO_ERROR) {// ���������������
			poiname = geoResult.getPoiList().get(0).name;
		}
		//��ȡ�ص�����
		strlocation =  myLocation.getAddress(lat + "", lng + "");
		weather = getWeather(strlocation);

		//��ȡϵͳʱ��
		Time time = new Time("Asia/Hong_Kong");
		time.setToNow();
		
		PostData(lat, lng, time, weather, poiname);
	}
}


