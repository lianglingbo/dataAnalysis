package com.joymeter.serviceImpl;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
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
	public void event(String dataStr) {
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
	 * @param params
	 */
	@Override
	public List<HashMap<String, Object>> getOffline(String params) {
		if (StringUtils.isEmpty(params))return null;

		try {
			JSONObject jsonObject = JSONObject.parseObject(params);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);

			return deviceInfoMapper.getofflineCount(deviceInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
		return null;
	}

	/**
	 * 根据参数获取离线设备详细信息
	 * 
	 * @param params
	 */
	@Override
	public List<DeviceInfo> getOfflineDevice(String params) {
		if (StringUtils.isEmpty(params))return null;

		try {
			JSONObject jsonObject = JSONObject.parseObject(params);
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

		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);
			deviceInfoMapper.insert(deviceInfo);
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
	public void updateSIM(String data) {
		if (StringUtils.isEmpty(data))return;

		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);
			deviceInfoMapper.updateSim(deviceInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

}
