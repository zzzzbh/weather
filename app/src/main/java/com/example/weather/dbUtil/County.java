package com.example.weather.dbUtil;

import org.litepal.crud.DataSupport;

public class County extends DataSupport {

    private int id;
    private String countyName;
    private int cityID;
    private int weatherID;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public int getCityid() {
        return cityID;
    }

    public void setCityid(int cityid) {
        this.cityID = cityid;
    }

    public int getWeatherID() {
        return weatherID;
    }

    public void setWeatherID(int weatherID) {
        this.weatherID = weatherID;
    }
}
