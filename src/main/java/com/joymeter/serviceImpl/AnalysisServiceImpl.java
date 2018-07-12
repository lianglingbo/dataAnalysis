package com.joymeter.serviceImpl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONArray;
import com.joymeter.entity.UsageHour;
import com.joymeter.entity.WaterMeterUse;
import com.joymeter.service.RedisService;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;
import com.joymeter.util.TimeTools;
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
	@Autowired
	private RedisService redisService;

	private static String queryUrl = PropertiesUtils.getProperty("queryUrl", "");
	private static final Logger logger = Logger.getLogger(AnalysisServiceImpl.class.getName());
	private static final Logger updateSimLogger = Logger.getLogger("updateSim");
	private static final Logger registerLogger = Logger.getLogger("register");
	private static final Logger updateDeviceLogger = Logger.getLogger("updateDevice");
	private static final Logger addDataLogger = Logger.getLogger("addData");
	private static final Logger usageHourLog = Logger.getLogger("usageHourLog");

	private static String postUsageUrl = PropertiesUtils.getProperty("postUsageUrl", "");




	/**
	 * 1.保存数据到Druid, 数据结构: {"serverId":"001","deviceId":"12345678",
	 * "type":"1","event":"data","data":"","datetime":"1513576307290"}
	 * 2.数据源新增字段，eventinfo，记录event事件的data值（为字符串类型的时候，不能存入druid，druid中设置data属性为double类型）
	 *   数据源新增时，判断事件，如果event 不为 data ，则将其data值存入eventinfo中
	 * 3.新增方法setusageHour() ,mysql新增表：usage_hour；统计每日凌晨0到6点，每个小时的用水量；
	 * 	 每天凌晨，每个整点写入一次数据，最后生成完整信息；mysql中只存当天数据，每天统计结束后，将信息放入druid中；
	 * 	 防止最后有空数据：每次收到数据后，把后面几个小时内容都填充；如果上一小时数据为空，填充所有
	 *   每天定时，清空usage_hour表
	 *   最后把记录存进druid
	 *
	 * @param dataStr
	 */
	@Override
	public void addData(String dataStr) {
		if (StringUtils.isEmpty(dataStr))return;
		try {
			JSONObject jsonData = JSONObject.parseObject(dataStr);
			//获得的json数据格式增加了msg信息，将msg存入druid时，对应的字段为eventInfo
			if(dataStr.contains("msg")){
				dataStr = dataStr.replace("msg","eventinfo");
			}
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
			//发送至缓存
			redisService.sendToCJoy(deviceId,dataStr);
			//更新抄表状态、设备状态、阀门状态
			DeviceInfo deviceInfo = new DeviceInfo();
			deviceInfo.setDeviceId(deviceId);
			if("offline".equals(event)) {
				deviceInfo.setDeviceState("0");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
			}else if ("online".equals(event)||"data".equals(event)||"keepalive".equals(event)||"push".equals(event)) {
				//能收到上面四种事件，说明设备在线
				deviceInfo.setDeviceState("1");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				if ("data".equals(event)) {
					//能收到读表data数据，说明读表成功
					deviceInfo.setReadState("0");
					deviceInfoMapper.updateDeviceInfo(deviceInfo);
					//开始判断凌晨用水情况
					try{
						//获取date时间
						SimpleDateFormat sdfh=new SimpleDateFormat("HH");
						int currenHour =  Integer.valueOf(sdfh.format(new Date(datetime)));
						//每天清空mysql;重要！！寫在定時任務中
						//【3】凌晨0点到6点：整点统计用水量
						if((currenHour >=23 )|| (currenHour >= 0 && currenHour < 6)){
							//判断类型为水表的数据
							if("3200".equals(deviceType) || "3201".equals(deviceType) || "32".equals(deviceType)){
								//每个整点都进行数据统计，如果mysql中有记录则更新，无记录则插入，手动更新时间
								UsageHour usageHour = new UsageHour();
								usageHour.setDeviceId(deviceId);
								//手动更新时间（防止出现数据无修改情况下，mysql不自动更新时间）;存入时间改为设备自带的时间；
								String deviceTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(datetime);//将时间格式转换成符合Timestamp要求的格式.
								usageHour.setDeviceTime(Timestamp.valueOf(deviceTime));

								if(currenHour >=23 ){
									//0点插入，值到zero,one，two，three，four，five，six；
									usageHour.setZero(totaldata);
								}else if(currenHour >=0 && currenHour <6){
									//0点后，插入之前，先判断前一整点的值是否为空；如果为空，初始化所有时间点数据，如果不为空初始化后续时间点数据；
									UsageHour selectResult  = deviceInfoMapper.getOneUsageHour(deviceId);
									if(StringUtils.isEmpty(selectResult)){
										//上一次結果爲空，初始化
										usageHour.setUsageByHour(currenHour,totaldata);
									}else{
										//結果不爲空，判斷上一小時是否有數據
										String lastusage = selectResult.getUsageByHour(currenHour);
										if(StringUtils.isEmpty(lastusage) || "".equals(lastusage) || lastusage.length() == 0){
											//初始化所有數據
											usageHour.setUsageByHour(currenHour,totaldata);
										}else {
											//上一次数据和此次对比，结果返回1，表示上次数据大于这次数据，不合理，判断为异常
											int comp = new BigDecimal(lastusage).compareTo(new BigDecimal(totaldata));
											if(comp == 1){
												//更新状态为1：异常
												usageHour.setStatus("1");
											}else {
												//更新状态为0：正常
												usageHour.setStatus("0");
											}
											//后续时间点数据的初始化
											usageHour.setUsageByHour(currenHour+1,totaldata);
										}
									}
								}
								//插入mysql
								deviceInfoMapper.insertIntoUsageHour(usageHour);
								usageHourLog.log(Level.INFO,usageHour.toString());
							}
						}
					}catch (Exception e){
						addDataLogger.log(Level.INFO,e+"凌晨用水统计方法异常"+dataStr);
					}
				}
			}else if ("close".equals(event)) {
				deviceInfo.setValveState("0");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
			}else if("open".equals(event)){
				deviceInfo.setValveState("1");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
			} else if ("data_failed".equals(event)) {
				deviceInfo.setReadState("1");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
			}
		} catch (Exception e) {
			updateDeviceLogger.log(Level.SEVERE, dataStr, e);
		}
		//发送数据到druid中
		DataCache.add(dataStr);
	}


	/**
	 * 根据参数获取离线设备
	 * 
	 * @param data
	 */
	@Override
	public List<HashMap<String, Object>> getOffline(String data) {
		if (StringUtils.isEmpty(data))return null;
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

	/***
	 * 查询可疑用水情况
	 * @param data
	 * @return
	 */
	@Override
	public List<HashMap<String, Object>> getUsageStatusFailed(String data) {
		if (StringUtils.isEmpty(data))return null;
		logger.log(Level.INFO,data);
		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);
			return deviceInfoMapper.selectUsageStatusFailed(deviceInfo);
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
		if (StringUtils.isEmpty(data))return null;
		JSONObject jsonData = JSONObject.parseObject(data);
		String deviceId = jsonData.getString("deviceId");
		String event = jsonData.getString("event");
		String datetime1 = jsonData.getString("datetime1");
		String datetime2 = jsonData.getString("datetime2");
		//非空判断，时间减去8小时再查询，开始时间与结束时间大小比较
		//条件拼接
		StringBuffer sql = new StringBuffer();

		if(!(StringUtils.isEmpty(deviceId)||deviceId.length()==0)){
			sql.append("where deviceId = '"+ deviceId+"' ");
		}
		if(!(StringUtils.isEmpty(event)||event.length()==0)){

			if(!(StringUtils.isEmpty(sql)||sql.length()==0)){
				//如果sql不为空，则为多条件
				sql.append(" and  event = '"+ event +"' ");
			}else {
				sql.append("  where event = '"+ event +"' ");
			}
			
		}
		if(!(StringUtils.isEmpty(datetime1)||datetime1.length()==0)  &&  !(StringUtils.isEmpty(datetime2)||datetime2.length()==0) ){
			//转时差
			//获取时间,前一天的16点为真实时间的0点，进行拼接：格式：2018-05-03T16
			String startTime = TimeTools.getSpecifiedDayBefore(datetime1)+"T16";
			//结束时间以明天为准，减去8小时
			String endTime = TimeTools.getSpecifiedDayAfter(datetime2)+"T16";
			if(!(StringUtils.isEmpty(sql)||sql.length()==0)){
				//如果sql不为空，则为多条件
				sql.append(" and  __time >= '"+ startTime +"' and  __time <= '" +endTime+"' ");
			}else {
				sql.append("  where  __time >= '"+ startTime +"' and  __time <= '" +endTime+"' ");
			}
		}
		String QUERY_HIST_DATA = "{\"query\":\"select deviceId ,serverId ,event ,eventinfo ,data,( __time + INTERVAL '8' HOUR) as utf8time   from dataInfo   "+sql.toString()+"  order by __time desc limit 500 \"}";

		try {
			String result = HttpClient.sendPost(queryUrl, QUERY_HIST_DATA);
			return  result;
		} catch (Exception e) {
			logger.log(Level.SEVERE, QUERY_HIST_DATA, e);
			return null;
		}
	}

    /**
     * 查询某天可疑用水的水表,用量
     *
     * SELECT max("currentdata") as maxUse ,deviceId FROM "watermeter"
     * WHERE "__time" >= CURRENT_TIMESTAMP - INTERVAL '7' DAY  and currentdata > 0  group by "deviceId" order by max("currentdata") desc
     *
	 * 对时间进行处理，获取的时间需加 减去 8小时
     * @param
     * @return
     */
    @Override
    public String getWaterMeterFromDruid(String time) {
		//获取时间,前一天的16点为真实时间的0点，进行拼接：格式：2018-05-03T16
		String startTime = TimeTools.getSpecifiedDayBefore(time)+"T16";
		//后一天时间
		String endTime =time+"T16";

        String QUERY_WATER_DATA ="{\"query\":\"select deviceId ,max(currentdata) as maxUse   from  watermeter where  __time >='"+startTime+"' and __time <='"+ endTime +"' and  currentdata > 0  group by deviceId order by max(currentdata) desc limit 500 \"}";
        try {
            String result = HttpClient.sendPost(queryUrl, QUERY_WATER_DATA);
            //将json转为对象，遍历每个对象，再增加设备得项目信息
			List<WaterMeterUse> waterMeterUses = JSONObject.parseArray(result, WaterMeterUse.class);
			//循环遍历每个对象，通过id查出项目地信息，加到对象中
			for (WaterMeterUse waterMeterUse :waterMeterUses ) {
				//查询
				DeviceInfo deviceInfo = deviceInfoMapper.getOne(waterMeterUse.getDeviceId());
				if(StringUtils.isEmpty(deviceInfo)){
					logger.log(Level.SEVERE,"查询设备"+waterMeterUse.getDeviceId()+"结果为空");
					continue;
				}
				waterMeterUse.setProject(deviceInfo.getProject());
				waterMeterUse.setProvince(deviceInfo.getProvince());
				waterMeterUse.setCity(deviceInfo.getCity());
				waterMeterUse.setDistrict(deviceInfo.getDistrict());
				waterMeterUse.setCommunity(deviceInfo.getCommunity());
				waterMeterUse.setAddress(deviceInfo.getAddress());

			}
			String s = JSONArray.toJSONString(waterMeterUses);

			return  s;
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
	 * 获取可疑用水的用水和项目信息
	 * @param data
	 * @return
	 */
	@Override
	public List<HashMap<String, Object>> getUsageWithProjectByParams(String data) {
		if (StringUtils.isEmpty(data))return null;

		logger.log(Level.INFO,data);
		try {
			JSONObject jsonObject = JSONObject.parseObject(data);
			DeviceInfo deviceInfo = new DeviceInfo(jsonObject);

			return deviceInfoMapper.getUsageWithProjectByParams(deviceInfo);
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

	//删除设备
	@Override
	public void deleteDeviceInfoById(String deviceId) {
		if("".equals(deviceId) || StringUtils.isEmpty(deviceId)){
			return;
		}
		try {
			logger.log(Level.SEVERE, "删除设备："+ deviceId);
			deviceInfoMapper.deleteDeviceInfoById(deviceId);
		} catch (Exception e) {

		}
	}

	//更新设备信息
	@Override
	public void updateDeviceInfo(DeviceInfo deviceInfo) {
		if("".equals(deviceInfo) || StringUtils.isEmpty(deviceInfo)){
			return;
		}
		try {
			logger.log(Level.SEVERE, "更新设备："+ deviceInfo);
			deviceInfoMapper.updateDeviceInfo(deviceInfo);
		} catch (Exception e) {

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
