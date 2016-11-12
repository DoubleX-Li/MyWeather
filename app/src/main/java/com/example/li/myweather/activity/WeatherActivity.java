package com.example.li.myweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.li.myweather.R;
import com.example.li.myweather.service.AutoUpdateService;
import com.example.li.myweather.util.HttpCallbackListener;
import com.example.li.myweather.util.HttpUtil;
import com.example.li.myweather.util.Utility;

public class WeatherActivity extends Activity implements View.OnClickListener {

    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     */
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView publishText;
    /**
     * 用于显示天气描述信息
     */
    private TextView weatherDespText;
    /**
     * 用于显示气温
     */
    private TextView tempText;

    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;
    /**
     * 用于显示空气质量
     */
    private TextView quality;
    /**
     * 用于显示PM2.5
     */
    private TextView pm25;
    /**
     * 用于显示明日日期
     */
    private TextView tDate;
    /**
     * 用于显示明日天气
     */
    private TextView tTxt;
    /**
     * 用于显示明日高温
     */
    private TextView tTmpMax;
    /**
     * 用于显示明日低温
     */
    private TextView tTmpMin;
    /**
     * 用于显示明日降雨概率
     */
    private TextView tPop;
    /**
     * 切换城市按钮
     */
    private Button switchCity;
    /**
     * 更新天气按钮
     */
    private Button refreshWeather;

    private String countyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        //实例化
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        tempText = (TextView) findViewById(R.id.temp);
        currentDateText = (TextView) findViewById(R.id.current_date);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        quality = (TextView) findViewById(R.id.quality);
        pm25 = (TextView) findViewById(R.id.pm25);

        tDate = (TextView) findViewById(R.id.tomorrow_date);
        tTxt = (TextView) findViewById(R.id.tomorrow_weather);
        tTmpMax = (TextView) findViewById(R.id.tomorrow_Maxtemp);
        tTmpMin = (TextView) findViewById(R.id.tomorrow_Mintemp);
        tPop = (TextView) findViewById(R.id.tomorrow_pop);

        countyCode = getIntent().getStringExtra("county_code");

        if (!TextUtils.isEmpty(countyCode)) {
            // 有县级代号时就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            // 没有县级代号时就直接显示本地天气
            showWeather();
        }
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherCode(countyCode);//更新就是在去查一次，这里保证县代码不变
                }
                queryWeatherCode(countyCode);
                break;
            default:
                break;
        }
    }

    /**
     * 查询县级代号所对应的天气代号。
     */
    private void queryWeatherCode(String countyCode) {
        String address = "https://api.heweather.com/x3/weather?cityid=CN101" +
                countyCode + "&key=" + "17e7e0af58ae4312a287eba47626db00";
        queryFromServer(address, "countyCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
     */
    private void queryFromServer(final String address, final String type) {
        Log.d("从服务器查询", type);
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFinish(final String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {

                        Utility.handleWeatherResponse(WeatherActivity.this, response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeather();
                            }
                        });
                    }
                } else if ("weatherCode".equals(type)) {

                    //这里这种情况不存在
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name", ""));
        tempText.setText(prefs.getString("temp", "") + "℃  ");
        weatherDespText.setText(prefs.getString("weather_desp", ""));
        publishText.setText(prefs.getString("publish_time", "").substring(10) + "发布");
        currentDateText.setText(prefs.getString("current_date", ""));
        quality.setText(prefs.getString("quality", ""));
        pm25.setText(prefs.getString("pm25", ""));

        // 明日信息
        tDate.setText(prefs.getString("tomorrow_date", ""));
        tTxt.setText(prefs.getString("tomorrow_txt", ""));
        tTmpMax.setText(prefs.getString("tomorrow_maxtemp", ""));
        tTmpMin.setText(prefs.getString("tomorrow_mintemp", ""));
        tPop.setText(prefs.getString("tomorrow_pop",""));

        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}