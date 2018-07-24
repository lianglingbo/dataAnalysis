package com.joymeter.entity;

/**
 * @ClassName RegionBean
 * @Description TODO
 * @Author liang
 * @Date 2018/7/23 14:42
 * @Version 1.0
 **/
public class RegionBean {
    private String name;
    private String adcode;
    private String padcode;

    @Override
    public String toString() {
        return "RegionBean{" +
                "name='" + name + '\'' +
                ", adcode='" + adcode + '\'' +
                ", padcode='" + padcode + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdcode() {
        return adcode;
    }

    public void setAdcode(String adcode) {
        this.adcode = adcode;
    }

    public String getPadcode() {
        return padcode;
    }

    public void setPadcode(String padcode) {
        this.padcode = padcode;
    }
}
