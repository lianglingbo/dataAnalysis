package com.joymeter.serviceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.joymeter.cache.DataCache;
import com.joymeter.entity.DeviceInfo;
import com.joymeter.mapper.DeviceInfoMapper;
import com.joymeter.service.AnalysisService;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;

@Service
public class AnalysisServiceImpl implements AnalysisService {
	@Autowired
	private DeviceInfoMapper deviceInfoMapper;
	private static final Logger logger = Logger.getLogger(AnalysisServiceImpl.class.getName());
	private static final String queryUrl = PropertiesUtils.getProperty("queryUrl", "");
	private static final String QUERY_OFFLINENUM = PropertiesUtils.getProperty("QUERY_OFFLINENUM", "");

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

			logger.log(Level.INFO,dataStr);
			//DataCache.add(dataStr);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 更新设备状态
	 * 
	 * @param data
	 */
	@Override
	public void updateState(String data) {
		if (StringUtils.isEmpty(data))return;

		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);

			logger.log(Level.INFO,data);
			deviceInfoMapper.updateDevice(deviceInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
	}
	
	/**
	 * 获取总离线数量
	 */
	@Override
	public int getOfflineNum() {
		try {
			String result = HttpClient.sendPost(queryUrl, QUERY_OFFLINENUM);
			JSONArray jarray = JSONArray.parseArray(result);
			if (jarray == null || jarray.isEmpty())
				return 0;
			JSONObject jsonObject = JSONObject.parseObject(jarray.get(0).toString());
			return jsonObject.getIntValue("offNum");
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
		return 0;
	}

	/**
	 * 根据参数获取离线数量
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
			List<HashMap<String, Object>> result = deviceInfoMapper.getofflineCount(deviceInfo);
			logger.log(Level.INFO, result.toString());
			return result;
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
	public List<DeviceInfo> getOfflineDevice(String data) {
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
	 * @param data
	 */
	@Override
	public void register(String data) {
		if (StringUtils.isEmpty(data))return;

		logger.log(Level.INFO, data);
		try {
			DeviceInfo deviceInfo = new DeviceInfo(JSONObject.parseObject(data));
			if (deviceInfoMapper.getOne(deviceInfo.getDeviceId())==null) {
				deviceInfoMapper.insert(deviceInfo);
			}else {
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 更新SIM卡的流量
	 * 
	 * @param data
	 */
	@Override
	public void updateSim(String data) {
		if (StringUtils.isEmpty(data))return;

		logger.log(Level.INFO, data);
		try {
			DeviceInfo deviceInfo = new DeviceInfo(JSONObject.parseObject(data));
			deviceInfoMapper.updateSim(deviceInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

}
