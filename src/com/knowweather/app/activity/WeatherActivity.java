package com.knowweather.app.activity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NoTitle;
import org.androidannotations.annotations.ViewById;

import com.knowweather.app.R;
import com.knowweather.app.util.HttpCallbackListener;
import com.knowweather.app.util.HttpUtil;
import com.knowweather.app.util.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

@NoTitle
@EActivity(R.layout.weather_layout)
public class WeatherActivity extends Activity {

	@ViewById(R.id.weather_info_layout)
	LinearLayout weatherInfoLayout;
	
	@ViewById(R.id.city_name)
	TextView cityNameText;
	
	@ViewById(R.id.publish_text)
	TextView publishText;
	
	@ViewById(R.id.weather_desp)
	TextView weatherDespText;
	
	@ViewById(R.id.temp1)
	TextView temp1Text;
	
	@ViewById(R.id.temp2)
	TextView temp2Text;
	
	@ViewById(R.id.current_date)
	TextView currentDateText;
	
	@AfterViews
	void afterViewProcess() {
		String countryCode = getIntent().getStringExtra("country_code");
		if (!TextUtils.isEmpty(countryCode)) {
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countryCode);			
		} else {
			showWeather();
		}
	}

	private void queryWeatherCode(String countryCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countryCode + ".xml";
		queryFromServer(address, "countryCode");		
	}
	
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");		
	}
	
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if ("countryCode".equals(type)) {
					if (!TextUtils.isEmpty(type)) {
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();
						}
					});
					
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						publishText.setText("同步失败！");
					}
				});
				
			}
		});
	}

	private void showWeather() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			cityNameText.setText(prefs.getString("city_name", ""));
			temp1Text.setText(prefs.getString("temp1", ""));
			temp2Text.setText(prefs.getString("temp2", ""));
			weatherDespText.setText(prefs.getString("weather_desp", ""));
			publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
			currentDateText.setText(prefs.getString("current_date", ""));
			weatherInfoLayout.setVisibility(View.VISIBLE);
			cityNameText.setVisibility(View.VISIBLE);
	}


}
