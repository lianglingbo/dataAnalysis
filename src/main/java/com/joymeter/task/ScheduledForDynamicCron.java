package com.joymeter.task;

import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 定时采集设备耗能信息，并存入Druid数据库中
 */
@Service
public class ScheduledForDynamicCron implements SchedulingConfigurer {

    @Autowired
    private static String cron = "0 0 0/%s * * ?";
    private static final String analysisRate = PropertiesUtils.getProperty("analysisRate", "30");
    private static final Logger logger = Logger.getLogger(ScheduledForDynamicCron.class.getName());
    private static final String queryUrl = PropertiesUtils.getProperty("queryUrl","");
    private static final String postUrl = PropertiesUtils.getProperty("postUrl","");
    private static final String[] types = PropertiesUtils.getProperty("types","").split("\\.");

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (StringUtils.isEmpty(queryUrl) || StringUtils.isEmpty(postUrl)
                || (types==null || (types!=null && types.length==0))) return;
        try {
            taskRegistrar.addTriggerTask(() -> {
                getTotalDataByType();
            }, (triggerContext) -> {
                //定时任务触发，可修改定时任务的执行周期，默认30分钟
                CronTrigger trigger = new CronTrigger(String.format(cron, analysisRate));
                return trigger.nextExecutionTime(triggerContext);
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * 获取不同类型设备总耗能并存入Druid
     */
    public void getTotalDataByType(){
        for (String type : types) {
            try {
                String result = HttpClient.sendPost(queryUrl, "{\"query\":\"" + String.format(Queries.QUERY_TYPE_DATA, type) + "\"}");
                String data = result.contains("sumdata")?result.substring(result.indexOf(":")+1,result.indexOf("}")):"0";
                HttpClient.sendPost(postUrl, "{\"type\":\"" + type + "\",\"data\":\"" + data + "\",\"datetime\":\"" + System.currentTimeMillis() + "\"}");
            }catch (Exception e) {
                logger.log(Level.SEVERE,null,e);
            }
        }
    }
}
