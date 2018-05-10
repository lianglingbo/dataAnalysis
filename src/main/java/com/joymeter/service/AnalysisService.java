package com.joymeter.service;

import java.util.HashMap;
import java.util.List;

public interface AnalysisService {
	void addData(String data);
	
	void updateStatus(String data);
	
	List<HashMap<String, Object>> getOffline(String params);
}
