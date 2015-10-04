package com.knowweather.app.util;

import android.text.TextUtils;

import com.knowweather.app.db.KnowWeatherDB;
import com.knowweather.app.model.City;
import com.knowweather.app.model.Country;
import com.knowweather.app.model.Province;

public class Utility {

	public synchronized static boolean handleProvincesResponse(KnowWeatherDB knowWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					knowWeatherDB.saveProvince(province);					
				}
				return true;
			}
		}
		return false;
	}
	
	public synchronized static boolean handleCitiesResponse(KnowWeatherDB knowWeatherDB, String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String p : allCities) {
					String[] array = p.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					knowWeatherDB.saveCity(city);		
				}
				return true;
			}
		}
		return false;
	}
	
	public synchronized static boolean handleCountriesResponse(KnowWeatherDB knowWeatherDB, String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCountries = response.split(",");
			if (allCountries != null && allCountries.length > 0) {
				for (String p : allCountries) {
					String[] array = p.split("\\|");
					Country country = new Country();
					country.setCountryCode(array[0]);
					country.setCountryName(array[1]);
					country.setCityId(cityId);
					knowWeatherDB.saveCountry(country);
				}
				return true;
			}
		}
		return false;
	}
	
}
