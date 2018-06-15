package com.joymeter.task;

import com.alibaba.fastjson.JSON;
import com.joymeter.entity.UsageHour;
import com.joymeter.mapper.DeviceInfoMapper;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
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


    // 定时任务每天22點40執行一次，清空usageHour表
    @Scheduled(cron = "0 40 22 * * ?")
    public void truncateUsageHour(){
        try{
            deviceInfoMapper.truncateUsageHour();
            logger.log(Level.SEVERE,"夜间清空数据库");
        }catch (Exception e){
            logger.log(Level.SEVERE, null, e);
        }
     }

    //每天执行一次，将usageHour表中数据遍历，同步到druid中;批量导入方式；用量为0的数据不同步
    @Scheduled(cron = "0 0 22 * * ?")
    public void fromUsageHourToDruid(){
        try{
            //查询mysql表中所有数据
            List<UsageHour> usageHours = deviceInfoMapper.selectExceptionUsage();
            for (UsageHour usage:usageHours) {
                //设置操作事件为当前时间
                Date date = new Date();
                String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date);//将时间格式转换成符合Timestamp要求的格式.
                Timestamp updatetime =Timestamp.valueOf(nowTime);//把时间转换
                usage.setUpdateTime(updatetime);
                //计算每个小时的用量
                BigDecimal zero=new BigDecimal(usage.getZero());
                BigDecimal one=new BigDecimal(usage.getOne());
                BigDecimal two=new BigDecimal(usage.getTwo());
                BigDecimal three=new BigDecimal(usage.getThree());
                BigDecimal four=new BigDecimal(usage.getFour());
                BigDecimal five=new BigDecimal(usage.getFive());
                BigDecimal six=new BigDecimal(usage.getSix());
                //计算
                String ones = one.subtract(zero).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                String twos = two.subtract(one).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                String threes = three.subtract(two).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                String fours = four.subtract(three).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                String fives = five.subtract(four).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                String sixs = six.subtract(five).setScale(2, BigDecimal.ROUND_HALF_UP).toString();

                //赋值
                usage.setZero("0");
                usage.setOne(ones);
                usage.setTwo(twos);
                usage.setThree(threes);
                usage.setFour(fours);
                usage.setFive(fives);
                usage.setSix(sixs);
                //转json
                String postData = JSON.toJSONString(usage);
                //遍历，发送给druid
                String s = HttpClient.sendPost(postUsageUrl, postData);// 向Druid发送数据
                logger.log(Level.SEVERE,"夜间同步数据："+postData+"返回结果"+s);
            }
        }catch (Exception e){
            logger.log(Level.SEVERE, "夜间同步数据异常", e);
        }
    }
}
