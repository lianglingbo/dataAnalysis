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

	//获取可疑用水的用水和项目信息
	List<HashMap<String, Object>> getUsageWithProjectByParams(String data);

	//查询抄表失败聚合SQL
	List<HashMap<String, Object>> getReadFailed(String data);

	//查询可疑用水聚合SQL
	List<HashMap<String, Object>> getUsageStatusFailed(String data);

	String getDeviceEvenFromDruid(String data);

	//查询最某天可疑用水的水表,用量
	String getWaterMeterFromDruid(String time);

	//查询最近7天可疑用水的水表,频率
	String getWaterMeterCountFromDruid( );

	//查询异常水表,新数据data小于老数据data
    String getExceptionWaterMeter();

    //根据deviceId查询watermeter用水情况列表
	String getDeviceInfoFromDruid(String deviceId);



}
