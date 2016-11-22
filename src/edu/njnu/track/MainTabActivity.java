package edu.njnu.track;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

/**
 * @since 1.0
 * @version 1.0
 * @author He Tao of NNU
 */

public class MainTabActivity extends FragmentActivity{	
	private long exitTime = 0;
	private static final int ITEM_1=Menu.FIRST;
	//����FragmentTabHost����
	private FragmentTabHost mTabHost;
	//����һ������
	private LayoutInflater layoutInflater;
	//�������������Fragment����
	private Class fragmentArray[] = {FragmentPage1.class,FragmentPage2.class,FragmentPage3.class,FragmentPage4.class,FragmentPage5.class};	
	//������������Ű�ťͼƬ
	private int mImageViewArray[] = {R.drawable.tab_home_btn,R.drawable.tab_message_btn,R.drawable.tab_selfinfo_btn,
									 R.drawable.tab_square_btn,R.drawable.tab_more_btn};
	//Tabѡ�������
	private String mTextviewArray[] = {"��ͼ", "�ճ�", "����", "�㳡", "����"};
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //������̨��λ����
		Intent startIntent = new Intent(MainTabActivity.this, StartService.class);  
        startService(startIntent);  
        
        setContentView(R.layout.main_tab_layout); 
        initView();
    }
	 
	/**
	 * ��ʼ�����
	 */
	private void initView(){
		//ʵ�������ֶ���
		layoutInflater = LayoutInflater.from(this);
		//ʵ����TabHost���󣬵õ�TabHost
		mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		//�õ�fragment�ĸ���
		int count = fragmentArray.length;	
				
		for(int i = 0; i < count; i++){	
			//Ϊÿһ��Tab��ť����ͼ�ꡢ���ֺ�����
			TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
			//��Tab��ť��ӽ�Tabѡ���
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
			//����Tab��ť�ı���
			mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
		}
	}
				
	/**
	 * ��Tab��ť����ͼ�������
	 */
	private View getTabItemView(int index){
		View view = layoutInflater.inflate(R.layout.tab_item_view, null);
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageViewArray[index]);
		TextView textView = (TextView) view.findViewById(R.id.textview);		
		textView.setText(mTextviewArray[index]);
		textView.setTextColor(this.getResources().getColor(R.color.bottom_text));
		return view;
	}
	
	//�˵�����ʽ
	public boolean onPrepareOptionsMenu(Menu menu){
	    menu.clear();
	    menu.add(0,ITEM_1,0,"����").setIcon(android.R.drawable.ic_menu_info_details);
	    return super.onCreateOptionsMenu(menu);
	}
	//�˵�������
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case ITEM_1:
			new AlertDialog.Builder(MainTabActivity.this)
			.setTitle("����")
			.setMessage("Copyright Nanjing Normal University All right reserved.")
			.setPositiveButton("ȷ��", null)
			.show();
			break;
		}	
		return true;
	}
	
	
	//˫�����ؼ��˳�
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
			if((System.currentTimeMillis()-exitTime) > 2000){	            	
	            Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();      
	            exitTime = System.currentTimeMillis();   
	        } else {
	            finish();
	            System.exit(0);
	        }
	        return true;   
	    }
	    return super.onKeyDown(keyCode, event);
	}
}
