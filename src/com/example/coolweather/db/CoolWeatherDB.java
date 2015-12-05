package com.example.coolweather.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	public static final String DB_NAME="cool_weather";
	public static final int VERSION=1;
	public static CoolWeatherDB coolWeatherDB;
	private SQLiteDatabase db;
	private CoolWeatherDB(Context context) {
		CoolWeatherOpenHelper helper=new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION, null);
		db=helper.getWritableDatabase();
		
	}
	public synchronized static CoolWeatherDB getInstance(Context context){
		if (coolWeatherDB==null) {
			coolWeatherDB=new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}
	public void saveProvince(Province province){
		if (province!=null) {
			ContentValues values=new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("province", null, values);
		}
	}
	public List<Province> loadProvinces(){
		List<Province> provinces=new ArrayList<Province>();
		Cursor cursor=db.query("province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Province province=new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				provinces.add(province);
			} while (cursor.moveToNext());
		}
		return provinces;
		}
	
	public void saveCity(City city){
		if (city!=null) {
			ContentValues values=new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("city", null, values);
		}
	}
	public List<City> loadCities(int provinceId){
		List<City> cities=new ArrayList<City>();
		Cursor cursor=db.query("city", null, "province_id=?", new String[]{String.valueOf(provinceId)}, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city=new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				city.setProvinceId(cursor.getInt(cursor.getColumnIndex("province_id")));
				cities.add(city);
			} while (cursor.moveToNext());
		}
		return cities;
		}	
	
}
