package com.knowweather.app.db;

import java.util.ArrayList;
import java.util.List;

import com.knowweather.app.model.City;
import com.knowweather.app.model.Country;
import com.knowweather.app.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class KnowWeatherDB {

	public static final String DB_NAME = "know_weather";
	
	public static final int VERSION = 1;
	
	private static KnowWeatherDB knowWeatherDB;
	
	private SQLiteDatabase db;
	
	private KnowWeatherDB(Context context) {
		KnowWeatherOpenHelper dbHelp = new KnowWeatherOpenHelper(context, DB_NAME, null, VERSION);
		db = dbHelp.getWritableDatabase();
	}
	
	public synchronized static KnowWeatherDB getInstance (Context context) {
		if (knowWeatherDB == null) {
			knowWeatherDB = new KnowWeatherDB(context);
		}
		return knowWeatherDB;
	}
	
	public void saveProvince(Province province) {
		if (province !=null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}		
	}
	
	public List<Province> loadProvinces() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				list.add(province);
			} while (cursor.moveToNext());

		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}
	
	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}
	
	public List<City> loadCities(int provinceId) {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id = ?", new String[] {String.valueOf(provinceId)}, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				list.add(city);
			} while (cursor.moveToNext());

		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}
	
	public void saveCountry(Country country) {
		if (country != null) {
			ContentValues values = new ContentValues();
			values.put("country_name", country.getCountryName());
			values.put("country_code", country.getCountryCode());
			values.put("city_id", country.getCityId());
			db.insert("Country", null, values);
		}
	}
	
	public List<Country> loadCountries(int cityId) {
		List<Country> list = new ArrayList<Country>();
		Cursor cursor = db.query("Country", null, "city_id = ?", new String[] {String.valueOf(cityId)}, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Country country = new Country();
				country.setId(cursor.getInt(cursor.getColumnIndex("id")));
				country.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
				country.setCountryCode(cursor.getString(cursor.getColumnIndex("country_code")));
				list.add(country);
			} while (cursor.moveToNext());

		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}
}
