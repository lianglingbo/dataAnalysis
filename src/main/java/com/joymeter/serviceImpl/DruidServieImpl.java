package com.joymeter.serviceImpl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.joymeter.service.DruidService;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;

@Service
public class DruidServieImpl implements DruidService{
	@Autowired
	private static final Logger logger = Logger.getLogger(DruidServieImpl.class.getName());
	private static String druidUrl = PropertiesUtils.getProperty("druidUrl", "");
	
	/**
     * 保存数据到Druid, 数据结构: {"serverId":"001","deviceId":"12345678",
     * "type":"1","event":"data","data":"","datetime":"1513576307290"}
     *
     * @param dataStr
     */
	@Override
    public void addData(String dataStr) {
    	if (StringUtils.isEmpty(dataStr)
                || druidUrl.equals("")) return;
    	try {
    		JSONObject jsonData = JSONObject.parseObject(dataStr);
            String serverId = jsonData.getString("serverId");
            String deviceId = jsonData.getString("deviceId");
            String deviceType = jsonData.getString("type");
            String event = jsonData.getString("event");
            long datetime = Long.valueOf(jsonData.getString("datetime"));

            if (StringUtils.isEmpty(serverId) || StringUtils.isEmpty(deviceId)
                    || StringUtils.isEmpty(deviceType) || StringUtils.isEmpty(event)
                    || datetime <= 0) return;
            
            HttpClient.sendPost(druidUrl, dataStr);     //向Druid发送数据
    	}catch (Exception e) {
    		logger.log(Level.SEVERE, null, e);
		}
    }
}
