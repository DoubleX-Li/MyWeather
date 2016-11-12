package com.example.li.myweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.example.li.myweather.db.MyWeatherDB;
import com.example.li.myweather.model.City;
import com.example.li.myweather.model.County;
import com.example.li.myweather.model.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Li on 2016/11/1 0001.
 */

public class Utility {

    /**
     * 处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvincesResponse(
            MyWeatherDB myWeatherDB, String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length > 0) {
                for (String p : allProvinces) {
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    myWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 处理服务器返回的市级数据
     */
    public static boolean handleCitiesResponse(MyWeatherDB myWeatherDB,
                                               String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //
                    myWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     *  处理服务器返回的县级数据
     */
    public static boolean handleCountiesResponse(MyWeatherDB myWeatherDB,
                                                 String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0) {
                for (String c : allCounties) {
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    //
                    myWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }
    /**
     * 解析服务器返回的JSON数据，并将解析出的数据存储到本地
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void handleWeatherResponse(Context context, String response) {
        try {

            // 以下根据和风天气的格式解析JSON
            Log.d("解析", "正在解析");
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather data service 3.0");
            jsonObject = jsonArray.getJSONObject(0);
            JSONObject tempJsonObject = jsonObject.getJSONObject("basic");
            //这里应该使用异常防止空指针，可惜还不够熟练
            String cityName = tempJsonObject.getString("city");
            String weatherCode = tempJsonObject.getString("id");//在本程序中不会使用,用城市ID代替，用于自动更新
            String publishTime = tempJsonObject.getJSONObject("update").getString("utc");

            tempJsonObject = jsonObject.getJSONObject("now");
            String weatherDesp = tempJsonObject.getJSONObject("cond").getString("txt");
            String temp = tempJsonObject.getString("tmp");
            Log.d("气温", temp);
            saveWeatherInfo(context, cityName, weatherCode, temp,weatherDesp, publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中。
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void saveWeatherInfo(Context context, String cityName, String weatherCode,
                                       String temp, String weatherDesp, String publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp", temp);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }
}