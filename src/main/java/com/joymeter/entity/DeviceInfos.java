package com.joymeter.entity;

/**
 * @ClassName DeviceInfoList
 * @Description TODO
 * 此实体类为用于后续逐步替代deviceInfo实体类
 * 自定义设备列表实体类，多了时差，去掉simId等
 * @Author liang
 * @Date 2018/6/13 17:30
 * @Version 1.0
 **/
public class DeviceInfos {
    private String project;
    private String province;
    private String city;
    private String district;
    private String community;
    private String address;
    private String lastUpdate;  //数据库字段为：updateTime格式化后
    private String diffTime;
    private String deviceId;
    private String gatewayId;
    private String deviceState;
    private String readState;
    private String readFaile;

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

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getDiffTime() {
        return diffTime;
    }

    public void setDiffTime(String diffTime) {
        this.diffTime = diffTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(String deviceState) {
        this.deviceState = deviceState;
    }

    public String getReadState() {
        return readState;
    }

    public void setReadState(String readState) {
        this.readState = readState;
    }

    public String getReadFaile() {
        return readFaile;
    }

    public void setReadFaile(String readFaile) {
        this.readFaile = readFaile;
    }

    @Override
    public String toString() {
        return "DeviceInfos{" +
                "project='" + project + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", community='" + community + '\'' +
                ", address='" + address + '\'' +
                ", lastUpdate='" + lastUpdate + '\'' +
                ", diffTime='" + diffTime + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", gatewayId='" + gatewayId + '\'' +
                ", deviceState='" + deviceState + '\'' +
                ", readState='" + readState + '\'' +
                ", readFaile='" + readFaile + '\'' +
                '}';
    }
}
