package com.joymeter.service;

import java.util.HashMap;
import java.util.List;

import com.joymeter.entity.DeviceInfo;

public interface AnalysisService {
	void addData(String data);
	
	void register(DeviceInfo deviceInfo);
	
	void updateSim(DeviceInfo deviceInfo);
	
	void updateState(String data);
	
	int getOfflineNum();
	
	List<HashMap<String, Object>> getOffline(String data);
	
	List<DeviceInfo> getOfflineDevice(String data);
}
