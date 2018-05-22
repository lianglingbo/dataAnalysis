package com.joymeter.serviceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.joymeter.cache.DataCache;
import com.joymeter.entity.DeviceInfo;
import com.joymeter.mapper.DeviceInfoMapper;
import com.joymeter.service.AnalysisService;

@Service
public class AnalysisServiceImpl implements AnalysisService {
	@Autowired
	private DeviceInfoMapper deviceInfoMapper;
	private static final Logger logger = Logger.getLogger(AnalysisServiceImpl.class.getName());
	private static final Logger updateSimLogger = Logger.getLogger("updateSim");
	private static final Logger registerLogger = Logger.getLogger("register");
	private static final Logger updateDeviceLogger = Logger.getLogger("updateDevice");
	private static final Logger addDataLogger = Logger.getLogger("addData");
	/**
	 * 保存数据到Druid, 数据结构: {"serverId":"001","deviceId":"12345678",
	 * "type":"1","event":"data","data":"","datetime":"1513576307290"}
	 *
	 * @param dataStr
	 */
	@Override
	public void addData(String dataStr) {
		if (StringUtils.isEmpty(dataStr))return;
		try {
			JSONObject jsonData = JSONObject.parseObject(dataStr);
			String serverId = jsonData.getString("serverId");
			String deviceId = jsonData.getString("deviceId");
			String deviceType = jsonData.getString("type");
			String event = jsonData.getString("event");
			long datetime = Long.valueOf(jsonData.getString("datetime"));

			if (StringUtils.isEmpty(serverId) || StringUtils.isEmpty(deviceId) || StringUtils.isEmpty(deviceType)
					|| StringUtils.isEmpty(event) || datetime <= 0)
				return;

			addDataLogger.log(Level.INFO,dataStr);
			
			DataCache.add(dataStr);
			
			//更新抄表状态、设备状态、阀门状态
			DeviceInfo deviceInfo = new DeviceInfo();
			deviceInfo.setDeviceId(deviceId);
			if("offline".equals(event)) {
				deviceInfo.setDeviceState("0");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				updateDeviceLogger.log(Level.INFO,"更新设备状态,deviceId:"+deviceId+"even:"+event);
			}else if ("online".equals(event)||"data".equals(event)||"keepalive".equals(event)||"push".equals(event)) {
				if ("data".equals(event)) {
					deviceInfo.setReadState("0");
				}
				deviceInfo.setDeviceState("1");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				updateDeviceLogger.log(Level.INFO,"更新设备状态,deviceId:"+deviceId+"even:"+event);
			}else if ("close".equals(event)) {
				deviceInfo.setValveState("0");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				updateDeviceLogger.log(Level.INFO,"更新设备状态,deviceId:"+deviceId+"even:"+event);
			}else if ("open".equals(event)) {
				deviceInfo.setValveState("1");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				updateDeviceLogger.log(Level.INFO,"更新设备状态,deviceId:"+deviceId+"even:"+event);
			}else if ("data_failed".equals(event)) {
				deviceInfo.setReadState("1");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				updateDeviceLogger.log(Level.INFO,"更新设备状态,deviceId:"+deviceId+"even:"+event);
			}
		} catch (Exception e) {
			updateDeviceLogger.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 根据参数获取离线设备
	 * 
	 * @param data
	 */
	@Override
	public List<HashMap<String, Object>> getOffline(String data) {
		if (StringUtils.isEmpty(data))return null;
		logger.log(Level.INFO,data);
		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);
			return deviceInfoMapper.getofflineGroup(deviceInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
		return null;
	}

	/**
	 * 获取抄表失败的设备数量
	 * @param data
	 * @return
	 */
	@Override
	public List<HashMap<String, Object>> getReadFailed(String data) {
		if (StringUtils.isEmpty(data))return null;
		logger.log(Level.INFO,data);
		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);
			return deviceInfoMapper.getReadFailedGroup(deviceInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
		return null;
	}


	/**
	 * 根据参数获取离线设备详细信息
	 * 
	 * @param data
	 */
	@Override
	public List<HashMap<String, Object>> getDeviceByParams(String data) {
		if (StringUtils.isEmpty(data))return null;

		logger.log(Level.INFO,data);
		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);

			return deviceInfoMapper.getByParams(deviceInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
		return null;
	}


	/**
	 * 注册设备相关信息
	 *
	 * @param
	 */
	@Override
	public void register(DeviceInfo deviceInfo) {
		registerLogger.log(Level.INFO, deviceInfo.toString());
		if (StringUtils.isEmpty(deviceInfo.getDeviceId()) || StringUtils.isEmpty(deviceInfo.getGatewayId()) 
				|| StringUtils.isEmpty(deviceInfo.getProject())|| StringUtils.isEmpty(deviceInfo.getProvince())
				|| StringUtils.isEmpty(deviceInfo.getCity())|| StringUtils.isEmpty(deviceInfo.getDistrict())
				|| StringUtils.isEmpty(deviceInfo.getCommunity())|| StringUtils.isEmpty(deviceInfo.getAddress()))
			return;

		try {
			if (deviceInfoMapper.getOne(deviceInfo.getDeviceId())==null) {
				deviceInfoMapper.insert(deviceInfo);
			}else {
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
			}
		} catch (Exception e) {
			registerLogger.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 更新SIM卡的流量
	 * 
	 * @param deviceInfo
	 */
	@Override
	public void updateSim(DeviceInfo deviceInfo) {
		updateSimLogger.log(Level.INFO, deviceInfo.toString());
		if (StringUtils.isEmpty(deviceInfo.getDeviceId()) || StringUtils.isEmpty(deviceInfo.getSimId())
				|| StringUtils.isEmpty(deviceInfo.getSimState())|| StringUtils.isEmpty(deviceInfo.getDataUsed()))
			return;
		try {
			deviceInfoMapper.updateDeviceInfo(deviceInfo);
		} catch (Exception e) {
			updateSimLogger.log(Level.SEVERE, null, e);
		}
	}

}
