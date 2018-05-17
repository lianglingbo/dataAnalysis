package com.joymeter.service;

import java.util.HashMap;
import java.util.List;

import com.joymeter.entity.DeviceInfo;

public interface AnalysisService {
	void addData(String data);
	
	void register(DeviceInfo deviceInfo);
	
	void updateSim(DeviceInfo deviceInfo);
	
	List<HashMap<String, Object>> getOffline(String data);
	
	List<HashMap<String, Object>> getOfflineDevice(String data);
}
