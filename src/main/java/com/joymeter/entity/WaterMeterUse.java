package com.joymeter.entity;

/**
 * @ClassName WaterMeterUse
 * @Description 用水量实体，当日最大用水量，设备id
 * @Author liang
 * @Date 2018/6/1 9:09
 * @Version 1.0
 **/
public class WaterMeterUse {
    //新增maxuse，统计当日最大用水量
    private String maxUse;
    private String deviceId;

    private String project;
    private String province;
    private String city;
    private String district;
    private String community;
    private String address;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMaxUse() {
        return maxUse;
    }

    public void setMaxUse(String maxUse) {
        this.maxUse = maxUse;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "WaterMeterUse{" +
                "maxUse='" + maxUse + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", project='" + project + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", community='" + community + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
