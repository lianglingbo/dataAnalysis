package com.joymeter.serviceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.joymeter.entity.DeviceInfo;
import com.joymeter.mapper.DeviceInfoMapper;
import com.joymeter.service.AnalysisService;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;

@Service
public class AnalysisServiceImpl implements AnalysisService {
	@Autowired
	private DeviceInfoMapper recordMapper;
	private static final Logger logger = Logger.getLogger(AnalysisServiceImpl.class.getName());
	private static String druidUrl = PropertiesUtils.getProperty("druidUrl", "");

	/**
	 * 保存数据到Druid, 数据结构: {"serverId":"001","deviceId":"12345678",
	 * "type":"1","event":"data","data":"","datetime":"1513576307290"}
	 *
	 * @param dataStr
	 */
	@Override
	public void event(String dataStr) {
		if (StringUtils.isEmpty(dataStr) || druidUrl.equals(""))
			return;
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

			HttpClient.sendPost(druidUrl, dataStr); // 向Druid发送数据
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
	public void updateStatus(String data) {
		if (StringUtils.isEmpty(data))
			return;

		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo record = new DeviceInfo(jsonObject);

			recordMapper.update(record);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 根据参数获取离线数量
	 * 
	 * @param params
	 */
	@Override
	public List<HashMap<String, Object>> getOffline(String params) {
		if (StringUtils.isEmpty(params))
			return null;

		try {
			JSONObject jsonObject = JSONObject.parseObject(params);
			DeviceInfo record = new DeviceInfo(jsonObject);

			return recordMapper.getofflineCount(record);
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
		if (StringUtils.isEmpty(params))
			return null;

		try {
			JSONObject jsonObject = JSONObject.parseObject(params);
			DeviceInfo record = new DeviceInfo(jsonObject);

			return recordMapper.getByParams(record);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
		return null;
	}

	/**
	 *注册设备相关信息 
	 *
	 *@param data
	 */
	@Override
	public void register(String data) {
		
	}

	/**
	 * 更新SIM卡的流量
	 * 
	 * @param data
	 */
	@Override
	public void updateSIM(String data) {
		// TODO Auto-generated method stub
		
	}

}
