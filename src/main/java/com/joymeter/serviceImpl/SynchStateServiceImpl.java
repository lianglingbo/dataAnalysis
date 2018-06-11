package com.joymeter.serviceImpl;

import com.joymeter.entity.DeviceInfo;
import com.joymeter.mapper.DeviceInfoMapper;
import com.joymeter.service.SynchStateService;
import com.joymeter.task.MysqlTask;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

                String QUERY_DEVICE_DATA ="{\"query\":\"select event  from  dataInfo where  deviceId ='"+deviceId+"'  order by __time desc limit 1 \"}";
                //查询druid
                String result = HttpClient.sendPost(queryUrl, QUERY_DEVICE_DATA);
                String event = result.contains("event") ? result.substring(result.indexOf(":") + 2, result.indexOf("}")-1) : "";
                //进行状态的判断，对druid结果进行分析，然后更新mysql
                if("data".equals(event)){
                    //得到数据，设备在线，抄表成功
                    if("0".equals(deviceState) || "".equals(deviceState) || StringUtils.isEmpty(deviceState)){
                        flag=true;
                        deviceInfo.setDeviceState("1");
                    }
                    if("1".equals(readState) || "".equals(readState) || StringUtils.isEmpty(readState)){
                        flag=true;
                        deviceInfo.setReadState("0");
                    }
                }else if("data_failed".equals(event)){
                    //抄表失败
                    if("0".equals(readState) || "".equals(readState) || StringUtils.isEmpty(readState)){
                        flag=true;
                        deviceInfo.setReadState("1");
                    }
                }else if("online".equals(event)){
                    //设备在线
                    if("0".equals(deviceState) || "".equals(deviceState) || StringUtils.isEmpty(deviceState)){
                        flag=true;
                        deviceInfo.setDeviceState("1");
                    }
                }else if("offline".equals(event)){
                    //设备离线
                    if("1".equals(deviceState) || "".equals(deviceState) || StringUtils.isEmpty(deviceState)){
                        flag=true;
                        deviceInfo.setDeviceState("0");
                    }
                }else if("keepalive".equals(event) ){
                    //心跳，设备在线
                    if("0".equals(deviceState) || "".equals(deviceState) || StringUtils.isEmpty(deviceState)){
                        flag=true;
                        deviceInfo.setDeviceState("1");
                    }
                }else if("open".equals(event)){
                    //阀门开
                    if("0".equals(valveState) || "".equals(valveState) || StringUtils.isEmpty(valveState)) {
                        flag=true;
                        deviceInfo.setValveState("1");
                    }
                }else if("open_failed".equals(event)){
                    //阀门开失败

                }else if("close".equals(event)){
                    //阀门关
                    if("1".equals(valveState) || "".equals(valveState) || StringUtils.isEmpty(valveState)){
                        flag=true;
                        deviceInfo.setValveState("0");
                    }
                }else if("close_failed".equals(event)){
                    //阀门关失败
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
            return "e+\"同步方法异常\"";
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
}
