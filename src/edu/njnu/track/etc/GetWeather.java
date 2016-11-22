package edu.njnu.track.etc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class GetWeather{

	/**
	 * 获取天气tips
	 * @return 天气和tips字符串
	 */
	public String getWeather(String city) {
		 String url = "http://php.weather.sina.com.cn/xml.php?city="+city+"&password=DJOYnieT8234jlsK&day=0";
		 HttpResponse httpResponse = null;
		 StringBuffer result=null;
		 DefaultHttpClient client=null;
		 try {
			 client=new DefaultHttpClient();
			 client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
			 client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 15000);
			 HttpGet httpGet = new HttpGet(url);
			 httpResponse = client.execute(httpGet);
			 if (httpResponse.getStatusLine().getStatusCode() == 200){
				 result=new StringBuffer("");
				 String s=EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
				InputStream   in_withcode   =   new   ByteArrayInputStream(s.getBytes("UTF-8"));
				Weather w=new Configration().readInfo(in_withcode);
				if(w!=null){	//只要天气状况
					 //result.append(w.getCity());//城市
					 result.append(w.getStatus1());//天气状况
					 //result.append("转"+w.getStatus2()+" ");//天气状况
					 //result.append(w.getDirection1()+w.getPower1()+"级~");//风
					 //result.append(w.getDirection2()+w.getPower2()+"级 ");//风
					 //result.append("温度:"+w.getTemperature2()+"~"+w.getTemperature1()+"℃");//低温
					 //result.append(" 户外运动:"+w.getYd_s());
				}
			 }else{
				 Log.i("note", "数据请求异常！");
				 result=null;
			 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(result!=null)
			return result.toString();
		else return null;
	}
	

}
