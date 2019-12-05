package com.example.weather.gsonUtil;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    public String status;
    public Basic basic;
    public Aqi aqi;
    public Now now;
    public Suggesstion suggesstion;

    @SerializedName("daily_forecast")
    private List<Forecast> forecastList;
}
