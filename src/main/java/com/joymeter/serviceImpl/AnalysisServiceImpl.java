package com.joymeter.serviceImpl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONArray;
import com.joymeter.entity.MsgFromGatewayBean;
import com.joymeter.entity.UsageHour;
import com.joymeter.entity.WaterMeterUse;
import com.joymeter.service.RedisService;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;
import com.joymeter.util.ResultUtil;
import com.joymeter.util.TimeTools;
import com.joymeter.util.common.EmptyUtils;
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
	private static String updateStatusUrl = PropertiesUtils.getProperty("updateStatusUrl", "");

	private static final Logger logger = Logger.getLogger(AnalysisServiceImpl.class.getName());
	private static final Logger updateSimLogger = Logger.getLogger("updateSim");
	private static final Logger registerLogger = Logger.getLogger("register");
	private static final Logger updateDeviceLogger = Logger.getLogger("updateDevice");
	private static final Logger addDataLogger = Logger.getLogger("addData");
	private static final Logger usageHourLog = Logger.getLogger("usageHourLog");

	/**
	 * 1.保存数据到Druid, 数据结构: {"serverId":"001","deviceId":"12345678",
	 * "type":"1","event":"data","data":"","datetime":"1513576307290","msg":"msg"}
	 * 2.数据源新增字段，eventinfo，记录event事件的data值（为字符串类型的时候，不能存入druid，druid中设置data属性为double类型）
	 * 数据源新增时，判断事件，如果event 不为 data ，则将其data值存入eventinfo中 3.新增方法setusageHour()
	 * ,mysql新增表：usage_hour；统计每日凌晨0到6点，每个小时的用水量；
	 * 每天凌晨，每个整点写入一次数据，最后生成完整信息；mysql中只存当天数据，每天统计结束后，将信息放入druid中；
	 * 防止最后有空数据：每次收到数据后，把后面几个小时内容都填充；如果上一小时数据为空，填充所有 每天定时，清空usage_hour表 最后把记录存进druid
	 *
	 * 4.设备的状态变更和阀门状态变化 需要通知业务层
	 * 
	 * @param dataStr
	 */
	@Override
	public Map<String, Object> addData(String dataStr) {

		if (EmptyUtils.isEmpty(dataStr))
			return ResultUtil.error(400, "Unexpected param");

		try {
			JSONObject jsonData = JSONObject.parseObject(dataStr);
			MsgFromGatewayBean messFromGatewayBean = new MsgFromGatewayBean(jsonData);
			// 内容非空校验
			if (messFromGatewayBean.isEmpty()) {
				addDataLogger.log(Level.SEVERE, "接收的内容有空值" + dataStr);
				return ResultUtil.error(406, "Unexpected param");
			}

			// 过滤事件，发送至mysql
			eventFilter(messFromGatewayBean);
			// 发送数据到druid中
			// 获得的json数据格式增加了msg信息，将msg存入druid时，对应的字段为eventInfo
			if (dataStr.contains("msg")) {
				dataStr = dataStr.replace("msg", "eventinfo");
			}
			DataCache.add(dataStr);
			addDataLogger.log(Level.INFO, dataStr);
		} catch (Exception e) {
			addDataLogger.log(Level.SEVERE, "add接口异常" + dataStr, e);
		}
		return ResultUtil.success();

	}

	/**
	 * 分析设备的状态，更新抄表状态、设备状态、阀门状态发送至mysql的deviceInfo表中
	 * 
	 * @param messFromGatewayBean
	 */
	public void eventFilter(MsgFromGatewayBean messFromGatewayBean) {
		String event = messFromGatewayBean.getEvent();
		String deviceId = messFromGatewayBean.getDeviceId();
		String dataUsed = messFromGatewayBean.getData();
		// 更新抄表状态、设备状态、阀门状态 ，deviceState , valveState
		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.setDeviceId(deviceId);
		switch (event) {
		case "offline": // 设备离线
			deviceInfo.setDeviceState("0");
			deviceInfoMapper.updateDeviceInfo(deviceInfo);
			try {
				isStatusChange(deviceInfo, "deviceState");
			} catch (Exception e) {
				updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
			}
			break;
		case "close": // 阀门关闭
			deviceInfo.setValveState("0");
			try {
				isStatusChange(deviceInfo, "valveState");
			} catch (Exception e) {
				updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
			}
			deviceInfoMapper.updateDeviceInfo(deviceInfo);
			break;
		case "open": // 阀门打开
			deviceInfo.setValveState("1");
			try {
				isStatusChange(deviceInfo, "valveState");
			} catch (Exception e) {
				updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
			}
			deviceInfoMapper.updateDeviceInfo(deviceInfo);
			break;
		case "data_failed": // 读表失败
			deviceInfo.setReadState("1");
			deviceInfoMapper.updateDeviceInfo(deviceInfo);
			break;
		case "error": // 设备故障
			deviceInfo.setDeviceState("2");
			deviceInfoMapper.updateDeviceInfo(deviceInfo);
			try {
				isStatusChange(deviceInfo, "deviceState");
			} catch (Exception e) {
				updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
			}
			break;
		case "data": // 读表成功
			deviceInfo.setDeviceState("1");
			// 能收到读表data数据，说明读表成功
			deviceInfo.setReadState("0");
			// 设置使用量，只写入用量大于上一次的情况
			try {
				deviceInfo = setDataUsed(deviceInfo, dataUsed);
				updateDeviceLogger.log(Level.INFO, "更新设备用量" + deviceInfo.toString());
			} catch (Exception e) {
				updateDeviceLogger.log(Level.SEVERE, "更新设备用量出错" + deviceInfo.toString(), e);
			}
			// 更新mysql
			deviceInfoMapper.updateDeviceInfo(deviceInfo);
			// 通知业务层
			try {
				isStatusChange(deviceInfo, "deviceState");
			} catch (Exception e) {
				updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
			}
			// 设置夜间用量
			try {
				sendToUsage(messFromGatewayBean);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "sendTousage异常：", e);
			}
			// 更新至redis
			try {
				// 发送至缓存
				redisService.sendToCJoy(messFromGatewayBean.getDeviceId(), messFromGatewayBean.toString());
			} catch (Exception e) {
				updateDeviceLogger.log(Level.SEVERE, messFromGatewayBean.toString(), e);
			}
			break;
		case "online": // 设备上线
		case "keepalive":
		case "push":
			// 收到以上四种事件，说明设备在线
			deviceInfo.setDeviceState("1");
			try {
				isStatusChange(deviceInfo, "deviceState");
			} catch (Exception e) {
				updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
			}
			deviceInfoMapper.updateDeviceInfo(deviceInfo);
			break;
		}

	}

	/**
	 * 判断用量，是否大于上一次，如果大于，给对象赋值返回；不大于，不处理
	 * 
	 * @param deviceInfo
	 * @param dataUsedNow
	 * @return
	 */
	public DeviceInfo setDataUsed(DeviceInfo deviceInfo, String dataUsedNow) {
		DeviceInfo localDevice = deviceInfoMapper.getOne(deviceInfo.getDeviceId());
		String dataUsedLocal = localDevice.getDataUsed();
		updateDeviceLogger.log(Level.INFO, "更新设备用量，上一次用量为：" + dataUsedLocal + "本次用量为：" + dataUsedNow);
		if (EmptyUtils.isEmpty(dataUsedLocal)) {
			deviceInfo.setDataUsed(dataUsedNow);
			return deviceInfo;
		}
		int i = dataUsedLocal.compareTo(dataUsedNow);
		if (i < 0) {
			deviceInfo.setDataUsed(dataUsedNow);
		}
		return deviceInfo;
	}

	/**
	 * 更新设备状态； 状态有变化，通知业务层 { "type”: "1" // 0:表计设备 1：网关设备 "deviceId" :
	 * "201703001320", "status" : "2", // 设备状态（包含网关） 0：离线 1：在线 2：故障 "valveStatus" :
	 * "1" // 阀门状态 0：关 1：开 } http://39.106.25.214/api/DeviceController/updateStatus
	 * 
	 * @param deviceInfo
	 * @param param
	 *            deviceState , valveState
	 */
	public void isStatusChange(DeviceInfo deviceInfo, String param) {
		// 先判断redis中是否有此数据
		try {
			boolean exist = redisService.isExist(deviceInfo.getDeviceId());
			if (!exist)
				return;
			DeviceInfo localDevice = deviceInfoMapper.getOne(deviceInfo.getDeviceId());
			if (EmptyUtils.isEmpty(localDevice))
				return;
			String type = null;
			type = localDevice.getDeviceId().equals(localDevice.getGatewayId()) ? "1" : "0"; // 网关:1 设备:0
			JSONObject statusJson = new JSONObject();
			statusJson.put("type", type);
			statusJson.put("deviceId", deviceInfo.getDeviceId());
			if ("deviceState".equals(param)) {
				if (!deviceInfo.getDeviceState().equals(localDevice.getDeviceState())) {
					// 设备状态发生变更，通知业务
					statusJson.put("status", deviceInfo.getDeviceState());
					updateDeviceLogger.log(Level.INFO, "设备状态发生变更isStatusChange" + statusJson.toString());
					HttpClient.sendPost(updateStatusUrl, statusJson.toJSONString());
				}
			} else if ("valveState".equals(param)) {
				if (!deviceInfo.getValveState().equals(localDevice.getValveState())) {
					// 阀门状态发生变更，通知业务
					statusJson.put("valveStatus", deviceInfo.getValveState());
					updateDeviceLogger.log(Level.INFO, "设备状态发生变更isStatusChange" + statusJson.toString());
					HttpClient.sendPost(updateStatusUrl, statusJson.toJSONString());
				}
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "isStatusChange 出错" + deviceInfo.toString(), e);
		}

	}

	/**
	 * 分析凌晨设备用水量，发送至mysql的usage表中
	 * 
	 * @param messFromGatewayBean
	 */
	public void sendToUsage(MsgFromGatewayBean messFromGatewayBean) {

		// 开始判断凌晨用水情况
		try {
			// 获取date时间
			String time = messFromGatewayBean.getDatetime();
			int currenHour = TimeTools.timestampToHour(Long.valueOf(time));
			// 每天清空mysql;重要！！寫在定時任務中
			// 【3】凌晨0点到6点：整点统计用水量
			if ((currenHour >= 23) || (currenHour >= 0 && currenHour < 6)) {
				String deviceId = messFromGatewayBean.getDeviceId();
				String deviceType = messFromGatewayBean.getType();
				String totaldata = messFromGatewayBean.getData();
				// 判断类型为水表的数据
				if ("3200".equals(deviceType) || "3201".equals(deviceType) || "32".equals(deviceType)) {
					// 每个整点都进行数据统计，如果mysql中有记录则更新，无记录则插入，手动更新时间
					UsageHour usageHour = new UsageHour();
					usageHour.setDeviceId(deviceId);
					// 手动更新时间（防止出现数据无修改情况下，mysql不自动更新时间）;存入时间改为设备自带的时间；
					String deviceTime = TimeTools.timestampToDate(Long.valueOf(time));
					usageHour.setDeviceTime(Timestamp.valueOf(deviceTime));
					if (currenHour >= 23) {
						// 0点插入，值到zero,one，two，three，four，five，six；
						usageHour.setZero(totaldata);
					} else if (currenHour >= 0 && currenHour < 6) {
						// 0点后，插入之前，先判断前一整点的值是否为空；如果为空，初始化所有时间点数据，如果不为空初始化后续时间点数据；
						UsageHour selectResult = deviceInfoMapper.getOneUsageHour(deviceId);
						if (StringUtils.isEmpty(selectResult)) {
							// 上一次結果爲空，初始化
							usageHour.setUsageByHour(currenHour, totaldata);
						} else {
							// 結果不爲空，判斷上一小時是否有數據
							String lastusage = selectResult.getUsageByHour(currenHour);
							if (StringUtils.isEmpty(lastusage) || "".equals(lastusage) || lastusage.length() == 0) {
								// 初始化所有數據
								usageHour.setUsageByHour(currenHour, totaldata);
							} else {
								// 上一次数据和此次对比，结果返回1，表示上次数据大于这次数据，不合理，判断为异常
								int comp = new BigDecimal(lastusage).compareTo(new BigDecimal(totaldata));
								usageHour.setStatus(comp == 1 ? "1" : "0"); // 更新状态为1：异常 0：正常
								// 后续时间点数据的初始化
								usageHour.setUsageByHour(currenHour + 1, totaldata);
							}
						}
					}
					// 插入mysql
					deviceInfoMapper.insertIntoUsageHour(usageHour);
					usageHourLog.log(Level.INFO, usageHour.toString());
				}
			}
		} catch (Exception e) {
			usageHourLog.log(Level.INFO, e + "凌晨用水统计方法异常" + messFromGatewayBean.toString());
		}

	}

	/**
	 * 根据参数获取离线设备
	 * 
	 * @param data
	 */
	@Override
	public List<HashMap<String, Object>> getOffline(String data) {
		if (StringUtils.isEmpty(data))
			return null;
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
	 * 
	 * @param data
	 * @return
	 */
	@Override
	public List<HashMap<String, Object>> getReadFailed(String data) {
		if (StringUtils.isEmpty(data))
			return null;
		logger.log(Level.INFO, data);
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
	 * 
	 * @param data
	 * @return
	 */
	@Override
	public List<HashMap<String, Object>> getUsageStatusFailed(String data) {
		if (StringUtils.isEmpty(data))
			return null;
		logger.log(Level.INFO, data);
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
	 * 
	 * @param data
	 * @return
	 */
	@Override
	public String getDeviceEvenFromDruid(String data) {
		if (StringUtils.isEmpty(data))
			return null;
		JSONObject jsonData = JSONObject.parseObject(data);
		String deviceId = jsonData.getString("deviceId");
		String event = jsonData.getString("event");
		String datetime1 = jsonData.getString("datetime1");
		String datetime2 = jsonData.getString("datetime2");
		// 非空判断，时间减去8小时再查询，开始时间与结束时间大小比较
		// 条件拼接
		StringBuffer sql = new StringBuffer();

		if (!(StringUtils.isEmpty(deviceId) || deviceId.length() == 0)) {
			sql.append("where deviceId = '" + deviceId + "' ");
		}
		if (!(StringUtils.isEmpty(event) || event.length() == 0)) {
			sql.append(!(StringUtils.isEmpty(sql) || sql.length() == 0) ? " and " : "  where ")// 如果sql不为空，则为多条件
					.append("event = '" + event + "' ");
		}
		if (!(StringUtils.isEmpty(datetime1) || datetime1.length() == 0)
				&& !(StringUtils.isEmpty(datetime2) || datetime2.length() == 0)) {
			// 转时差
			// 获取时间,前一天的16点为真实时间的0点，进行拼接：格式：2018-05-03T16
			String startTime = TimeTools.getSpecifiedDayBefore(datetime1) + "T16";
			// 结束时间以明天为准，减去8小时
			String endTime = TimeTools.getSpecifiedDayAfter(datetime2) + "T16";
			sql.append(!(StringUtils.isEmpty(sql) || sql.length() == 0) ? " and " : "  where  ")// 如果sql不为空，则为多条件
					.append("__time >= '" + startTime + "' and  __time <= '" + endTime + "' ");
		}
		String QUERY_HIST_DATA = "{\"query\":\"select deviceId ,serverId ,event ,eventinfo ,data,( __time + INTERVAL '8' HOUR) as utf8time   from dataInfo   "
				+ sql.toString() + "  order by __time desc limit 500 \"}";

		try {
			String result = HttpClient.sendPost(queryUrl, QUERY_HIST_DATA);
			return result;
		} catch (Exception e) {
			logger.log(Level.SEVERE, QUERY_HIST_DATA, e);
			return null;
		}
	}

	/**
	 * 查询某天可疑用水的水表,用量
	 *
	 * SELECT max("currentdata") as maxUse ,deviceId FROM "watermeter" WHERE
	 * "__time" >= CURRENT_TIMESTAMP - INTERVAL '7' DAY and currentdata > 0 group by
	 * "deviceId" order by max("currentdata") desc
	 *
	 * 对时间进行处理，获取的时间需加 减去 8小时
	 * 
	 * @param
	 * @return
	 */
	@Override
	public String getWaterMeterFromDruid(String time) {
		// 获取时间,前一天的16点为真实时间的0点，进行拼接：格式：2018-05-03T16
		String startTime = TimeTools.getSpecifiedDayBefore(time) + "T16";
		// 后一天时间
		String endTime = time + "T16";

		String QUERY_WATER_DATA = "{\"query\":\"select deviceId ,max(currentdata) as maxUse   from  watermeter where  __time >='"
				+ startTime + "' and __time <='" + endTime
				+ "' and  currentdata > 0  group by deviceId order by max(currentdata) desc limit 500 \"}";
		try {
			String result = HttpClient.sendPost(queryUrl, QUERY_WATER_DATA);
			// 将json转为对象，遍历每个对象，再增加设备得项目信息
			List<WaterMeterUse> waterMeterUses = JSONObject.parseArray(result, WaterMeterUse.class);
			// 循环遍历每个对象，通过id查出项目地信息，加到对象中
			for (WaterMeterUse waterMeterUse : waterMeterUses) {
				// 查询
				DeviceInfo deviceInfo = deviceInfoMapper.getOne(waterMeterUse.getDeviceId());
				if (StringUtils.isEmpty(deviceInfo)) {
					logger.log(Level.SEVERE, "查询设备" + waterMeterUse.getDeviceId() + "结果为空");
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

			return s;
		} catch (Exception e) {
			logger.log(Level.SEVERE, QUERY_WATER_DATA, e);
			return null;
		}
	}

	// 查询最近7天可疑用水的水表,频率
	@Override
	public String getWaterMeterCountFromDruid() {
		String QUERY_WATER_DATA = "{\"query\":\"select deviceId ,count(1) as useCount   from  watermeter where  __time >= CURRENT_TIMESTAMP - INTERVAL '7' DAY and  currentdata > 0  group by deviceId order by count(1) desc limit 500 \"}";

		try {
			String result = HttpClient.sendPost(queryUrl, QUERY_WATER_DATA);
			return result;
		} catch (Exception e) {
			logger.log(Level.SEVERE, QUERY_WATER_DATA, e);
			return null;
		}
	}

	/**
	 * 查询异常水表,新数据data小于老数据data，currentdata标记为-1的水表
	 * 
	 * @return
	 */
	@Override
	public String getExceptionWaterMeter() {
		String QUERY_WATER_DATA = "{\"query\":\"select deviceId,count(1) as exceptCount  from  watermeter where  __time >= CURRENT_TIMESTAMP - INTERVAL '7' DAY  and  currentdata = '-1' group by deviceId order by exceptCount desc limit 500 \"}";
		try {
			String result = HttpClient.sendPost(queryUrl, QUERY_WATER_DATA);
			return result;
		} catch (Exception e) {
			logger.log(Level.SEVERE, QUERY_WATER_DATA, e);
			return null;
		}
	}

	/**
	 * 根据deviceId查询watermeter用水情况列表
	 * 
	 * @param deviceId
	 * @return
	 */
	@Override
	public String getDeviceInfoFromDruid(String deviceId) {
		String QUERY_WATER_DATA = "{\"query\":\"select deviceId,currentdata,totaldata,( __time + INTERVAL '8' HOUR) as utf8time  from  watermeter where  __time >= CURRENT_TIMESTAMP - INTERVAL '7' DAY  and deviceId = '"
				+ deviceId + "'   order by __time  limit 5000 \"}";
		try {
			String result = HttpClient.sendPost(queryUrl, QUERY_WATER_DATA);
			return result;
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
		if (StringUtils.isEmpty(data))
			return null;
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
	 * 注册设备相关信息 增加更新逻辑判断
	 *
	 * @param
	 */
	@Override
	public Map<String, Object> register(DeviceInfo deviceInfo) {
		if (StringUtils.isEmpty(deviceInfo.getDeviceId()) || StringUtils.isEmpty(deviceInfo.getGatewayId())
				|| StringUtils.isEmpty(deviceInfo.getProject()) || StringUtils.isEmpty(deviceInfo.getProvince())
				|| StringUtils.isEmpty(deviceInfo.getCity()) || StringUtils.isEmpty(deviceInfo.getDistrict())
				|| StringUtils.isEmpty(deviceInfo.getCommunity()) || StringUtils.isEmpty(deviceInfo.getAddress())
				|| StringUtils.isEmpty(deviceInfo.getValveId()))

			/**
			 * || StringUtils.isEmpty(deviceInfo.getCategory()) ||
			 * StringUtils.isEmpty(deviceInfo.getValveProtocol())||
			 * StringUtils.isEmpty(deviceInfo.getDeviceProtocol()) ||
			 * StringUtils.isEmpty(deviceInfo.getGatewayUrl()
			 */
			return ResultUtil.error(400, "Unexpected param");

		try {
			DeviceInfo localDevice = deviceInfoMapper.getOne(deviceInfo.getDeviceId());
			if (localDevice == null) {
				// 注册设备
				deviceInfoMapper.insert(deviceInfo);
			} else {
				// 更新设备逻辑：当内容发生变更时，才去update
				if (!deviceInfo.equals(localDevice)) {
					deviceInfoMapper.updateDeviceInfo(deviceInfo);
					registerLogger.log(Level.INFO, deviceInfo.toString());
				}
			}
		} catch (Exception e) {
			registerLogger.log(Level.SEVERE, deviceInfo.toString(), e);
		}
		return ResultUtil.success();
	}

	// 删除设备
	@Override
	public void deleteDeviceInfoById(String deviceId) {
		if ("".equals(deviceId) || StringUtils.isEmpty(deviceId)) {
			return;
		}
		try {
			logger.log(Level.SEVERE, "删除设备：" + deviceId);
			deviceInfoMapper.deleteDeviceInfoById(deviceId);
		} catch (Exception e) {

		}
	}

	// 更新设备信息
	@Override
	public void updateDeviceInfo(DeviceInfo deviceInfo) {
		if ("".equals(deviceInfo) || StringUtils.isEmpty(deviceInfo)) {
			return;
		}
		try {
			logger.log(Level.INFO, "更新设备：" + deviceInfo);
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
				|| StringUtils.isEmpty(deviceInfo.getSimState()) || StringUtils.isEmpty(deviceInfo.getDataUsed()))
			return;
		try {
			deviceInfoMapper.updateDeviceInfo(deviceInfo);
		} catch (Exception e) {
			updateSimLogger.log(Level.SEVERE, deviceInfo.toString(), e);
		}
	}

}
