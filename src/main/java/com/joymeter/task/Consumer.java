package com.joymeter.task;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.joymeter.cache.DataCache;
import com.joymeter.entity.DeviceInfo;
import com.joymeter.entity.MsgFromGatewayBean;
import com.joymeter.entity.UsageHour;
import com.joymeter.mapper.DeviceInfoMapper;
import com.joymeter.service.RedisService;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;
import com.joymeter.util.TimeTools;
import com.joymeter.util.common.EmptyUtils;
import com.joymeter.util.common.SpringBean;
/**
 *RocketMQ消费者
 * @author Pan Shuoting
 *
 */
public class Consumer {
	private DeviceInfoMapper deviceInfoMapper = SpringBean.getBean(DeviceInfoMapper.class);
	private RedisService redisService = (RedisService) SpringBean.getBean(RedisService.class);

	private static Consumer consumer = null;

	private static final Logger logger = Logger.getLogger(Consumer.class.getName());
	private static final Logger updateDeviceLogger = Logger.getLogger("updateDevice");
	private static final Logger addDataLogger = Logger.getLogger("addData");
	private static final Logger usageHourLog = Logger.getLogger("usageHourLog");

	private static String updateStatusUrl = PropertiesUtils.getProperty("updateStatusUrl", "");
	private static String namesrvAddr = PropertiesUtils.getProperty("namesrvAddr", "");
	private static String instanceName = PropertiesUtils.getProperty("instanceName", "");
	private static String topic = PropertiesUtils.getProperty("topic", "");
	private static String subExpression = PropertiesUtils.getProperty("subExpression", "");
	
	/**
	 * 单例
	 * @return
	 */
	public static Consumer GetInstance() {
		if(consumer==null) {
			consumer  = new Consumer();
		}
		return consumer;
	}
	
	/**
	 * 1.保存数据到Druid, 数据结构: {"serverId":"001","deviceId":"12345678",
	 * "type":"1","event":"data","data":"","datetime":"1513576307290","msg":"msg"}
	 * 2.数据源新增字段，eventinfo，记录event事件的data值（为字符串类型的时候，不能存入druid，druid中设置data属性为double类型）
	 *   数据源新增时，判断事件，如果event 不为 data ，则将其data值存入eventinfo中
	 * 3.新增方法setusageHour() ,mysql新增表：usage_hour；统计每日凌晨0到6点，每个小时的用水量；
	 * 	 每天凌晨，每个整点写入一次数据，最后生成完整信息；mysql中只存当天数据，每天统计结束后，将信息放入druid中；
	 * 	 防止最后有空数据：每次收到数据后，把后面几个小时内容都填充；如果上一小时数据为空，填充所有
	 *   每天定时，清空usage_hour表
	 *   最后把记录存进druid
	 *
	 * 4.设备的状态变更和阀门状态变化  需要通知业务层
	 * @param dataStr
	 */
	public  void  addData(String dataStr) {

		if (EmptyUtils.isEmpty(dataStr))  return;
		try {
 			JSONObject jsonData = JSONObject.parseObject(dataStr);
			MsgFromGatewayBean messFromGatewayBean= new MsgFromGatewayBean(jsonData);
			//内容非空校验
			if(messFromGatewayBean.isEmpty())  {
				addDataLogger.log(Level.SEVERE, "接收的内容有空值"+dataStr);
				return;
			}

			//过滤事件，发送至mysql
			eventFilter(messFromGatewayBean);
			//发送数据到druid中
			//获得的json数据格式增加了msg信息，将msg存入druid时，对应的字段为eventInfo
			if(dataStr.contains("msg")){
				dataStr = dataStr.replace("msg","eventinfo");
			}
			DataCache.add(dataStr);
			addDataLogger.log(Level.INFO, dataStr);
 		} catch (Exception e) {
			addDataLogger.log(Level.SEVERE, "add接口异常"+dataStr, e);
		}

	}


	/**
	 * 分析设备的状态，更新抄表状态、设备状态、阀门状态发送至mysql的deviceInfo表中
	 * @param messFromGatewayBean
	 */
	public  void eventFilter(MsgFromGatewayBean messFromGatewayBean){
		String event = messFromGatewayBean.getEvent();
		String deviceId = messFromGatewayBean.getDeviceId();
		String dataUsed = messFromGatewayBean.getData();
		//更新抄表状态、设备状态、阀门状态 ，deviceState , valveState
		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.setDeviceId(deviceId);
		switch (event) {
			case "offline":  //设备离线
				deviceInfo.setDeviceState("0");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				try{
					isStatusChange(deviceInfo,"deviceState");
				}catch (Exception e){
					updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
				}
 				break;
			case "close":    //阀门关闭
				deviceInfo.setValveState("0");
				try{
					isStatusChange(deviceInfo,"valveState");
				}catch (Exception e){
					updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
				}
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				break;
			case "open":     //阀门打开
				deviceInfo.setValveState("1");
				try{
					isStatusChange(deviceInfo,"valveState");
				}catch (Exception e){
					updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
				}
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				break;
			case "data_failed":  //读表失败
				deviceInfo.setReadState("1");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				break;
			case "error":  //设备故障
				deviceInfo.setDeviceState("2");
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				try{
					isStatusChange(deviceInfo,"deviceState");
				}catch (Exception e){
					updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
				}
				break;
			case "data":       //读表成功
				deviceInfo.setDeviceState("1");
				//能收到读表data数据，说明读表成功
				deviceInfo.setReadState("0");
				//设置使用量，只写入用量大于上一次的情况
				try{
					 deviceInfo = setDataUsed(deviceInfo, dataUsed);
					updateDeviceLogger.log(Level.INFO,"更新设备用量"+ deviceInfo.toString());
				}catch (Exception e){
					updateDeviceLogger.log(Level.SEVERE, "更新设备用量出错"+deviceInfo.toString(), e);
				}
				//更新mysql
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				//通知业务层
				try{
					isStatusChange(deviceInfo,"deviceState");
				}catch (Exception e){
					updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
				}
				//设置夜间用量
				try{
					sendToUsage(messFromGatewayBean);
				}catch (Exception e){
					logger.log(Level.SEVERE, "sendTousage异常：", e);
				}
				//更新至redis
				try{
					//发送至缓存
					redisService.sendToCJoy(messFromGatewayBean.getDeviceId(),messFromGatewayBean.toString());
				}catch (Exception e) {
					updateDeviceLogger.log(Level.SEVERE, messFromGatewayBean.toString(), e);
				}
 				break;
			case "online":   //设备上线
			case "keepalive":
			case "push":
				//收到以上四种事件，说明设备在线
				deviceInfo.setDeviceState("1");
				try{
					isStatusChange(deviceInfo,"deviceState");
				}catch (Exception e){
					updateDeviceLogger.log(Level.SEVERE, deviceInfo.toString(), e);
				}
				deviceInfoMapper.updateDeviceInfo(deviceInfo);
				break;
		}

	}

	/**
	 * 判断用量，是否大于上一次，如果大于，给对象赋值返回；不大于，不处理
	 * @param deviceInfo
	 * @param dataUsedNow
	 * @return
	 */
	public DeviceInfo setDataUsed(DeviceInfo deviceInfo,String dataUsedNow){
		DeviceInfo localDevice = deviceInfoMapper.getOne(deviceInfo.getDeviceId());
		String dataUsedLocal = localDevice.getDataUsed();
		updateDeviceLogger.log(Level.INFO,"更新设备用量，上一次用量为："+dataUsedLocal+"本次用量为："+dataUsedNow);
		if(EmptyUtils.isEmpty(dataUsedLocal)){
			deviceInfo.setDataUsed(dataUsedNow);
			return deviceInfo;
		}
		int i = dataUsedLocal.compareTo(dataUsedNow);
		if(i < 0){
			deviceInfo.setDataUsed(dataUsedNow);
		}
		return deviceInfo;
	}
	/**
	 * 更新设备状态；
	 * 状态有变化，通知业务层
	 * {
	 *   "type”: "1"                  // 0:表计设备  1：网关设备
	 *  "deviceId" : "201703001320",
	 *  "status" : "2",                // 设备状态（包含网关）  0：离线 1：在线 2：故障
	 *  "valveStatus" : "1"          // 阀门状态  0：关  1：开
	 * }
	 * http://39.106.25.214/api/DeviceController/updateStatus
	 * @param deviceInfo
	 * @param param   deviceState , valveState
	 */
	public void isStatusChange(DeviceInfo deviceInfo,String param){
		//先判断redis中是否有此数据
		try{
			boolean exist = redisService.isExist(deviceInfo.getDeviceId());
			if(!exist) return;
			DeviceInfo localDevice = deviceInfoMapper.getOne(deviceInfo.getDeviceId());
			if(EmptyUtils.isEmpty(localDevice)) return;
			String type = null;
			if(localDevice.getDeviceId().equals(localDevice.getGatewayId())){
				//网关
				type = "1";
			}else {
				//设备
				type = "0";
			}
			JSONObject statusJson = new JSONObject();
			statusJson.put("type",type);
			statusJson.put("deviceId",deviceInfo.getDeviceId());
			if("deviceState".equals(param)){
				if(!deviceInfo.getDeviceState().equals(localDevice.getDeviceState())){
					//设备状态发生变更，通知业务
					statusJson.put("status",deviceInfo.getDeviceState());
					updateDeviceLogger.log(Level.INFO,"设备状态发生变更isStatusChange"+statusJson.toString());
					HttpClient.sendPost(updateStatusUrl,statusJson.toJSONString());
				}
			}else if("valveState".equals(param)){
				if(!deviceInfo.getValveState().equals(localDevice.getValveState())){
					//阀门状态发生变更，通知业务
					statusJson.put("valveStatus",deviceInfo.getValveState());
					updateDeviceLogger.log(Level.INFO,"设备状态发生变更isStatusChange"+statusJson.toString());
					HttpClient.sendPost(updateStatusUrl,statusJson.toJSONString());
				}
			}

		}catch (Exception e){
			logger.log(Level.SEVERE,"isStatusChange 出错"+deviceInfo.toString(), e);
		}

	}
	
	/**
	 * 分析凌晨设备用水量，发送至mysql的usage表中
	 * @param messFromGatewayBean
	 */
	public  void sendToUsage(MsgFromGatewayBean messFromGatewayBean){

		//开始判断凌晨用水情况
		try{
			//获取date时间
			String time = messFromGatewayBean.getDatetime();
			int currenHour = TimeTools.timestampToHour(Long.valueOf(time));
			//每天清空mysql;重要！！寫在定時任務中
			//【3】凌晨0点到6点：整点统计用水量
			if((currenHour >=23 )|| (currenHour >= 0 && currenHour < 6)){
				String deviceId = messFromGatewayBean.getDeviceId();
				String deviceType = messFromGatewayBean.getType();
				String totaldata = messFromGatewayBean.getData();
				//判断类型为水表的数据
				if("3200".equals(deviceType) || "3201".equals(deviceType) || "32".equals(deviceType)){
					//每个整点都进行数据统计，如果mysql中有记录则更新，无记录则插入，手动更新时间
					UsageHour usageHour = new UsageHour();
					usageHour.setDeviceId(deviceId);
					//手动更新时间（防止出现数据无修改情况下，mysql不自动更新时间）;存入时间改为设备自带的时间；
					String deviceTime = TimeTools.timestampToDate(Long.valueOf(time));
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
			usageHourLog.log(Level.INFO,e+"凌晨用水统计方法异常"+messFromGatewayBean.toString());
		}

	}

	/**
	 * 启动消费者
	 */
	public void start() {
		DefaultMQPushConsumer mqconsumer = new DefaultMQPushConsumer("test-group");
    	try {
    		mqconsumer.setNamesrvAddr(namesrvAddr);
    		mqconsumer.setInstanceName(instanceName);
    		mqconsumer.subscribe(topic, subExpression);
			
    		mqconsumer.registerMessageListener(new MessageListenerConcurrently() {
				
				@Override
				public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
					for (MessageExt msg:msgs) {
	                    System.out.println("消费者消费数据:"+new String(msg.getBody()));
	                    addData(new String(msg.getBody()));  
					}
					return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
				}
			});
    		mqconsumer.start();
		} catch (MQClientException e) {
			logger.log(Level.SEVERE, e.toString());
		}
	}
}
