package com.knowweather.app.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle.Control;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.NoTitle;
import org.androidannotations.annotations.ViewById;

import com.knowweather.app.R;
import com.knowweather.app.db.KnowWeatherDB;
import com.knowweather.app.model.City;
import com.knowweather.app.model.Country;
import com.knowweather.app.model.Province;
import com.knowweather.app.util.HttpCallbackListener;
import com.knowweather.app.util.HttpUtil;
import com.knowweather.app.util.Utility;

import android.app.Activity;
import android.app.DownloadManager.Query;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@NoTitle
@EActivity(R.layout.choose_area)
public class ChooseAreaActivity extends Activity {

	private boolean isFromWeatherActivity;
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTRY = 2;
	
	private ProgressDialog progressDialog;
	private ArrayAdapter<String> adapter;
	private KnowWeatherDB knowWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	private List<Province> provinceList;
	private List<City> cityList;
	private List<Country> countryList;
	
	private Province selectProvince;
	private City selectCity;
	
	private int currentLevel;
	
	@ViewById(R.id.title_text)
	TextView titleText;
	
	@ViewById(R.id.list_view)
	ListView listView;
	
	@AfterViews
	void afterViewProcess(){
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity_.class);
			startActivity(intent);
			finish();
			return;
		}
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		knowWeatherDB = KnowWeatherDB.getInstance(this);
		
//		listView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//					if (currentLevel == LEVEL_PROVINCE) {
//					selectProvince = provinceList.get(position);
//					queryCitues();
//				} else if (currentLevel == LEVEL_CITY) {
//					selectCity = cityList.get(position);
//					queryCounties();
//				}
//				
//			}
//			
//		});
		queryProvinces();
	}
 
	@ItemClick(R.id.list_view)
    public void myListItemClicked(int position) {
		if (currentLevel == LEVEL_PROVINCE) {
			selectProvince = provinceList.get(position);
			queryCitues();
		} else if (currentLevel == LEVEL_CITY) {
			selectCity = cityList.get(position);
			queryCounties();
		} else if (currentLevel == LEVEL_COUNTRY) {
			String countryCode = countryList.get(position).getCountryCode();
			Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity_.class);
			intent.putExtra("country_code", countryCode);
			startActivity(intent);
			finish();
		}
    }

	private void queryProvinces(){
		provinceList = knowWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;			
		} else {
			queryFromServer(null, "province");
		}
	}

	private void queryCitues() {
		cityList = knowWeatherDB.loadCities(selectProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectProvince.getProvinceName());
			currentLevel = LEVEL_CITY;			
		} else {
			queryFromServer(selectProvince.getProvinceCode(), "city");
		} 
	}

	private void queryCounties() {
		countryList = knowWeatherDB.loadCountries(selectCity.getId());
		if (countryList.size() > 0) {
			dataList.clear();
			for (Country country : countryList) {
				dataList.add(country.getCountryName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectCity.getCityName());
			currentLevel = LEVEL_COUNTRY;			
		} else {
			queryFromServer(selectCity.getCityCode(), "country");
		} 		
		
	}
	
	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code +".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(knowWeatherDB, response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(knowWeatherDB, response, selectProvince.getId());
				} else if ("country".equals(type)) {
					result = Utility.handleCountriesResponse(knowWeatherDB, response, selectCity.getId());
				}
				if (result) {
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCitues();
							} else if ("country".equals(type)) {
								queryCounties();
							}
						}
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
				
			}
		});
		
	}

	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	
	private void closeProgressDialog() {
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTRY) {
			queryCitues();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity_.class);
				startActivity(intent);
			}
			finish();
		}
	}
}


