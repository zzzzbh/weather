package com.example.weather.gsonUtil;

import com.google.gson.annotations.SerializedName;

public class Aqi {

    public AQICity city;
    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
