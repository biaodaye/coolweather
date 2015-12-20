package com.example.coolweather;

import com.example.coolweather.util.HttpCallBackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity {
	//�ؼ�
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_layout);
		//��ʼ���ؼ�
		weatherInfoLayout=(LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText=(TextView) findViewById(R.id.city_name);
		publishText=(TextView) findViewById(R.id.publish_text);
		weatherDespText=(TextView) findViewById(R.id.weather_desp);
		temp1Text=(TextView) findViewById(R.id.temp1);
		temp2Text=(TextView) findViewById(R.id.temp2);
		currentDateText=(TextView) findViewById(R.id.current_date);
		//��ȡcountycode
		String countyCode=getIntent().getStringExtra("county_code");
		//�ж�countycode�Ƿ���ڣ���ʼ��ѯ
		if (!TextUtils.isEmpty(countyCode)) {
			publishText.setText("ͬ���С�����");
			//���ؼ����ò��ɼ�
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			//��ʼ��ѯ
			queryWeatherCode(countyCode);
			
		}else {
			
		}
	}
	//����countycode��ѯweathercode
	private void queryWeatherCode(String countyCode){
		String address="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		queryFromServer(address, "countyCode");
	}
	//����weathercode��ѯ����
	private void queryWeatherInfo(String weatherCode){
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFromServer(address, "weatherCode");
	}
	//������ѯcountycode&weathercode��������
	private void queryFromServer(final String address, final String type){
		HttpUtil.sendRequest(address, new HttpCallBackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {
					//�ӷ�������Ӧ������������Ϣ
					if (!TextUtils.isEmpty(response)) {
						String[] arra=response.split("\\|");
						if (arra!=null&&arra.length==2) {
							queryWeatherInfo(arra[1]);
						}
					}
				}else if ("weatherCode".equals(type)) {
					
						Utility.handleWeatherResponse(WeatherActivity.this, response);
						//�л���ui���߳�
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						publishText.setText("ͬ��ʧ�ܣ�");
					}
				});
			}
		});
	}
	//��sharepreferemces��ȡ���������ؼ�
	private void showWeather(){
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		publishText.setText("����"+prefs.getString("publish_time", "")+"����");
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		currentDateText.setText(prefs.getString("current_time", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}
}
