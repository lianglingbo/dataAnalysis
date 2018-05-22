package com.joymeter.service;

import java.util.HashMap;
import java.util.List;

import com.joymeter.entity.DeviceInfo;

public interface AnalysisService {
	void addData(String data);
	
	void register(DeviceInfo deviceInfo);
	
	void updateSim(DeviceInfo deviceInfo);
	
	List<HashMap<String, Object>> getOffline(String data);
	
	List<HashMap<String, Object>> getDeviceByParams(String data);

	//查询抄表失败聚合SQL
	List<HashMap<String, Object>> getReadFailed(String data);

	String getDeviceEvenFromDruid(String data);
}
