package com.knowweather.app.activity;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NoTitle;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DrawableRes;

import com.knowweather.app.R;
import com.knowweather.app.service.AutoUpdateService;
import com.knowweather.app.util.HttpCallbackListener;
import com.knowweather.app.util.HttpUtil;
import com.knowweather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@NoTitle
@EActivity(R.layout.weather_layout)
public class WeatherActivity extends Activity {
	
	@ViewById(R.id.whole_layout)
	RelativeLayout wholeLayout;
	
	@DrawableRes(R.drawable.rain)
	Drawable rainPic;

	@DrawableRes(R.drawable.sun)
	Drawable sunPic;
	
	@DrawableRes(R.drawable.cloud)
	Drawable cloudPic;
	
	@DrawableRes(R.drawable.yin)
	Drawable yinPic;
	
	@DrawableRes(R.drawable.snow)
	Drawable snowPic;
	
	@ViewById(R.id.switch_city)
	Button switchCity;
	
	@ViewById(R.id.refresh_weather)
	Button refreshWeather;
	
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
		
		AdView adView = new AdView(this, AdSize.FIT_SCREEN);
		LinearLayout adLayout = (LinearLayout) findViewById(R.id.adLayout);
		adLayout.addView(adView);
	}

	@Click(R.id.switch_city)
	void switchCityClicked() {
		Intent intent = new Intent(this, ChooseAreaActivity_.class);
		intent.putExtra("from_weather_activity", true);
		startActivity(intent);
		finish();
	}
	
	@Click(R.id.refresh_weather)
	void refreshWeatherClicked() {
		publishText.setText("同步中...");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode = prefs.getString("weather_code", "");
		if (!TextUtils.isEmpty(weatherCode)) {
			queryWeatherInfo(weatherCode);
		}
	}
	
//	@Click(R.id.switch_city,R.id.refresh_weather)
//	void buttonClicked(Button bt){ 
//		switch (bt.getId()) {
//		case R.id.switch_city:
//			Intent intent = new Intent(this, ChooseAreaActivity_.class);
//			intent.putExtra("from_weather_activity", true);
//			startActivity(intent);
//			finish();
//			break;
//		case R.id.refresh_weather:
//			publishText.setText("同步中。。。");
//			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//			String weatherCode = prefs.getString("weather_code", "");
//			if (!TextUtils.isEmpty(weatherCode)) {
//				queryWeatherInfo(weatherCode);
//			}
//			break;
//		default:
//			break;
//		}
//	}

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
			String a = prefs.getString("temp1", "");
			String b = prefs.getString("temp2", "");
			weatherDespText.setText(prefs.getString("weather_desp", ""));
			publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
			currentDateText.setText(prefs.getString("current_date", ""));
			weatherInfoLayout.setVisibility(View.VISIBLE);
			cityNameText.setVisibility(View.VISIBLE);
			
			String weatherDesp = prefs.getString("weather_desp", "");
			if (weatherDesp.contains("雨")) { 
				wholeLayout.setBackgroundDrawable(rainPic);
			} else if (weatherDesp.equals("晴") || weatherDesp.contains("晴转")) {
				wholeLayout.setBackgroundDrawable(sunPic);
			} else if (weatherDesp.equals("多云") || weatherDesp.contains("多云转") ) {
				wholeLayout.setBackgroundDrawable(cloudPic);
			} else if (weatherDesp.equals("阴") || weatherDesp.contains("阴转")) {
				wholeLayout.setBackgroundDrawable(yinPic);
			} else if (weatherDesp.contains("雪")) {
				wholeLayout.setBackgroundDrawable(snowPic);
			}
			Intent intent = new Intent(this, AutoUpdateService.class);
			startService(intent);
	}


}
