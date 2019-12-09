package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.weather.Util.HttpUtil;
import com.example.weather.Util.Utility;
import com.example.weather.gsonUtil.Forecast;
import com.example.weather.gsonUtil.Weather;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private LinearLayout forecastLayout;
    private ImageView bingPicImg;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weather_infoText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView sportText;
    private TextView carWashText;
    private TextView comfortText;
    private String mWeatherID;
    private Button nav_button;

    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //初始化控件
        weatherLayout =findViewById(R.id.weather_layout);
        forecastLayout =findViewById(R.id.forecast_layout);
        bingPicImg =findViewById(R.id.bing_pic_img);
        titleCity =findViewById(R.id.title_city);
        titleUpdateTime =findViewById(R.id.title_update_time);
        degreeText =findViewById(R.id.temperature_text);
        weather_infoText =findViewById(R.id.weather_info);
        aqiText =findViewById(R.id.aqi_text);
        pm25Text =findViewById(R.id.pm2_5_txt);
        sportText =findViewById(R.id.sport_text);
        carWashText =findViewById(R.id.car_wash_text);
        comfortText =findViewById(R.id.comfort_text);
        swipeRefreshLayout =findViewById(R.id.swipe);
        nav_button =findViewById(R.id.nav_button);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary);
        drawerLayout =findViewById(R.id.drawer_layout);

        nav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        //处理天气，如果已有缓存的话直接解析，没有的话去服务器查询
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString =sp.getString("weather",null);
        if(weatherString !=null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
            mWeatherID =weather.basic.weatherID;
        }else{
            mWeatherID =getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherID);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {    //创建一个下拉的监听器，下拉时便会请求最新的天气信息
            @Override
            public void onRefresh() {
                requestWeather(mWeatherID);
            }
        });

        String bingPic =sp.getString("bing_pic",null);
        if(bingPic !=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

        //如果SDK在21以上，可以通过以下方法让图片和状态栏融为一体
        if(Build.VERSION.SDK_INT>=21){
            View decorView =getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }


    //根据ID来请求天气信息
    public void requestWeather( final String weatherID){
        String weatherUrl ="http://guolin.tech/api/weather?cityid="+weatherID;  //拼接出一个接口地址，向该地址发送请求，服务器会返回一个JSON字符串
        HttpUtil.sendOkHtttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"Failed To Get Weather Information !!!",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }


            //如果之前的请求成功，收到返回的字符串，则要在这里将JSON字符串解析成对应的Weather实体
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText =response.body().string();
                final Weather weather =Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){         //接收成功的话会将数据暂存到SharedPreferences中
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherID =weather.basic.weatherID;
                        }else{
                            Toast.makeText(WeatherActivity.this,"Failed To Get Weather Information !!!",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }


    //处理并展示Weather实体中的数据
    private void showWeatherInfo(Weather weather){
        String cityName =weather.basic.cityName;
        String updateTime =weather.basic.update.updateTime;
        String degree =weather.now.temperature;
        String info =weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weather_infoText.setText(info);

        forecastLayout.removeAllViews();
        for(Forecast forecast :weather.forecastList){
            View view  = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText =view.findViewById(R.id.date_txt);
            TextView infoText =view.findViewById(R.id.info_txt);
            TextView maxText =view.findViewById(R.id.max_txt);
            TextView minText =view.findViewById(R.id.min_txt);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort ="Comfort Level"+weather.suggesstion.comfort;
        String sport ="Exercise Suggestion"+weather.suggesstion.sport;
        String carwash ="CarWash Index" +weather.suggesstion.carWash;
        comfortText.setText(comfort);
        sportText.setText(sport);
        carWashText.setText(carwash);
        weatherLayout.setVisibility(View.VISIBLE);
    }


    //加载图片
    private void loadBingPic(){
        String requestBingPic ="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHtttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            //向指定地点申请图片，流程和向服务器申请天气数据相差不多，将申请的图片通过Glide()来装载到对应的ImageView中
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic =response.body().string();
                SharedPreferences.Editor editor =PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bingPic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
}
