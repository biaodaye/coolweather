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
	//等级常量
	public static int PROVINCE_LEVEL=0;
	public static int CITY_LEVEL=1;
	public static int COUNTY_LEVEL=2;
	//数据库工具付对象
	public CoolWeatherDB coolWeatherDB;
	//控件
	private TextView titleText;
	private ListView listView;
	private ProgressDialog progressDialog;
	//填充listview工具
	private ArrayAdapter<String> adapter;
	private List<String> dataList=new ArrayList<String>();
	//数据对象列表
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	//选中的对象
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	//当前级别
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_area);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//获得控件和数据库工具对象
		titleText=(TextView) findViewById(R.id.title_text);
		listView=(ListView) findViewById(R.id.list_view);
		Log.d("MainActivity", "1");
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		//填充数据到listview
		adapter=new ArrayAdapter<String>(this, R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		//监听点击列表项，根据点击查询数据
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
		//开启app的时候，在数据库中或者网络上查询省份数据
		queryProvinces();
	}
	//在数据库中查询省份信息
	private void queryProvinces(){
		
		//获得数据库中省份的数据,遍历数据结构中的省份对象，获得省份名称放入datalist
		provinceList=coolWeatherDB.loadProvinces();	
		if(provinceList.size()>0){
		//清除datalist中之前的数据
				dataList.clear();
		for(Province p:provinceList){
			dataList.add(p.getProvinceName());
		}
		//通知adapter数据更新,设置默认选中，设置标题内容
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		titleText.setText("中国");
		//将当前级别设为province
		currentLevel=PROVINCE_LEVEL;
		}else{
			queryFromServer(null, "province");
		}
	}
	//在数据库中查询城市信息，否则联网查询
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
	//在数据库中查询村数据，否则联网查询
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
	//联网查询数据，并插入数据库中
	private void queryFromServer(final String code,final String type){
		String address;
		//判断是否查询省份数据，选择uri
		if (!TextUtils.isEmpty(code)) {
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		//查询数据
		HttpUtil.sendRequest(address, new HttpCallBackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				//解析响应数据，存入数据库
				//判断解析的数据
				boolean result=false;
				if ("province".equals(type)) {
					result=Utility.handleProvincesResponse(coolWeatherDB, response);
					
				}else if ("city".equals(type)) {
					result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if ("county".equals(type)) {
					result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				//回归主线程修改UI控件
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
						Toast.makeText(ChooseAreaActivity.this, "联网加载失败！", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}
	private void showProgressDialog(){
		if (progressDialog==null) {
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("加载中。。。");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	private void closeProgressDialog(){
		if (progressDialog!=null) {
			progressDialog.dismiss();
		}
	}
	//返回键，按级别逐步返回上一级
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
