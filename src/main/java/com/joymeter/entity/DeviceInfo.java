package com.joymeter.entity;

import com.alibaba.fastjson.JSONObject;

public class DeviceInfo {
	private long deviceId;
	private long simId;
	private String gatewayId;
	private String project;
	private String province;
	private String city;
	private String district;
	private String community;
	private String address;
	private int valveState;
	private int deviceState;
	private int simState;
	private long dataUsed;
	

	public DeviceInfo() {
		
	}
	
	public DeviceInfo(JSONObject jsonObject) {
		super();
		this.deviceId = jsonObject.getLongValue("deviceId");
		this.simId = jsonObject.getLongValue("simId");
		this.gatewayId = jsonObject.getString("gatewayId");
		this.project = jsonObject.getString("projectName");
		this.province = jsonObject.getString("province");
		this.city = jsonObject.getString("city");
		this.district = jsonObject.getString("district");
		this.community = jsonObject.getString("community");
		this.valveState = jsonObject.getIntValue("valveState");
		this.deviceState = jsonObject.getIntValue("deviceState");
		this.simState = jsonObject.getIntValue("simState");
		this.dataUsed = jsonObject.getLongValue("dataUsed");
	}
	
	public long getSimId() {
		return simId;
	}

	public void setSimId(long simId) {
		this.simId = simId;
	}

	public String getGatewayId() {
		return gatewayId;
	}

	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getValveState() {
		return valveState;
	}

	public void setValveState(int valveState) {
		this.valveState = valveState;
	}

	public int getMeterState() {
		return deviceState;
	}

	public void setMeterState(int meterState) {
		this.deviceState = meterState;
	}

	public int getSimState() {
		return simState;
	}

	public void setSimState(int simState) {
		this.simState = simState;
	}
	
	public long getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(long deviceId) {
		this.deviceId = deviceId;
	}
	public String getProjectName() {
		return project;
	}
	public void setProjectName(String projectName) {
		this.project = projectName;
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

	public long getDataUsed() {
		return dataUsed;
	}

	public void setDataUsed(long dataUsed) {
		this.dataUsed = dataUsed;
	}
	
}
