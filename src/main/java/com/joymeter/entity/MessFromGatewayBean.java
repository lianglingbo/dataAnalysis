package com.joymeter.entity;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName MessFromGatewayBean
 * @Description TODO
 * 网关发送数据，调用add接口的json对应bean
 * @Author liang
 * @Date 2018/7/18 11:14
 * @Version 1.0
 **/
public class MessFromGatewayBean {
    private String serverId;
    private String deviceId;
    private String type;
    private String event;
    private String msg;
    private String datetime;
    private String data;



    public MessFromGatewayBean(JSONObject jsonObject) {
        super();
        this.serverId = jsonObject.getString("serverId");
        this.deviceId = jsonObject.getString("deviceId");
        this.type = jsonObject.getString("type");
        this.event = jsonObject.getString("event");
        this.msg = jsonObject.getString("msg");
        this.datetime = jsonObject.getString("datetime");
        this.data = jsonObject.getString("data");

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "MessFromGatewayBean{" +
                "serverId='" + serverId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", type='" + type + '\'' +
                ", event='" + event + '\'' +
                ", msg='" + msg + '\'' +
                ", datetime='" + datetime + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
