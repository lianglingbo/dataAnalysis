package com.joymeter.serviceImpl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.joymeter.task.Scheduler;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.joymeter.cache.DataCache;
import com.joymeter.entity.DeviceInfo;
import com.joymeter.mapper.DeviceInfoMapper;
import com.joymeter.service.AnalysisService;

@Service
public class AnalysisServiceImpl implements AnalysisService {
	@Autowired
	private DeviceInfoMapper deviceInfoMapper;

	private static String postWMUrl = PropertiesUtils.getProperty("postWMUrl", "");
	private static String queryUrl = PropertiesUtils.getProperty("queryUrl", "");
	private static int watermeterTime = Integer.valueOf(PropertiesUtils.getProperty("watermeterTime", ""));
	private static final Logger logger = Logger.getLogger(AnalysisServiceImpl.class.getName());
	private static final Logger updateSimLogger = Logger.getLogger("updateSim");
	private static final Logger registerLogger = Logger.getLogger("register");
	private static final Logger updateDeviceLogger = Logger.getLogger("updateDevice");
	private static final Logger addDataLogger = Logger.getLogger("addData");





	/**
	 * 1.保存数据到Druid, 数据结构: {"serverId":"001","deviceId":"12345678",
	 * "type":"1","event":"data","data":"","datetime":"1513576307290"}
	 * 2.数据源新增字段，eventinfo，记录event事件的data值（为字符串类型的时候，不能存入druid，druid中设置data属性为double类型）
	 *   数据源新增时，判断事件，如果event 不为 data ，则将其data值存入eventinfo中
	 * @param dataStr
	 */
	@Override
	public void addData(String dataStr) {
		if (StringUtils.isEmpty(dataStr))return;
		try {
			JSONObject jsonData = JSONObject.parseObject(dataStr);
			String serverId = jsonData.getString("serverId");
			String deviceId = jsonData.getString("deviceId");
			String deviceType = jsonData.getString("type");
			String event = jsonData.getString("event");
			String totaldata = jsonData.getString("data");
			long datetime = Long.valueOf(jsonData.getString("datetime"));

			if (StringUtils.isEmpty(serverId) || StringUtils.isEmpty(deviceId) || StringUtils.isEmpty(deviceType)
					|| StringUtils.isEmpty(event) || datetime <= 0)
				return;
			addDataLogger.log(Level.INFO,dataStr);
			//如果event 不为 data ，则将其data值存入eventinfo中
			if(!"data".equals(event)){
				jsonData.put("eventinfo",totaldata);
				dataStr = jsonData.toJSONString();
			}
			DataCache.add(dataStr);
			
			//更新抄表状态、设备状态、阀门状态
			DeviceInfo deviceInfo = new DeviceInfo();
			deviceInfo.setDeviceId(deviceId);
			if("offline".equals(event)) {
				deviceInfo.setDeviceState("0");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
			}else if ("online".equals(event)||"data".equals(event)||"keepalive".equals(event)||"push".equals(event)) {
				if ("data".equals(event)) {
					deviceInfo.setReadState("0");
					//增加水表用量,数据源,筛选条件:时间0点到6点,事件data,设备类型type3200JLAA无线冷水表,3201有线冷水表,32冷水表
					//时间戳转换为时间
					try{
						SimpleDateFormat sdf=new SimpleDateFormat("HH");
						int currenHour =  Integer.valueOf(sdf.format(new Date(datetime)));
						//凌晨0点到6点
						if(currenHour >= 0 && currenHour <= watermeterTime){
							if("3200".equals(deviceType) || "3201".equals(deviceType) || "32".equals(deviceType)){
								/**
								 * 时间，设备类型都符合，添加到watermeter数据源中
								 * 1.查询当前设备当天第一条数据
								 * 2.如果有结果，获取totaldata，和当前用量，计算出用量差 ，如果小于0，标记为-1
								 * 3.如果没有结果，此数据为新数据，currentdata为0
								 *
								 */
								//String QUERY_USED_DATA = "{\"query\":\"select  totaldata,deviceId,__time  from watermeter where  __time >= CURRENT_TIMESTAMP - INTERVAL '6' HOUR order by __time desc limit 1 \"}";
								String QUERY_USED_DATA = "{\"query\":\"select  totaldata,deviceId ,__time  from watermeter where deviceId = "+deviceId+" and __time >= CURRENT_TIMESTAMP - INTERVAL '6' HOUR order by __time asc limit 1 \"}";
								String result = HttpClient.sendPost(queryUrl, QUERY_USED_DATA);
								String currentdata;
								if (result.length()<10){
									//返回结果为空
									 currentdata ="0";
								}else{
									String  lasttotaldata= result.contains("totaldata") ? result.substring(result.indexOf(":") + 1, result.indexOf(",")) : "0";
									BigDecimal b = new BigDecimal(totaldata).subtract(new BigDecimal(lasttotaldata));
									if((b.compareTo(BigDecimal.ZERO)) == -1) {
										//与首次data比较 结果小于0，不合理，标记为-1
										currentdata ="-1";
									}else{
										//与上次data结果比较，如果小于0，不合理，标记为-1
										String QUERY_LAST_DATA = "{\"query\":\"select  totaldata,deviceId  from watermeter where deviceId = "+deviceId+" order by __time  desc  limit 1 \"}";
										String dataResult = HttpClient.sendPost(queryUrl, QUERY_LAST_DATA);
										String  ldata= dataResult.contains("totaldata") ? dataResult.substring(result.indexOf(":") + 1, dataResult.indexOf(",")) : "0";
										BigDecimal tmp = new BigDecimal(totaldata).subtract(new BigDecimal(ldata));
										if((tmp.compareTo(BigDecimal.ZERO)) == -1) {
											currentdata ="-1";
										}else {
											currentdata = b.toString();
										}
									}
								}
								String postData = "{\"type\":\"" + deviceType+ "\",\"totaldata\":\"" + totaldata + "\",\"currentdata\":\"" + currentdata + "\",\"serverId\":\"" + serverId + "\",\"deviceId\":\"" + deviceId + "\",\"datetime\":\"" + datetime + "\"}";
								HttpClient.sendPost(postWMUrl, postData); // 向Druid发送数据
								addDataLogger.log(Level.INFO,"插入watermeter"+postData);
							}
						}
					}catch (Exception e){
						addDataLogger.log(Level.INFO,e+"插入watermeter数据源异常"+dataStr);
					}


				}
				deviceInfo.setDeviceState("1");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
			}else if ("close".equals(event)) {
				deviceInfo.setValveState("0");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				deviceInfo.setValveState("1");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
			}else if ("data_failed".equals(event)) {
				deviceInfo.setReadState("1");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
			}
		} catch (Exception e) {
			updateDeviceLogger.log(Level.SEVERE, dataStr, e);
		}
	}

	/**
	 * 根据参数获取离线设备
	 * 
	 * @param data
	 */
	@Override
	public List<HashMap<String, Object>> getOffline(String data) {
		if (StringUtils.isEmpty(data))return null;
		logger.log(Level.INFO,data);
		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);
			return deviceInfoMapper.getofflineGroup(deviceInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, data, e);
		}
		return null;
	}

	/**
	 * 获取抄表失败的设备数量
	 * @param data
	 * @return
	 */
	@Override
	public List<HashMap<String, Object>> getReadFailed(String data) {
		if (StringUtils.isEmpty(data))return null;
		logger.log(Level.INFO,data);
		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);
			return deviceInfoMapper.getReadFailedGroup(deviceInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, data, e);
		}
		return null;
	}

	/**
	 * 从druid中获取设备的历史事件信息
	 * @param data
	 * @return
	 */
	@Override
	public String getDeviceEvenFromDruid(String data) {
		String QUERY_HIST_DATA = "{\"query\":\"select deviceId ,serverId ,event ,data,( __time + INTERVAL '8' HOUR) as utf8time   from dataInfo where deviceId = "+data+"  order by __time desc limit 5000 \"}";
		try {
			String result = HttpClient.sendPost(queryUrl, QUERY_HIST_DATA);
			return  result;
		} catch (Exception e) {
			logger.log(Level.SEVERE, QUERY_HIST_DATA, e);
			return null;
		}
	}

    /**
     * 查询最近7天可疑用水的水表,用量
     *
     * SELECT max("currentdata") as maxUse ,deviceId FROM "watermeter"
     * WHERE "__time" >= CURRENT_TIMESTAMP - INTERVAL '7' DAY  and currentdata > 0  group by "deviceId" order by max("currentdata") desc
     *
     * @param
     * @return
     */
    @Override
    public String getWaterMeterFromDruid() {
        String QUERY_WATER_DATA ="{\"query\":\"select deviceId ,max(currentdata) as maxUse   from  watermeter where  __time >= CURRENT_TIMESTAMP - INTERVAL '7' DAY and  currentdata > 0  group by deviceId order by max(currentdata) desc limit 500 \"}";

        try {
            String result = HttpClient.sendPost(queryUrl, QUERY_WATER_DATA);
            return  result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, QUERY_WATER_DATA, e);
            return null;
        }
    }

	//查询最近7天可疑用水的水表,频率
	@Override
	public String getWaterMeterCountFromDruid() {
		String QUERY_WATER_DATA ="{\"query\":\"select deviceId ,count(1) as useCount   from  watermeter where  __time >= CURRENT_TIMESTAMP - INTERVAL '7' DAY and  currentdata > 0  group by deviceId order by count(1) desc limit 500 \"}";

		try {
			String result = HttpClient.sendPost(queryUrl, QUERY_WATER_DATA);
			return  result;
		} catch (Exception e) {
			logger.log(Level.SEVERE, QUERY_WATER_DATA, e);
			return null;
		}
	}

	/**
	 * 查询异常水表,新数据data小于老数据data，currentdata标记为-1的水表
	 * @return
	 */
	@Override
	public String getExceptionWaterMeter() {
		String QUERY_WATER_DATA ="{\"query\":\"select deviceId,count(1) as exceptCount  from  watermeter where  __time >= CURRENT_TIMESTAMP - INTERVAL '7' DAY  and  currentdata = '-1' group by deviceId order by exceptCount desc limit 500 \"}";
		try {
			String result = HttpClient.sendPost(queryUrl, QUERY_WATER_DATA);
			return  result;
		} catch (Exception e) {
			logger.log(Level.SEVERE, QUERY_WATER_DATA, e);
			return null;
		}
	}

	/**
	 * 根据deviceId查询watermeter用水情况列表
	 * @param deviceId
	 * @return
	 */
	@Override
	public String getDeviceInfoFromDruid(String deviceId) {
		String QUERY_WATER_DATA ="{\"query\":\"select deviceId,currentdata,totaldata,( __time + INTERVAL '8' HOUR) as utf8time  from  watermeter where  __time >= CURRENT_TIMESTAMP - INTERVAL '7' DAY  and deviceId = '"+deviceId+"'   order by __time  limit 5000 \"}";
		try {
			String result = HttpClient.sendPost(queryUrl, QUERY_WATER_DATA);
			return  result;
		} catch (Exception e) {
			System.out.println(QUERY_WATER_DATA+e);
			logger.log(Level.SEVERE, QUERY_WATER_DATA, e);
			return null;
		}
	}


	/**
	 * 根据参数获取离线设备详细信息
	 * 
	 * @param data
	 */
	@Override
	public List<HashMap<String, Object>> getDeviceByParams(String data) {
		if (StringUtils.isEmpty(data))return null;

		logger.log(Level.INFO,data);
		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);

			return deviceInfoMapper.getByParams(deviceInfo);
		} catch (Exception e) {
			logger.log(Level.SEVERE, data, e);
			return null;
		}

	}


	/**
	 * 注册设备相关信息
	 *
	 * @param
	 */
	@Override
	public void register(DeviceInfo deviceInfo) {
		registerLogger.log(Level.INFO, deviceInfo.toString());
		if (StringUtils.isEmpty(deviceInfo.getDeviceId()) || StringUtils.isEmpty(deviceInfo.getGatewayId()) 
				|| StringUtils.isEmpty(deviceInfo.getProject())|| StringUtils.isEmpty(deviceInfo.getProvince())
				|| StringUtils.isEmpty(deviceInfo.getCity())|| StringUtils.isEmpty(deviceInfo.getDistrict())
				|| StringUtils.isEmpty(deviceInfo.getCommunity())|| StringUtils.isEmpty(deviceInfo.getAddress()))
			return;

		try {
			if (deviceInfoMapper.getOne(deviceInfo.getDeviceId())==null) {
				deviceInfoMapper.insert(deviceInfo);
			}else {
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
			}
		} catch (Exception e) {
			registerLogger.log(Level.SEVERE, deviceInfo.toString(), e);
		}
	}

	/**
	 * 更新SIM卡的流量
	 * 
	 * @param deviceInfo
	 */
	@Override
	public void updateSim(DeviceInfo deviceInfo) {
		updateSimLogger.log(Level.INFO, deviceInfo.toString());
		if (StringUtils.isEmpty(deviceInfo.getDeviceId()) || StringUtils.isEmpty(deviceInfo.getSimId())
				|| StringUtils.isEmpty(deviceInfo.getSimState())|| StringUtils.isEmpty(deviceInfo.getDataUsed()))
			return;
		try {
			deviceInfoMapper.updateDeviceInfo(deviceInfo);
		} catch (Exception e) {
			updateSimLogger.log(Level.SEVERE, deviceInfo.toString(), e);
		}
	}

}
