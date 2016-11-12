package com.example.li.myweather.util;

/**
 * Created by Li on 2016/11/1 0001.
 */

public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
