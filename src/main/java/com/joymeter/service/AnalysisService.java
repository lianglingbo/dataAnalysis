package com.joymeter.service;

import java.util.HashMap;
import java.util.List;

import com.joymeter.entity.DeviceInfo;

public interface AnalysisService {
	void event(String data);
	
	void register(String data);
	
	void updateSIM(String data);
	
	void updateStatus(String data);
	
	List<HashMap<String, Object>> getOffline(String data);
	
	List<DeviceInfo> getOfflineDevice(String data);
}
