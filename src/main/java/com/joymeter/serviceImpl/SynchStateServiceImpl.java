package com.joymeter.serviceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.joymeter.entity.DeviceInfo;
import com.joymeter.mapper.DeviceInfoMapper;
import com.joymeter.service.SynchStateService;
import com.joymeter.task.MysqlTask;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ClassName SynchStateServiceImpl
 * @Description TODO
 * 手动触发：同步数据，遍历mysql，查询druid，状态不一致的信息反给前台，并修改状态
 * 事件码 	说明
 * data 	数据
 * data_failed 	读表失败
 * callback_failed 	回调失败
 * online 	上线
 * offline 	离线
 * keepalive 	心跳
 * open 	开的状态
 * open_failed 	开失败
 * close 	关的状态
 * close_failed 	关失败
 * error 	故障设备
 * unknown 	未知设备
 *
 * @Author liang
 * @Date 2018/6/8 9:33
 * @Version 1.0
 **/
@Service
public class SynchStateServiceImpl implements SynchStateService {
    @Autowired
    private DeviceInfoMapper deviceInfoMapper;
    @Autowired
    private MysqlTask mysqlTask;

    private static final Logger logger = Logger.getLogger(SynchStateServiceImpl.class.getName());
    private static String queryUrl = PropertiesUtils.getProperty("queryUrl", "");

    /**
     * 遍历方法mysql，查询druid
     * 根据结果，调响应的方法
     * 同步状态策略：
     *      1.若mysql的操作时间比druid中时间新，则数据以mysql为准；状态不变
     *      2.若druid时间新，则状态不对，以druid为准；修改状态；
     * @return
     */
    @Override
        public String queryDruidFromMysql( ) {
        StringBuffer resultBuffer = new StringBuffer("");
        //每次查询只更新50条记录
        int count = 0;
        try {

            //查mysql，所有数据
            List<DeviceInfo> deviceInfoList = deviceInfoMapper.getAll();
            for (DeviceInfo deviceInfo:deviceInfoList) {

                boolean flag = false;//是否需要更新的标志
                String deviceId = deviceInfo.getDeviceId();
                String deviceState = deviceInfo.getDeviceState();//mysql中设备状态
                String readState = deviceInfo.getReadState();//mysql中抄表状态
                String valveState = deviceInfo.getValveState();//mysql中阀门状态
                Timestamp updateTime = deviceInfo.getUpdateTime();//操作时间  
                if(StringUtils.isEmpty(updateTime) || updateTime.equals("")){
                    logger.log(Level.INFO,"mysql查询时间为空deviceId: "+deviceId);
                    continue;
                }
                String QUERY_DEVICE_DATA ="{\"query\":\"select event,__time  from  dataInfo where  deviceId ='"+deviceId+"'  order by __time desc limit 1 \"}";
                //查询druid
                String result = HttpClient.sendPost(queryUrl, QUERY_DEVICE_DATA);
                if(StringUtils.isEmpty(result) || result.length() < 10){
                    logger.log(Level.INFO,"查询druid结果为空result: "+result);
                    continue;
                }
                JSONArray array = JSONArray.parseArray(result);
                JSONObject jsonObject = array.getJSONObject(0);
                String event = jsonObject.getString("event");
                String __time = jsonObject.getString("__time");
                DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");//将时间格式转换成符合Timestamp要求的格式.
                Calendar cal = Calendar.getInstance();

                int compareTo = 0;
                //druid中的时间比mysql操作时间新，则进行更新
                try {
                    Date druidTime = dfm.parse(__time);
                    cal.setTime(druidTime);
                    cal.add(Calendar.HOUR_OF_DAY,+8);
                    druidTime=cal.getTime();
                    Date mysqlTime = updateTime;
                    //比较大小,druid大，返回1
                    compareTo = druidTime.compareTo(mysqlTime);
                    if(compareTo != 1){
                         logger.log(Level.INFO,"解析时间,druid时间小于mysql，不更新,result: "+result+ "  deviceId:"+deviceId+ "  mysql:"+updateTime+ "  druidTime:"+druidTime);
                         continue;
                    }
                } catch (ParseException e) {
                    logger.log(Level.INFO,e+"解析时间异常");
                 }
             //进行状态的判断，对druid结果进行分析，然后更新mysql
                switch (event) {
				case "data":
					 //得到数据，设备在线，抄表成功
	                 if("0".equals(deviceState) || "".equals(deviceState) || StringUtils.isEmpty(deviceState)){
	                     flag=true;
	                     deviceInfo.setDeviceState("1");
	                 }
	                 if("1".equals(readState) || "".equals(readState) || StringUtils.isEmpty(readState)){
	                     flag=true;
	                     deviceInfo.setReadState("0");
	                 }
					break;
				case "data_failed":
					 //抄表失败
	                 if("0".equals(readState) || "".equals(readState) || StringUtils.isEmpty(readState)){
	                     flag=true;
	                     deviceInfo.setReadState("1");
	                 }
					break;
				case "online":
					//设备在线
	                 if("0".equals(deviceState) || "".equals(deviceState) || StringUtils.isEmpty(deviceState)){
	                     flag=true;
	                     deviceInfo.setDeviceState("1");
	                 }
					break;
				case "offline":
					//设备离线
	                 if("1".equals(deviceState) || "".equals(deviceState) || StringUtils.isEmpty(deviceState)){
	                     flag=true;
	                     deviceInfo.setDeviceState("0");
	                 }
					break;
				case "keepalive":
					 //心跳，设备在线
	                 if("0".equals(deviceState) || "".equals(deviceState) || StringUtils.isEmpty(deviceState)){
	                     flag=true;
	                     deviceInfo.setDeviceState("1");
	                 }
					break;
				case "open":
					 //阀门开
	                 if("0".equals(valveState) || "".equals(valveState) || StringUtils.isEmpty(valveState)) {
	                     flag=true;
	                     deviceInfo.setValveState("1");
	                 }
					break;
				case "open_failed":
					 //阀门开失败
					break;
				case "close":
					//阀门关
	                 if("1".equals(valveState) || "".equals(valveState) || StringUtils.isEmpty(valveState)){
	                     flag=true;
	                     deviceInfo.setValveState("0");
	                 }
					break;
				case "close_failed":
					 //阀门关失败
					break;
				default:
					break;
				}
             //判断完毕，更新数据库
             if(flag==true){
                 if(count > 50){
                     return resultBuffer.toString();
                 }
                 count++;

                 deviceInfoMapper.updateDeviceInfo(deviceInfo);
                 StringBuffer sb = new StringBuffer("设备id为："+deviceId+"  druid中最新事件为："+event);
                 if(deviceState!=null && !deviceState.equals(deviceInfo.getDeviceState())){
                     sb.append("  设备状态为："+deviceState+"  更新为："+deviceInfo.getDeviceState());
                 }
                 if(readState!=null && !readState.equals(deviceInfo.getReadState())){
                     sb.append(" ;读表状态为："+readState+"  更新读表状态为："+deviceInfo.getReadState());
                 }
                 if(valveState!=null && !valveState.equals(deviceInfo.getValveState())){
                     sb.append(" ;阀门状态为："+valveState+"  更新阀门状态为："+deviceInfo.getValveState() );
                 }
                 resultBuffer.append(sb.toString()+"\r\n");
                 logger.log(Level.INFO,"更新设备状态："+resultBuffer.toString());

             }
         }
        }catch (Exception e){
            logger.log(Level.INFO,e+"同步方法异常");
            return "同步方法异常"+e;
        }
        return resultBuffer.toString();
    }

    /**
     * 同步usage_hour 表中数据到druid
     */
    @Override
    public void SynchUsageToDruid() {
        mysqlTask.fromUsageHourToDruid();
    }

    /**
     * 同步各个项目的设备是否删除状态到本地mysql，方法写在定时任务内
     */
    @Override
    public void SynchEachProjectDevice() {
        mysqlTask.syncDeletedDeviceFromAPI();
    }
}
