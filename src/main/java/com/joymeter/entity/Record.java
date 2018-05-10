package com.joymeter.entity;

import com.alibaba.fastjson.JSONObject;

public class Record {
	private long deviceId;
	private String projectName;
	private String province;
	private String city;
	private String district;
	private String community;
	private String status;
	
	public Record() {
		
	}
	
	public Record(JSONObject jsonObject) {
		super();
		this.deviceId = jsonObject.getLongValue("deviceId");
		this.projectName = jsonObject.getString("projectName");
		this.province = jsonObject.getString("province");
		this.city = jsonObject.getString("city");
		this.district = jsonObject.getString("district");
		this.community = jsonObject.getString("community");
		this.status = jsonObject.getString("status");
	}
	
	public long getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(long deviceId) {
		this.deviceId = deviceId;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
