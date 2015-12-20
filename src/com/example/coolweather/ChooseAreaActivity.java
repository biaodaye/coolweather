package com.example.coolweather;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.db.City;
import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpCallBackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	//�ȼ�����
	public static int PROVINCE_LEVEL=0;
	public static int CITY_LEVEL=1;
	public static int COUNTY_LEVEL=2;
	//���ݿ⹤�߸�����
	public CoolWeatherDB coolWeatherDB;
	//�ؼ�
	private TextView titleText;
	private ListView listView;
	private ProgressDialog progressDialog;
	//���listview����
	private ArrayAdapter<String> adapter;
	private List<String> dataList=new ArrayList<String>();
	//���ݶ����б�
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	//ѡ�еĶ���
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	//��ǰ����
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_area);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//��ÿؼ������ݿ⹤�߶���
		titleText=(TextView) findViewById(R.id.title_text);
		listView=(ListView) findViewById(R.id.list_view);
		Log.d("MainActivity", "1");
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		//������ݵ�listview
		adapter=new ArrayAdapter<String>(this, R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		//��������б�����ݵ����ѯ����
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (currentLevel==PROVINCE_LEVEL) {
					selectedProvince=provinceList.get(arg2);
					queryCities();
				}else if (currentLevel==CITY_LEVEL) {
					selectedCity=cityList.get(arg2);
					queryCounties();
				}else if (currentLevel==COUNTY_LEVEL) {
					selectedCounty=countyList.get(arg2);
					Intent intent=new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", selectedCounty.getCountyCode());
					startActivity(intent);
				}
			}
		});
		//����app��ʱ�������ݿ��л��������ϲ�ѯʡ������
		queryProvinces();
	}
	//�����ݿ��в�ѯʡ����Ϣ
	private void queryProvinces(){
		
		//������ݿ���ʡ�ݵ�����,�������ݽṹ�е�ʡ�ݶ��󣬻��ʡ�����Ʒ���datalist
		provinceList=coolWeatherDB.loadProvinces();	
		if(provinceList.size()>0){
		//���datalist��֮ǰ������
				dataList.clear();
		for(Province p:provinceList){
			dataList.add(p.getProvinceName());
		}
		//֪ͨadapter���ݸ���,����Ĭ��ѡ�У����ñ�������
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		titleText.setText("�й�");
		//����ǰ������Ϊprovince
		currentLevel=PROVINCE_LEVEL;
		}else{
			queryFromServer(null, "province");
		}
	}
	//�����ݿ��в�ѯ������Ϣ������������ѯ
	private void queryCities(){
		
		cityList=coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size()>0) {
			dataList.clear();
			for(City c:cityList){
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=CITY_LEVEL;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	//�����ݿ��в�ѯ�����ݣ�����������ѯ
	private void queryCounties(){
		countyList=coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size()>0) {
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=COUNTY_LEVEL;
		}else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	//������ѯ���ݣ����������ݿ���
	private void queryFromServer(final String code,final String type){
		String address;
		//�ж��Ƿ��ѯʡ�����ݣ�ѡ��uri
		if (!TextUtils.isEmpty(code)) {
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		//��ѯ����
		HttpUtil.sendRequest(address, new HttpCallBackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				//������Ӧ���ݣ��������ݿ�
				//�жϽ���������
				boolean result=false;
				if ("province".equals(type)) {
					result=Utility.handleProvincesResponse(coolWeatherDB, response);
					
				}else if ("city".equals(type)) {
					result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if ("county".equals(type)) {
					result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				//�ع����߳��޸�UI�ؼ�
				if (result) {
					
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								closeProgressDialog();
								if ("province".equals(type)) {
								queryProvinces();
								}else if ("city".equals(type)) {
									queryCities();
								}else if ("county".equals(type)) {
									queryCounties();
								}
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
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "��������ʧ�ܣ�", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}
	private void showProgressDialog(){
		if (progressDialog==null) {
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("�����С�����");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	private void closeProgressDialog(){
		if (progressDialog!=null) {
			progressDialog.dismiss();
		}
	}
	//���ؼ����������𲽷�����һ��
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (currentLevel==CITY_LEVEL) {
			queryProvinces();
		}else if (currentLevel==COUNTY_LEVEL) {
			queryCities();
		}else {
			finish();
		}
	}
}
