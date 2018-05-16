package com.joymeter.service;

import java.util.HashMap;
import java.util.List;

import com.joymeter.entity.DeviceInfo;

public interface AnalysisService {
	void addData(String data);
	
	void register(String data);
	
	void updateSim(String data);
	
	void updateState(String data);
	
	int getOfflineNum();
	
	List<HashMap<String, Object>> getOffline(String data);
	
	List<DeviceInfo> getOfflineDevice(String data);
}
