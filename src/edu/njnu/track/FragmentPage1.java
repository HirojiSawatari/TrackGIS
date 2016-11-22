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
	//��ʼ����ͼ
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
        //��ʼ���켣ͼ��
		gLayerPos2 = new GraphicsLayer();
        mMapView.addLayer(gLayerPos2);
        //���ù켣��ѯ��ť������
        trackButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO �Զ����ɵķ������
			}
        });
    	//���÷Ŵ���С������
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
        //���ö�λ����ͼ��
        gLayerPos = new GraphicsLayer();
        mMapView.addLayer(gLayerPos);
        locationSymbol =  new PictureMarkerSymbol(this.getResources().getDrawable(R.drawable.location));
        //��ʼ��LocationClient
        locationClient = new LocationClient(getActivity());
        //���ö�λSDK����
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);
        option.setOpenGps(true);        
        option.setCoorType("gps");       
        option.setProdName("LocationDemo"); 
        option.setScanSpan(UPDATE_TIME);
        locationClient.setLocOption(option);
        //��ʼ��λ
        if(isLocated == 1){
        	locationClient.start();
        	locationClient.requestLocation();
        }
        //ע�ᶨλ��������
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
        //������ð���������
        getActivity().findViewById(R.id.imageButton2).setOnClickListener(this);
        popMenu = new PopMenu(getActivity());
		popMenu.addItems(new String[]{"����/�رն�λ", "�켣��ѯ", "����켣��ʾ"});
		popMenu.setOnItemClickListener(this);
    }
	
	//�ڵ�ͼ�ϱ��λ��
	private void markLocation(BDLocation location) {       
        gLayerPos.removeAll();
        double locx = location.getLongitude();//����
        double locy = location.getLatitude();//γ��
        wgspoint = new Point(locx, locy);  
        //��λ������λ��
		//����ת��Ϊ���ͼ��ͬ����ϵ
        mapPoint = (Point) GeometryEngine.project(wgspoint,SpatialReference.create(4326),mMapView.getSpatialReference());
        Graphic graphic = new Graphic(mapPoint,locationSymbol);
        gLayerPos.addGraphic(graphic);  
        mMapView.centerAt(mapPoint, true);
        mMapView.setScale(100);
        mMapView.setMaxResolution(300);
    }
	
	//�ڵ�ͼ�ϱ�ǹ켣
	private void markTrend(Polyline poli) {
		gLayerPos2.removeAll();
		//����ת��Ϊ���ͼ��ͬ����ϵ
		poli = (Polyline) GeometryEngine.project(poli,SpatialReference.create(4326),mMapView.getSpatialReference());
		Graphic graphic2 = new Graphic(poli,new SimpleLineSymbol(Color.RED,5));
		gLayerPos2.addGraphic(graphic2);
		Toast.makeText(getActivity().getApplicationContext(), "�켣�������", Toast.LENGTH_SHORT).show();
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

	//���ð�������������
	public void onItemClick(int index) {
		// TODO �Զ����ɵķ������
		if(index == 0){
			if(locationClient.isStarted()){
				locationClient.stop();       
		        gLayerPos.removeAll();
		        Toast.makeText(getActivity().getApplicationContext(), "��ͣʵʱ��λ", Toast.LENGTH_SHORT).show();
		        isLocated = 0;
			}
			else{
		        locationClient.start();
		        locationClient.requestLocation();
		        Toast.makeText(getActivity().getApplicationContext(), "����ʵʱ��λ", Toast.LENGTH_SHORT).show();
		        isLocated = 1;
			}
		}
		else if(index == 1){
			new AlertDialog.Builder(getActivity())
			.setTitle("����켣��ѯ")
			.setSingleChoiceItems(new String[]{"���չ켣","���¹켣","����켣","ȫ��"},0,
        		new DialogInterface.OnClickListener() {						
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(which==0){
						poly = new Polyline();
						Time time = new Time("Asia/Hong_Kong");
						time.setToNow();
						int tday = time.monthDay;
						//����SQLite����
						db = getActivity().openOrCreateDatabase("Track_info.db", Context.MODE_PRIVATE, null);  //��SQLite���ݿ�
						Cursor c = db.query("track", null, null, null, null, null, null);  //����α�
						if(c.moveToLast()){//�ж��α��Ƿ�Ϊ��
						    while(!c.isBeforeFirst()){	//�ж�ָ������Ƿ���Ԫ�أ��������
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
					        		//�ų�ȱ��ֵ
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
	    					    	//���㻭��
	   					        	Line line = new Line();
	   					        	line.setStart(new Point(reclon1,reclat1));//��ʼ��
	   					        	line.setEnd(new Point(reclon2,reclat2));//��ֹ��
	   					        	poly.addSegment(line,true);
					        	}
						    }
						}
						c.close();
						db.close();
						//��ʾ������
				        markTrend(poly);
					}
					else if(which==1){
						poly = new Polyline();
						Time time = new Time("Asia/Hong_Kong");
						time.setToNow();
						int tmonth = (time.month) + 1;
						//����SQLite����
						db = getActivity().openOrCreateDatabase("Track_info.db", Context.MODE_PRIVATE, null);  //��SQLite���ݿ�
						Cursor c = db.query("track", null, null, null, null, null, null);  //����α�
						if(c.moveToLast()){//�ж��α��Ƿ�Ϊ��
						    while(!c.isBeforeFirst()){	//�ж�ָ������Ƿ���Ԫ��
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
					        		//�ų�ȱ��ֵ
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
	    					    	//���㻭��
	   					        	Line line = new Line();
	   					        	line.setStart(new Point(reclon1,reclat1));//��ʼ��
	   					        	line.setEnd(new Point(reclon2,reclat2));//��ֹ��
	   					        	poly.addSegment(line,true);
					        	}
						    }
						}
						c.close();
						db.close();
						//��ʾ������
				        markTrend(poly);			
					}
					else if(which==2){
						poly = new Polyline();
						Time time = new Time("Asia/Hong_Kong");
						time.setToNow();
						int tyear = time.year;
						//����SQLite����
						db = getActivity().openOrCreateDatabase("Track_info.db", Context.MODE_PRIVATE, null);  //��SQLite���ݿ�
						Cursor c = db.query("track", null, null, null, null, null, null);  //����α�
						if(c.moveToLast()){//�ж��α��Ƿ�Ϊ��
						    while(!c.isBeforeFirst()){	//�ж�ָ������Ƿ���Ԫ��
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
					        		//�ų�ȱ��ֵ
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
	    					    	//���㻭��
	   					        	Line line = new Line();
	   					        	line.setStart(new Point(reclon1,reclat1));//��ʼ��
	   					        	line.setEnd(new Point(reclon2,reclat2));//��ֹ��
	   					        	poly.addSegment(line,true);
					        	}
						    }
						}
						c.close();
						db.close();
						//��ʾ������
				        markTrend(poly);
					}
					else if(which==3){
						poly = new Polyline();
						/*
						//����SharedPreferences����
						SharedPreferences track_info = getActivity().getSharedPreferences("track", 0);
					    lCode = track_info.getInt("loccode", 0) - 1;
					    //ѭ�������Ѳ�ֵ
						for(int i = lCode; i > -1;i --){
							CodeStr1 = "" + i;
							reclat1 = track_info.getFloat("lat" + CodeStr1, 0);
    				        reclon1 = track_info.getFloat("lng" + CodeStr1, 0);
    				        CodeStr2 = "" + (i - 1);
							reclat2 = track_info.getFloat("lat" + CodeStr2, 0);
    				        reclon2 = track_info.getFloat("lng" + CodeStr2, 0);
    				        //�ų�ȱ��ֵ
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
    					    //���㻭��
   					        Line line = new Line();
   					        line.setStart(new Point(reclon1,reclat1));//��ʼ��
   					        line.setEnd(new Point(reclon2,reclat2));//��ֹ��
   					        poly.addSegment(line,true);
						}
						*/
						//����SQLite����
						db = getActivity().openOrCreateDatabase("Track_info.db", Context.MODE_PRIVATE, null);  //��SQLite���ݿ�
						Cursor c = db.query("track", null, null, null, null, null, null);  //����α�
						if(c.moveToLast()){//�ж��α��Ƿ�Ϊ��
						    while(!c.isBeforeFirst()){	//�ж�ָ������Ƿ���Ԫ��
					        	reclat1 = c.getFloat(c.getColumnIndex("lat"));
					        	reclon1 = c.getFloat(c.getColumnIndex("lon"));
					        	c.moveToPrevious();
					        	if(!c.isBeforeFirst()){
					        		reclat2 = c.getFloat(c.getColumnIndex("lat"));
					        		reclon2 = c.getFloat(c.getColumnIndex("lon"));
					        		//�ų�ȱ��ֵ
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
	    					    	//���㻭��
	   					        	Line line = new Line();
	   					        	line.setStart(new Point(reclon1,reclat1));//��ʼ��
	   					        	line.setEnd(new Point(reclon2,reclat2));//��ֹ��
	   					        	poly.addSegment(line,true);
					        	}
						    }
						}
						c.close();
						db.close();
						//��ʾ������
				        markTrend(poly);
					}
				}
			})
			.setNegativeButton("ȡ��", null)
    		.show();
		}
		else if(index == 2){
			gLayerPos2.removeAll();
			Toast.makeText(getActivity().getApplicationContext(), "�رչ켣��ʾ", Toast.LENGTH_SHORT).show();
		}
	}
	//������������Ӧ
	public void onClick(View arg0) {
		// TODO �Զ����ɵķ������
		if(arg0.getId() == R.id.imageButton2){
			popMenu.showAsDropDown(arg0);
		}
	}	
}
