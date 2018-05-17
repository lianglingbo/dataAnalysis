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
			DeviceInfo deviceInfo = new DeviceInfo();
			int totalNum = deviceInfoMapper.getCount(deviceInfo);
			deviceInfo.setDeviceState("0");
			int offNum = deviceInfoMapper.getCount(deviceInfo);
			// 设备类型暂时传空
			HttpClient.sendPost(postOfUrl, "{\"type\":\"" + ' ' + "\",\"offNum\":\"" + offNum + "\",\"totalNum\":\""
					+ totalNum + "\",\"datetime\":\"" + System.currentTimeMillis() + "\"}");
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

}
