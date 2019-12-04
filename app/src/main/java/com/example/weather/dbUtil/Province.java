package com.example.weather.dbUtil;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport {

    private int id;
    private String provinceName;
    private int provinceCode;

    public int getId() {
        return id;
    }

    public String getName() {
        return provinceName;
    }

    public int getCode() {
        return provinceCode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.provinceName = name;
    }

    public void setCode(int code) {
        this.provinceCode = code;
    }
}