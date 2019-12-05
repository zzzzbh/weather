package com.example.weather.Util;

import okhttp3.OkHttpClient;
import okhttp3.Request;


//发起HTTP请求，与服务器交互
public class HttpUtil {
    public static void sendOkHtttpRequest (String address,okhttp3.Callback callback){
        OkHttpClient client  =new OkHttpClient();
        Request request =new  Request.Builder().url(address).build();
        client.newCall(request);
    }
}
