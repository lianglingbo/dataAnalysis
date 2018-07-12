package com.joymeter.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.joymeter.entity.DeviceInfo;
import com.joymeter.entity.UsageHour;
import com.joymeter.mapper.DeviceInfoMapper;
import com.joymeter.mapper.ProjectInfoMapper;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;
import com.joymeter.util.RSACoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    @Autowired
    ProjectInfoMapper projectInfoMapper;
    private static final Logger logger = Logger.getLogger(MysqlTask.class.getName());
    private static final Logger syncDataLog = Logger.getLogger("syncDataLog");

    private static String postUsageUrl = PropertiesUtils.getProperty("postUsageUrl", "");


    // 定时任务每天22點40執行一次，清空usageHour表
    @Scheduled(cron = "0 40 22 * * ?")
    public void truncateUsageHour(){
        try{
            deviceInfoMapper.truncateUsageHour();
            syncDataLog.log(Level.INFO,"夜间清空数据库");
        }catch (Exception e){
            syncDataLog.log(Level.SEVERE, null, e);
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
                syncDataLog.log(Level.INFO,"夜间同步数据："+postData+"返回结果"+s);
            }
        }catch (Exception e){
            syncDataLog.log(Level.SEVERE, "夜间同步数据异常", e);
        }
    }

    /**
     * 从各项目获取设备状态（是否已经删除）
     * 1.通过deviceid查deviceinfo表
     * 2.得到project查projectinfo表
     * 3.得到对应项目的url
     * 4.调api
     *
     */
    public  boolean isDelete(String deviceId){
        String access_token = RSACoder.getSafeAccessToken();
        String meterNo = deviceId  ;
        String api = "/findUserByMeterNo.do?access_token="+access_token+"&meterNo="+meterNo;
         try{
            //查询此设备的项目url
            DeviceInfo deviceInfo = deviceInfoMapper.getOne(meterNo);
            if(deviceInfo==null) return false;
            String url = projectInfoMapper.getUrl(deviceInfo.getProject());
            if(url==null || url.length()<4) return false;
            url=url+api;
            String result = HttpClient.sendGet(url);
            //如果项目接口异常，查询可能404
             if(StringUtils.isEmpty(result) || result.contains("Error")){
                 syncDataLog.log(Level.SEVERE, "项目接口异常，请求url为："+url);
                 return false;
             }
            JSONObject jsonObject = JSONObject.parseObject(result);
            String data = jsonObject.getString("data");
            if(data==null){
                //设备在上级项目中已删除，本地mysql同步
                syncDataLog.log(Level.SEVERE, "设备在上级平台中已删除，请求url为："+url);
                return true;
            }
        }catch (Exception e){
             syncDataLog.log(Level.SEVERE, "查询findUserByMeterNo接口异常，meterNo："+meterNo, e);
        }
        return false;
    }


    /**
     * 定时任务，每天定时同步各项目上已经删除了的设备；
     * 1.遍历本地mysql库中所有24小时内无数据的设备
     * 2.判断每个设备在上级是否删除
     * 3.如果设备删除了，则本级mysql也删除此设备，并且备份到device_info_deleted表中
     *
     */
    // 定时任务每天3點30執行一次
    @Scheduled(cron = "0 30 21 * * ?")
    public void syncDeletedDeviceFromAPI(){
        try{
            List<DeviceInfo> deviceInfoList = deviceInfoMapper.getNoneDataAday();
            if(deviceInfoList.isEmpty()) return;
            int count = 0;
            for (DeviceInfo deviceInfo:deviceInfoList) {
                if(isDelete(deviceInfo.getDeviceId())){
                    //此设备已删除
                    try{
                        //deviceInfo表中删除此条
                        deviceInfoMapper.deleteDeviceInfoById(deviceInfo.getDeviceId());
                        //device_info_deleted中备份此条
                        projectInfoMapper.insertIntoDel(deviceInfo);
                        syncDataLog.log(Level.SEVERE, "数据库备份数据:"+deviceInfo.toString());
                        count ++;
                    }catch (Exception e){
                        syncDataLog.log(Level.SEVERE, "数据库操作异常",e);
                    }
                }
            }
            syncDataLog.log(Level.SEVERE, "24小时无数据设备总数为："+deviceInfoList.size()+"  删除设备总数为："+count);
        }catch (Exception e){
            syncDataLog.log(Level.SEVERE, "定时同步删除设备任务异常",e);

        }
    }

}
