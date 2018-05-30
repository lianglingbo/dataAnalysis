package com.joymeter.task;

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
	DeviceInfoMapper deviceInfoMapper;

	private static final Logger logger = Logger.getLogger(ScheduledForDynamicCron.class.getName());
	private static final String queryUrl = PropertiesUtils.getProperty("queryUrl", "");
	private static final String postEnUrl = PropertiesUtils.getProperty("postEnUrl", "");
	private static final String postOfUrl = PropertiesUtils.getProperty("postOfUrl", "");
	private static final String[] types = PropertiesUtils.getProperty("types", "").split("\\.");
	private static final String QUERY_TYPE_DATA = PropertiesUtils.getProperty("QUERY_TYPE_DATA", "");

	// 定时任务每天每小时执行一次
	@Scheduled(cron = "0 0 0/1 * * ?")
	public void configureTasks() {
		if (StringUtils.isEmpty(queryUrl) || StringUtils.isEmpty(postEnUrl) || StringUtils.isEmpty(postOfUrl)
				|| (types == null || (types != null && types.length == 0)))
			return;

		//屏蔽用量统计，让出资源 5.30
		//getTotalDataByType();


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
				String sendJson = "{\"type\":\"" + type + "\",\"data\":\"" + data + "\",\"datetime\":\""
						+ System.currentTimeMillis() + "\"}";
				HttpClient.sendPost(postEnUrl, sendJson );
				logger.log(Level.SEVERE, sendJson, "执行定时任务");
			} catch (Exception e) {
				logger.log(Level.SEVERE, null, e);
			}
		}
	}

	/**
	 * 计算离线率，从mysql中查询离线设备个数与设备总数存入Druid 
	 */
	public void setOfflineRate() {
		try {
			DeviceInfo deviceInfo = new DeviceInfo();
			int totalNum = deviceInfoMapper.getCount(deviceInfo);
			deviceInfo.setDeviceState("0");
			int offNum = deviceInfoMapper.getCount(deviceInfo);
			String sendJson ="{\"type\":\"" + ' ' + "\",\"offNum\":\"" + offNum + "\",\"totalNum\":\""
					+ totalNum + "\",\"datetime\":\"" + System.currentTimeMillis() + "\"}";
			// 设备类型暂时传空
			HttpClient.sendPost(postOfUrl, sendJson );
			logger.log(Level.SEVERE, sendJson, "执行定时任务");
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

}
