package com.example.weather.gsonUtil;

import com.google.gson.annotations.SerializedName;

public class Forecast {     //这是数组，在引用时要以数组形式引用
    public String date;

    @SerializedName("tem")
    public Temperature temperature;
    @SerializedName("cond")
    public More more;

    public class Temperature{
        public String max;
        public String min;
    }

    public class More{
        @SerializedName("txt")
        public String info;
    }
}
