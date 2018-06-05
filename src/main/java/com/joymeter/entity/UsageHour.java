package com.joymeter.entity;

import java.sql.Timestamp;

/**
 * @ClassName UsageHour
 * @Description TODO
 * 统计每日凌晨每个小时的用水情况
 * @Author liang
 * @Date 2018/6/5 9:20
 * @Version 1.0
 **/
public class UsageHour {
    private String deviceId;
    private String one;
    private String two;
    private String three;
    private String four;
    private String five;
    private String six;
    private String status;
    private Timestamp updateTime;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getOne() {
        return one;
    }

    public void setOne(String one) {
        this.one = one;
    }

    public String getTwo() {
        return two;
    }

    public void setTwo(String two) {
        this.two = two;
    }

    public String getThree() {
        return three;
    }

    public void setThree(String three) {
        this.three = three;
    }

    public String getFour() {
        return four;
    }

    public void setFour(String four) {
        this.four = four;
    }

    public String getFive() {
        return five;
    }

    public void setFive(String five) {
        this.five = five;
    }

    public String getSix() {
        return six;
    }

    public void setSix(String six) {
        this.six = six;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "UsageHour{" +
                "deviceId='" + deviceId + '\'' +
                ", one='" + one + '\'' +
                ", two='" + two + '\'' +
                ", three='" + three + '\'' +
                ", four='" + four + '\'' +
                ", five='" + five + '\'' +
                ", six='" + six + '\'' +
                ", status='" + status + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
