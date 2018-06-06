package com.joymeter.task;

import com.alibaba.fastjson.JSON;
import com.joymeter.entity.UsageHour;
import com.joymeter.mapper.DeviceInfoMapper;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ClassName MysqlTask
 * @Description TODO
 * @Author liang
 * @Date 2018/6/5 13:56
 * @Version 1.0
 **/
@Service
public class MysqlTask {
    @Autowired
    DeviceInfoMapper deviceInfoMapper;
    private static final Logger logger = Logger.getLogger(ScheduledForDynamicCron.class.getName());
    private static String postUsageUrl = PropertiesUtils.getProperty("postUsageUrl", "");


    // 定时任务每天23點40執行一次，清空usageHour表
    @Scheduled(cron = "0 40 23 * * ?")
    public void truncateUsageHour(){
        try{
            deviceInfoMapper.truncateUsageHour();
            logger.log(Level.SEVERE,"夜间清空数据库");
        }catch (Exception e){
            logger.log(Level.SEVERE, null, e);
        }
     }

    //每天执行一次，将usageHour表中数据遍历，同步到druid中;批量导入方式；
    @Scheduled(cron = "0 0 23 * * ?")
    public void fromUsageHourToDruid(){
        try{
            //查询mysql表中所有数据
            List<UsageHour> usageHours = deviceInfoMapper.selectAllUsgae();
            for (UsageHour usage:usageHours) {
                String postData = JSON.toJSONString(usage);
                //遍历，发送给druid
                String s = HttpClient.sendPost(postUsageUrl, postData);// 向Druid发送数据
                logger.log(Level.SEVERE,"夜间同步数据："+postData);
            }
        }catch (Exception e){
            logger.log(Level.SEVERE, null, e);
        }
    }
}
