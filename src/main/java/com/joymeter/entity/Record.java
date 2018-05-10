package com.joymeter.entity;

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
	
	public Record(long deviceId, String projectName, String province, String city, String district, String community,
			String status) {
		super();
		this.deviceId = deviceId;
		this.projectName = projectName;
		this.province = province;
		this.city = city;
		this.district = district;
		this.community = community;
		this.status = status;
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
