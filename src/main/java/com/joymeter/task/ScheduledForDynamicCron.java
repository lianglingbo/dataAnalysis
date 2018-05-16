package com.joymeter.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.joymeter.entity.DeviceInfo;
import com.joymeter.mapper.DeviceInfoMapper;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 1.定时统计设备耗能信息，并存入Druid数据库中 2.定时计算离线设备数量
 */
@Service
public class ScheduledForDynamicCron {
	@Autowired
	private DeviceInfoMapper deviceInfoMapper;

	private static final Logger logger = Logger.getLogger(ScheduledForDynamicCron.class.getName());
	private static final String queryUrl = PropertiesUtils.getProperty("queryUrl", "");
	private static final String postEnUrl = PropertiesUtils.getProperty("postEnUrl", "");
	private static final String postOfUrl = PropertiesUtils.getProperty("postOfUrl", "");
	private static final String[] types = PropertiesUtils.getProperty("types", "").split("\\.");
	private static final String QUERY_TYPE_DATA = PropertiesUtils.getProperty("QUERY_TYPE_DATA", "");
	private static final String QUERY_OFFLINE_DEVICEID = PropertiesUtils.getProperty("QUERY_OFFLINE_DEVICEID", "");
	private static final String QUERY_DEVICEID_STATUS = PropertiesUtils.getProperty("QUERY_DEVICEID_STATUS", "");
	private static final String QUERY_TOTAL_DEVICEID = PropertiesUtils.getProperty("QUERY_TOTAL_DEVICEID", "");

	// 定时任务每天每小时执行一次
	@Scheduled(cron = "0 0 0/1 * * ?")
	public void configureTasks() {
		if (StringUtils.isEmpty(queryUrl) || StringUtils.isEmpty(postEnUrl) || StringUtils.isEmpty(postOfUrl)
				|| (types == null || (types != null && types.length == 0)))
			return;

		getTotalDataByType();
		setOfflineRate();
	}

	/**
	 * 获取不同类型设备总耗能并存入Druid
	 */
	public void getTotalDataByType() {
		String result;
		String data;
		for (String type : types) {
			try {
				result = HttpClient.sendPost(queryUrl, String.format(QUERY_TYPE_DATA, type));
				data = result.contains("sumdata") ? result.substring(result.indexOf(":") + 1, result.indexOf("}"))
						: "0";
				HttpClient.sendPost(postEnUrl, "{\"type\":\"" + type + "\",\"data\":\"" + data + "\",\"datetime\":\""
						+ System.currentTimeMillis() + "\"}");
			} catch (Exception e) {
				logger.log(Level.SEVERE, null, e);
			}
		}
	}

	/**
	 * 计算离线率，存入Druid 1.查询设备总数量
	 */
	public void setOfflineRate() {
		try {
			String result = HttpClient.sendPost(queryUrl, String.format(QUERY_TOTAL_DEVICEID));
			String totalNum = result.contains("totalNum")
					? result.substring(result.indexOf(":") + 1, result.indexOf("}"))
					: "0";
			int offNum = getOfflineCount();
			// 设备类型暂时传空
			HttpClient.sendPost(postOfUrl, "{\"type\":\"" + ' ' + "\",\"offNum\":\"" + offNum + "\",\"totalNum\":\""
					+ totalNum + "\",\"datetime\":\"" + System.currentTimeMillis() + "\"}");
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 计算离线设备数量 1.按设备id分组，查询事件为‘离线’，的所有设备id 2.遍历此结果，得到每个设备id
	 * 3.通过设备id去查询此设备最新的状态，统计最新状态为离线的设备数量,更新设备信息表中表的状态
	 * 
	 * @return
	 */
	public int getOfflineCount() {
		String result = HttpClient.sendPost(queryUrl, QUERY_OFFLINE_DEVICEID); // 获取离线设备Id
		JSONArray jarray = JSONArray.parseArray(result);
		if (jarray == null || jarray.isEmpty())
			return 0;
		int offlinecount = 0;
		JSONObject job;
		JSONArray rArray;
		DeviceInfo deviceInfo = new DeviceInfo();
		for (Object ja : jarray) {
			job = JSONObject.parseObject(ja.toString());
			// 根据设备Id获取设备最后的在线离线事件
			deviceInfo.setDeviceId(job.getString("deviceId"));
			rArray = JSONArray.parseArray(
					HttpClient.sendPost(queryUrl, String.format(QUERY_DEVICEID_STATUS, job.get("deviceId"))));
			if (JSONObject.parseObject(rArray.getString(0)).get("event").equals("offline")) {
				deviceInfo.setDeviceState("0");
				offlinecount++;
			} else {
				deviceInfo.setDeviceState("1");
			}
			deviceInfoMapper.updateDevice(deviceInfo);
		}
		return offlinecount;
	}
}
