package com.example.li.myweather.util;

import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Li on 2016/11/1 0001.
 */

public class HttpUtil {

    public static void sendHttpRequest(final String address,final Resources resources,
                                       final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    String fileName = "";
                    if (address != null) {//不能直接加，否则会变成null字符串
                        fileName = "city" + address+".txt";
                    } else {
                        fileName = "city"+".txt";
                    }

                    InputStream in = resources.getAssets().open(fileName);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in,"utf-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }
                    in.close();

                } catch (Exception e) {
                    if (listener != null) {
                        //
                        Log.d("A", e.toString());
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }


    public static void sendHttpRequest(final String address,
                                       final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (listener != null) {
                        // 回调onFinish()方法
                        listener.onFinish(response.toString());
                    }


                } catch (Exception e) {
                    Log.d("异常", "");
                    if (listener != null) {
                        // 回调onError()方法
                        listener.onError(e);
                    }
                } finally {

                }
            }
        }).start();
    }
}
