package com.example.weather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weather.Util.HttpUtil;
import com.example.weather.Util.Utility;
import com.example.weather.dbUtil.City;
import com.example.weather.dbUtil.County;
import com.example.weather.dbUtil.Province;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE =0;
    public static final int LEVEL_CITY =1;
    public static final int LEVEL_COUNTY =2;
    private TextView titletext;
    private Button back;
    private ListView listView;
    private ProgressDialog dialog;
    private ArrayAdapter<String> adapter;
    private List<String> list =new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  =inflater.inflate(R.layout.choose_area,container,false);
        titletext =view.findViewById(R.id.title_text);
        back =view.findViewById(R.id.back_button);
        listView =view.findViewById(R.id.list_view);
        adapter =new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(currentLevel ==LEVEL_PROVINCE){
                    selectedProvince =provinceList.get(position);
                    queryCities();
                }else if(currentLevel ==LEVEL_CITY){
                    selectedCity =cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherID =countyList.get(position).getWeatherID();
                    if(getActivity() instanceof MainActivity){      //在碎片中调用getActivity()用来判断碎片位于哪个Activity内，如果在MainActivity便跳转，如果在WeatherActivity便直接更新页面
                        Intent intent =new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherID);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherID);
                    }
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {        //back按钮里会根据当前的列表级别判断，保证点击back之后能返回到上一级的列表
            @Override
            public void onClick(View view) {
                if(currentLevel ==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel ==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    //查询所有的省份，优先从数据库里查询，数据库没有的话从服务器上查询。城市和街道的查询同理
    private void queryProvinces(){
        titletext.setText("中国");
        back.setVisibility(View.GONE);  //province已经是最上级，无法再返回，所以back按钮要隐藏起来
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            list.clear();
            for(Province province :provinceList){
                list.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel =LEVEL_PROVINCE;
        }else{
            String address ="http://guolin.tech/api/china" ;
            queryFromServer(address,"province");
        }
    }

    private void queryCities(){
        titletext.setText(selectedProvince.getProvinceName());
        back.setVisibility(View.VISIBLE);
        cityList =DataSupport.where("provinceid = ?",String.valueOf(selectedProvince)).find(City.class);
        if(cityList.size()>0){
            list.clear();
            for(City city:cityList){
                list.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel =LEVEL_CITY;
        }else{
            int provinceCode =selectedProvince.getProvinceCode();
            String address ="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    private void queryCounties(){
        titletext.setText(selectedCity.getCityName());
        back.setVisibility(View.VISIBLE);
        countyList =DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            list.clear();
            for(County county :countyList){
                list.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel =LEVEL_COUNTY;
        }else{
            int provinceCode =selectedProvince.getProvinceCode();
            int cityCode =selectedCity.getCityCode();
            String address ="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }


    //将传入的地址和类型放到服务器上查询
    private void queryFromServer(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHtttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {    //从这里要返回主线程处理逻辑
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"load failed",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {  //此处接收服务器返回的数据，然后再处理这些数据
                String responseText =response.body().string();
                boolean result =true;
                try {
                    if("province".equals(type)){
                        result = Utility.handleProvinceResponse(responseText);
                    }else if("city".equals(type)){
                        int provinceID =selectedProvince.getId();
                        result =Utility.handleCityResponse(responseText,provinceID);
                    }else if("county".equals(type)){
                        int cityID =selectedCity.getId();
                        result =Utility.handleCountyResponse(responseText,cityID);
                    }
                    if(result){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
                                if("province".equals(type)){
                                    queryProvinces();
                                }else if("city".equals(type)){
                                    queryCities();
                                }else if("county".equals(type)){
                                    queryCounties();
                                }
                            }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void showProgressDialog(){
        if(dialog ==null){
            dialog =new ProgressDialog(getActivity());
            dialog.setMessage("Loading......");
            dialog.setCanceledOnTouchOutside(false);
        }
        dialog.show();
    }

    private void closeProgressDialog(){
        if(dialog !=null){
            dialog.dismiss();
        }
    }
}
