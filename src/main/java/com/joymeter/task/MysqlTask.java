package com.joymeter.task;

import com.joymeter.mapper.DeviceInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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


    // 定时任务每天23點40執行一次，清空usageHour表
    @Scheduled(cron = "23 40 0 * * ?")
    public void truncateUsageHour(){
        try{
            deviceInfoMapper.truncateUsageHour();
        }catch (Exception e){
            logger.log(Level.SEVERE, null, e);
        }
     }

    //每天早上6点半执行一次，将usageHour表中数据遍历，同步到druid中
    @Scheduled(cron = "6 30 0 * * ?")
    public void fromUsageHourToDruid(){
        try{
            //查询mysql表中所有数据

            //遍历，发送给druid

        }catch (Exception e){
            logger.log(Level.SEVERE, null, e);
        }
    }
}
