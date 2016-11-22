package edu.njnu.track;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleLineSymbol;

import edu.njnu.track.etc.PopMenu.OnItemClickListener;
import edu.njnu.track.etc.PopMenu;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ZoomControls;

public class FragmentPage1 extends Fragment implements OnClickListener, OnItemClickListener {
	
	SQLiteDatabase db;
	
	private PopMenu popMenu;
	public int mapCode; 
	public int isLocated = 1;
	
	MapView mMapView ;
	private ZoomControls zoomcontrols; 
	private ImageButton trackButton;

    private LocationClient locationClient = null;
    private static int UPDATE_TIME = 30000;
    
	ArcGISTiledMapServiceLayer tileLayer;
    Point point;
    Point wgspoint;
    GraphicsLayer gLayerPos;
    PictureMarkerSymbol locationSymbol;
    Point mapPoint;
	
    private int lCode;
    private String CodeStr1; 
    private float reclat1;
    private float reclon1;
    private String CodeStr2; 
    private float reclat2;
    private float reclon2;
    GraphicsLayer gLayerPos2;
    Polyline poly;
    
	@Override
	//初始化地图
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_1,container, false);
		mMapView = (MapView)view.findViewById(R.id.map);
		tileLayer = new ArcGISTiledMapServiceLayer(
                "http://cache1.arcgisonline.cn/ArcGIS/rest/services/ChinaOnlineCommunity/MapServer");
        mMapView.addLayer(tileLayer);
        mapCode = 0;
		return view;		
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {  
        super.onActivityCreated(savedInstanceState); 
        trackButton = (ImageButton)getActivity().findViewById(R.id.imageButton1);
        //初始化轨迹图层
		gLayerPos2 = new GraphicsLayer();
        mMapView.addLayer(gLayerPos2);
        //设置轨迹查询按钮（分享）
        trackButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
			}
        });
    	//设置放大缩小键功能
        zoomcontrols = (ZoomControls)getActivity().findViewById(R.id.zoomControls1);
        zoomcontrols.setIsZoomInEnabled(true);  
        zoomcontrols.setIsZoomOutEnabled(true); 
        zoomcontrols.setOnZoomInClickListener(new OnClickListener(){
        	public void onClick(View v){
        		mMapView.zoomin();
        	}
    	});
        zoomcontrols.setOnZoomOutClickListener(new OnClickListener(){
        	public void onClick(View v){
        		mMapView.zoomout();
        	}
    	});
        //设置定位符号图层
        gLayerPos = new GraphicsLayer();
        mMapView.addLayer(gLayerPos);
        locationSymbol =  new PictureMarkerSymbol(this.getResources().getDrawable(R.drawable.location));
        //初始化LocationClient
        locationClient = new LocationClient(getActivity());
        //设置定位SDK参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);
        option.setOpenGps(true);        
        option.setCoorType("gps");       
        option.setProdName("LocationDemo"); 
        option.setScanSpan(UPDATE_TIME);
        locationClient.setLocOption(option);
        //开始定位
        if(isLocated == 1){
        	locationClient.start();
        	locationClient.requestLocation();
        }
        //注册定位监听函数
        locationClient.registerLocationListener(new BDLocationListener() {
            public void onReceiveLocation(BDLocation location) {
            	// TODO Auto-generated method stub
            	if (location == null) {
            		return;
            	}
            	if (isLocated == 1){
            		markLocation(location);
            	}
            }
        });
        //添加设置按键下拉栏
        getActivity().findViewById(R.id.imageButton2).setOnClickListener(this);
        popMenu = new PopMenu(getActivity());
		popMenu.addItems(new String[]{"开启/关闭定位", "轨迹查询", "清除轨迹显示"});
		popMenu.setOnItemClickListener(this);
    }
	
	//在地图上标记位置
	private void markLocation(BDLocation location) {       
        gLayerPos.removeAll();
        double locx = location.getLongitude();//经度
        double locy = location.getLatitude();//纬度
        wgspoint = new Point(locx, locy);  
        //定位到所在位置
		//将点转化为与底图相同坐标系
        mapPoint = (Point) GeometryEngine.project(wgspoint,SpatialReference.create(4326),mMapView.getSpatialReference());
        Graphic graphic = new Graphic(mapPoint,locationSymbol);
        gLayerPos.addGraphic(graphic);  
        mMapView.centerAt(mapPoint, true);
        mMapView.setScale(100);
        mMapView.setMaxResolution(300);
    }
	
	//在地图上标记轨迹
	private void markTrend(Polyline poli) {
		gLayerPos2.removeAll();
		//将线转化为与底图相同坐标系
		poli = (Polyline) GeometryEngine.project(poli,SpatialReference.create(4326),mMapView.getSpatialReference());
		Graphic graphic2 = new Graphic(poli,new SimpleLineSymbol(Color.RED,5));
		gLayerPos2.addGraphic(graphic2);
		Toast.makeText(getActivity().getApplicationContext(), "轨迹计算完成", Toast.LENGTH_SHORT).show();
	}
	
	public void onDestroy() { 
		super.onDestroy();
		if (locationClient != null && locationClient.isStarted()) {
            locationClient.stop();
            locationClient = null;
        }
	}
	public void onPause() {
		super.onPause();
		mMapView.pause();
	}

	public void onResume() {
		super.onResume(); 
		mMapView.unpause();
	}

	//设置按键下拉栏功能
	public void onItemClick(int index) {
		// TODO 自动生成的方法存根
		if(index == 0){
			if(locationClient.isStarted()){
				locationClient.stop();       
		        gLayerPos.removeAll();
		        Toast.makeText(getActivity().getApplicationContext(), "暂停实时定位", Toast.LENGTH_SHORT).show();
		        isLocated = 0;
			}
			else{
		        locationClient.start();
		        locationClient.requestLocation();
		        Toast.makeText(getActivity().getApplicationContext(), "开启实时定位", Toast.LENGTH_SHORT).show();
		        isLocated = 1;
			}
		}
		else if(index == 1){
			new AlertDialog.Builder(getActivity())
			.setTitle("最近轨迹查询")
			.setSingleChoiceItems(new String[]{"本日轨迹","本月轨迹","本年轨迹","全部"},0,
        		new DialogInterface.OnClickListener() {						
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(which==0){
						poly = new Polyline();
						Time time = new Time("Asia/Hong_Kong");
						time.setToNow();
						int tday = time.monthDay;
						//利用SQLite画线
						db = getActivity().openOrCreateDatabase("Track_info.db", Context.MODE_PRIVATE, null);  //打开SQLite数据库
						Cursor c = db.query("track", null, null, null, null, null, null);  //获得游标
						if(c.moveToLast()){//判断游标是否为空
						    while(!c.isBeforeFirst()){	//判断指针后面是否有元素（不是最后）
					        	reclat1 = c.getFloat(c.getColumnIndex("lat"));
					        	reclon1 = c.getFloat(c.getColumnIndex("lon"));
					        	c.moveToPrevious();
					        	if(!c.isBeforeFirst()){
					        		int pday = c.getInt(c.getColumnIndex("day"));
					        		if (tday != pday){
					        			break;
					        		}
					        		reclat2 = c.getFloat(c.getColumnIndex("lat"));
					        		reclon2 = c.getFloat(c.getColumnIndex("lon"));
					        		//排除缺测值
	    				        	if(reclat1 < 3 || reclat1 > 54){
	    				        		continue;
	    				        	}
	    				        	if(reclat2 < 3 || reclat2 > 54){
	    				        		continue;
	    				        	}
	    				        	if(reclon1 < 73 || reclon1 > 136){
	    				        		continue;
	    				        	}
	    				        	if(reclon2 < 73 || reclon2 > 136){
	    				        		continue;
	    				        	}
	    					    	//两点画线
	   					        	Line line = new Line();
	   					        	line.setStart(new Point(reclon1,reclat1));//起始点
	   					        	line.setEnd(new Point(reclon2,reclat2));//终止点
	   					        	poly.addSegment(line,true);
					        	}
						    }
						}
						c.close();
						db.close();
						//显示所画线
				        markTrend(poly);
					}
					else if(which==1){
						poly = new Polyline();
						Time time = new Time("Asia/Hong_Kong");
						time.setToNow();
						int tmonth = (time.month) + 1;
						//利用SQLite画线
						db = getActivity().openOrCreateDatabase("Track_info.db", Context.MODE_PRIVATE, null);  //打开SQLite数据库
						Cursor c = db.query("track", null, null, null, null, null, null);  //获得游标
						if(c.moveToLast()){//判断游标是否为空
						    while(!c.isBeforeFirst()){	//判断指针后面是否有元素
					        	reclat1 = c.getFloat(c.getColumnIndex("lat"));
					        	reclon1 = c.getFloat(c.getColumnIndex("lon"));
					        	c.moveToPrevious();
					        	if(!c.isBeforeFirst()){
					        		int pmonth = c.getInt(c.getColumnIndex("month"));
					        		if (tmonth != pmonth){
					        			break;
					        		}
					        		reclat2 = c.getFloat(c.getColumnIndex("lat"));
					        		reclon2 = c.getFloat(c.getColumnIndex("lon"));
					        		//排除缺测值
	    				        	if(reclat1 < 3 || reclat1 > 54){
	    				        		continue;
	    				        	}
	    				        	if(reclat2 < 3 || reclat2 > 54){
	    				        		continue;
	    				        	}
	    				        	if(reclon1 < 73 || reclon1 > 136){
	    				        		continue;
	    				        	}
	    				        	if(reclon2 < 73 || reclon2 > 136){
	    				        		continue;
	    				        	}
	    					    	//两点画线
	   					        	Line line = new Line();
	   					        	line.setStart(new Point(reclon1,reclat1));//起始点
	   					        	line.setEnd(new Point(reclon2,reclat2));//终止点
	   					        	poly.addSegment(line,true);
					        	}
						    }
						}
						c.close();
						db.close();
						//显示所画线
				        markTrend(poly);			
					}
					else if(which==2){
						poly = new Polyline();
						Time time = new Time("Asia/Hong_Kong");
						time.setToNow();
						int tyear = time.year;
						//利用SQLite画线
						db = getActivity().openOrCreateDatabase("Track_info.db", Context.MODE_PRIVATE, null);  //打开SQLite数据库
						Cursor c = db.query("track", null, null, null, null, null, null);  //获得游标
						if(c.moveToLast()){//判断游标是否为空
						    while(!c.isBeforeFirst()){	//判断指针后面是否有元素
					        	reclat1 = c.getFloat(c.getColumnIndex("lat"));
					        	reclon1 = c.getFloat(c.getColumnIndex("lon"));
					        	c.moveToPrevious();
					        	if(!c.isBeforeFirst()){
					        		int pyear = c.getInt(c.getColumnIndex("year"));
					        		if (tyear != pyear){
					        			break;
					        		}
					        		reclat2 = c.getFloat(c.getColumnIndex("lat"));
					        		reclon2 = c.getFloat(c.getColumnIndex("lon"));
					        		//排除缺测值
	    				        	if(reclat1 < 3 || reclat1 > 54){
	    				        		continue;
	    				        	}
	    				        	if(reclat2 < 3 || reclat2 > 54){
	    				        		continue;
	    				        	}
	    				        	if(reclon1 < 73 || reclon1 > 136){
	    				        		continue;
	    				        	}
	    				        	if(reclon2 < 73 || reclon2 > 136){
	    				        		continue;
	    				        	}
	    					    	//两点画线
	   					        	Line line = new Line();
	   					        	line.setStart(new Point(reclon1,reclat1));//起始点
	   					        	line.setEnd(new Point(reclon2,reclat2));//终止点
	   					        	poly.addSegment(line,true);
					        	}
						    }
						}
						c.close();
						db.close();
						//显示所画线
				        markTrend(poly);
					}
					else if(which==3){
						poly = new Polyline();
						/*
						//利用SharedPreferences画线
						SharedPreferences track_info = getActivity().getSharedPreferences("track", 0);
					    lCode = track_info.getInt("loccode", 0) - 1;
					    //循环遍历已测值
						for(int i = lCode; i > -1;i --){
							CodeStr1 = "" + i;
							reclat1 = track_info.getFloat("lat" + CodeStr1, 0);
    				        reclon1 = track_info.getFloat("lng" + CodeStr1, 0);
    				        CodeStr2 = "" + (i - 1);
							reclat2 = track_info.getFloat("lat" + CodeStr2, 0);
    				        reclon2 = track_info.getFloat("lng" + CodeStr2, 0);
    				        //排除缺测值
    				        if(reclat1 < 3 || reclat1 > 54){
    				        	continue;
    				        }
    				        if(reclat2 < 3 || reclat2 > 54){
    				        	continue;
    				        }
    				        if(reclon1 < 73 || reclon1 > 136){
    				        	continue;
    				        }
    				        if(reclon2 < 73 || reclon2 > 136){
    				        	continue;
    				        }
    					    //两点画线
   					        Line line = new Line();
   					        line.setStart(new Point(reclon1,reclat1));//起始点
   					        line.setEnd(new Point(reclon2,reclat2));//终止点
   					        poly.addSegment(line,true);
						}
						*/
						//利用SQLite画线
						db = getActivity().openOrCreateDatabase("Track_info.db", Context.MODE_PRIVATE, null);  //打开SQLite数据库
						Cursor c = db.query("track", null, null, null, null, null, null);  //获得游标
						if(c.moveToLast()){//判断游标是否为空
						    while(!c.isBeforeFirst()){	//判断指针后面是否有元素
					        	reclat1 = c.getFloat(c.getColumnIndex("lat"));
					        	reclon1 = c.getFloat(c.getColumnIndex("lon"));
					        	c.moveToPrevious();
					        	if(!c.isBeforeFirst()){
					        		reclat2 = c.getFloat(c.getColumnIndex("lat"));
					        		reclon2 = c.getFloat(c.getColumnIndex("lon"));
					        		//排除缺测值
	    				        	if(reclat1 < 3 || reclat1 > 54){
	    				        		continue;
	    				        	}
	    				        	if(reclat2 < 3 || reclat2 > 54){
	    				        		continue;
	    				        	}
	    				        	if(reclon1 < 73 || reclon1 > 136){
	    				        		continue;
	    				        	}
	    				        	if(reclon2 < 73 || reclon2 > 136){
	    				        		continue;
	    				        	}
	    					    	//两点画线
	   					        	Line line = new Line();
	   					        	line.setStart(new Point(reclon1,reclat1));//起始点
	   					        	line.setEnd(new Point(reclon2,reclat2));//终止点
	   					        	poly.addSegment(line,true);
					        	}
						    }
						}
						c.close();
						db.close();
						//显示所画线
				        markTrend(poly);
					}
				}
			})
			.setNegativeButton("取消", null)
    		.show();
		}
		else if(index == 2){
			gLayerPos2.removeAll();
			Toast.makeText(getActivity().getApplicationContext(), "关闭轨迹显示", Toast.LENGTH_SHORT).show();
		}
	}
	//按键下拉栏响应
	public void onClick(View arg0) {
		// TODO 自动生成的方法存根
		if(arg0.getId() == R.id.imageButton2){
			popMenu.showAsDropDown(arg0);
		}
	}	
}
