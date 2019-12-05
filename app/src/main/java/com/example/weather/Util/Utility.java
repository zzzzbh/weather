package com.example.weather.Util;

import android.text.TextUtils;

import com.example.weather.dbUtil.City;
import com.example.weather.dbUtil.County;
import com.example.weather.dbUtil.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//解析服务器传来的省市街道的数据，采用JSON处理方式

public class Utility {
    public static  boolean handleProvinceResponse(String response) throws JSONException {
        if(!TextUtils.isEmpty(response)){
            JSONArray Allprovince =new JSONArray(response);
            for (int i=0;i<Allprovince.length();i++){
                JSONObject object =Allprovince.getJSONObject(i);
                Province province =new Province();
                province.setProvinceName(object.getString("name"));
                province.setProvinceCode(object.getInt("id"));
                province.save();
            }
            return true;
        }
        return  false;
    }

    public static boolean handleCityResponse (String response, int provinceID) throws JSONException{
        if(!TextUtils.isEmpty(response)){
            JSONArray Allcity =new JSONArray();
            for(int i=0;i<Allcity.length();i++){
                JSONObject object =Allcity.getJSONObject(i);
                City city =new City();
                city.setCityName(object.getString("name"));
                city.setProvinceID(object.getInt("id"));
                city.save();
            }
            return true;
        }
        return false;
    }

    public static boolean handleCountyResponse(String response ,int cityID) throws JSONException{
        if(!TextUtils.isEmpty(response)){
            JSONArray Allcounty =new JSONArray(response);
            for(int i=0;i<Allcounty.length();i++){
                JSONObject object =Allcounty.getJSONObject(i);
                County county =new County();
                county.setCountyName(object.getString("name"));
                county.setCityid(object.getInt("id"));
                county.save();
            }
            return true;
        }
        return false;
    }
}
