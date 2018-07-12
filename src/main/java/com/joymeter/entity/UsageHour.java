package com.joymeter.entity;

import java.sql.Timestamp;

/**
 * @ClassName UsageHour
 * @Description TODO 统计每日凌晨每个小时的用水情况
 * @Author liang
 * @Date 2018/6/5 9:20
 * @Version 1.0
 **/
public class UsageHour {
	private String deviceId;
	private String zero;
	private String one;
	private String two;
	private String three;
	private String four;
	private String five;
	private String six;
	private String status;
	// 设备数据的事件
	private Timestamp deviceTime;
	// 操作记录的事件
	private Timestamp updateTime;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getZero() {
		return zero;
	}

	public void setZero(String zero) {
		this.zero = zero;
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

	/**
	 * 根据小时设置用量
	 * @param hour
	 * @param usage
	 */
	public void setUsageByHour(int hour,String usage) {
    	switch(hour) {
    	case 0:
    		this.zero = usage;
    		break;
    	case 1:
    		this.one = usage;
    		break;
    	case 2:
    		this.two = usage;
    		break;
    	case 3:
    		this.three = usage;
    		break;
    	case 4:
    		this.four = usage;
    		break;
    	case 5:
    		this.five = usage;
    		break;
    	case 6:
    		this.six = usage;
    		break;
    	}
    }
	
	/**
	 * 根据小时获取用量
	 * @param hour
	 * @return
	 */
	public String getUsageByHour(int hour) {
    	switch(hour) {
    	case 0:
    		return zero;
    	case 1:
    		return one;
    	case 2:
    		return two;
    	case 3:
    		return three;
    	case 4:
    		return four;
    	case 5:
    		return five;
    	case 6:
    		return six;
    	}
    	return null;
    }

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getDeviceTime() {
		return deviceTime;
	}

	public void setDeviceTime(Timestamp deviceTime) {
		this.deviceTime = deviceTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		return "UsageHour{" + "deviceId='" + deviceId + '\'' + ", zero='" + zero + '\'' + ", one='" + one + '\''
				+ ", two='" + two + '\'' + ", three='" + three + '\'' + ", four='" + four + '\'' + ", five='" + five
				+ '\'' + ", six='" + six + '\'' + ", status='" + status + '\'' + ", deviceTime=" + deviceTime
				+ ", updateTime=" + updateTime + '}';
	}
}
