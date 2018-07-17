package com.joymeter.provider;

import com.joymeter.entity.UsageHour;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import com.joymeter.entity.DeviceInfo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceInfoProvider {
	/**
	 * 动态生成更新数据SQL
	 * 
	 * @param deviceInfo
	 * @return
	 */
	public String updateDeviceInfo(DeviceInfo deviceInfo) {
		//手动更新时间（防止出现数据无修改情况下，mysql不自动更新时间）
		Date date = new Date();
		String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);//将时间格式转换成符合Timestamp要求的格式.
		Timestamp updatetime =Timestamp.valueOf(nowTime);//把时间转换
		deviceInfo.setUpdateTime(updatetime);

		String sql = new SQL() {
			{
				UPDATE("device_info");
				if (!StringUtils.isEmpty(deviceInfo.getGatewayId())) {
					SET("gatewayId = #{gatewayId}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getProject())) {
					SET("project = #{project}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getProvince())) {
					SET("province = #{province}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getCity())) {
					SET("city = #{city}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getDistrict())) {
					SET("district = #{district}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getCommunity())) {
					SET("community = #{community}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getAddress())) {
					SET("address = #{address}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getDeviceState())) {
					SET("deviceState = #{deviceState}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getValveState())) {
					SET("valveState = #{valveState}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getReadState())) {
					SET("readState = #{readState}");
					if (deviceInfo.getReadState() == "1") {
						SET("readFaile = readFaile+1");
					}
				}
				if (!StringUtils.isEmpty(deviceInfo.getCategory())) {
					SET("category=#{category}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getValveId())) {
					SET("valveId=#{valveId}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getValveProtocol())) {
					SET("valveProtocol=#{valveProtocol}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getDeviceProtocol())) {
					SET("deviceProtocol=#{deviceProtocol}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getSimId())) {
					SET("simId=#{simId}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getSimState())) {
					SET("simState=#{simState}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getDataUsed())) {
					SET("dataUsed=#{dataUsed}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getDeviceId())) {
					//如果更新事件是offline，也就是deviceState=0，则不修改updatetime；反之修改
					if("0".equals(deviceInfo.getDeviceState())){
						WHERE("deviceId = #{deviceId}");
					}else {
						SET("updateTime=#{updateTime}");
						WHERE("deviceId = #{deviceId}");
					}

				}
			}
		}.toString();
		return sql ;
	}
	/**
	 * 动态生成查询数据SQL
	 * 
	 * @param deviceInfo
	 * @return
	 */
	public String selectByParams(DeviceInfo deviceInfo) {
		return new SQL() {
			{
				SELECT("*");
				FROM("device_info");
				if (!StringUtils.isEmpty(deviceInfo.getProject())) {
					WHERE("project = #{project}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getProvince())) {
					WHERE("province = #{province}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getCity())) {
					WHERE("city = #{city}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getDistrict())) {
					WHERE("district = #{district}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getCommunity())) {
					WHERE("community = #{community}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getDeviceState())) {
					WHERE("deviceState = #{deviceState}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getReadState())) {
					WHERE("readState = #{readState}");
				}
			}
		}.toString();
	}


	/**
	 * 动态生成可疑用水列表查询
	 *
	 * @param deviceInfo
	 * @return
	 */
	public String selectUsageWithProjectByParams(DeviceInfo deviceInfo) {
		return new SQL() {
			{
				SELECT("t2.project,t2.province,t2.city,t2.district,t2.community,t2.address,t1.deviceId,ROUND((t1.one - t1.zero ),3) as ones,ROUND((t1.two - t1.one ),3) as twos,ROUND((t1.three - t1.two ),3) as threes,ROUND((t1.four - t1.three ),3) as fours,ROUND((t1.five - t1.four ),3) as fives,ROUND((t1.six - t1.five ),3) as sixs   ");
				FROM("usage_hour t1,device_info t2");
				WHERE("t1.deviceId = t2.deviceId ");
				//可疑标识
				WHERE("t1.one > t1.zero");
				WHERE("t1.two > t1.one");
				WHERE("t1.three > t1.two");
				WHERE("t1.four > t1.three");
				WHERE("t1.five > t1.four");
				WHERE("t1.six > t1.five");
				if (!StringUtils.isEmpty(deviceInfo.getProject())) {
					WHERE("t2.project = #{project}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getProvince())) {
					WHERE("t2.province = #{province}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getCity())) {
					WHERE("t2.city = #{city}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getDistrict())) {
					WHERE("t2.district = #{district}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getCommunity())) {
					WHERE("t2.community = #{community}");
				}

			}
		}.toString();
	}



	/**
	 * 动态生成查询数量SQL
	 * 
	 * @param deviceInfo
	 * @return
	 */
	public String selectcount(DeviceInfo deviceInfo) {
		return new SQL() {
			{
				SELECT("count(*)");
				FROM("device_info");
				if (!StringUtils.isEmpty(deviceInfo.getProject())) {
					WHERE("project = #{project}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getProvince())) {
					WHERE("province = #{province}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getCity())) {
					WHERE("city = #{city}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getDistrict())) {
					WHERE("district = #{district}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getCommunity())) {
					WHERE("community = #{community}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getAddress())) {
					WHERE("address = #{address}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getDeviceState())) {
					WHERE("deviceState = #{deviceState}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getReadState())) {
					WHERE("readState = #{readState}");
				}
				if (!StringUtils.isEmpty(deviceInfo.getValveState())) {
					WHERE("valveState = #{valveState}");
				}
			}
		}.toString();
	}

	/**
	 * 动态生成查询离线数量聚合SQL
	 * 
	 * @param deviceInfo
	 * @return
	 */
	public String selectoffline(DeviceInfo deviceInfo) {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlb = new StringBuilder();
		String column = null;
		sql.append("SELECT ");
		sqlb.append(",COUNT(*) as offline from device_info WHERE deviceState = '0' ");
		if (!StringUtils.isEmpty(deviceInfo.getProject())) {
			sqlb.append("and project = #{project} ");
			column = "province";
			if (!StringUtils.isEmpty(deviceInfo.getProvince())) {
				sqlb.append("and province = #{province} ");
				column = "city";
				if (!StringUtils.isEmpty(deviceInfo.getCity())) {
					sqlb.append("and city = #{city} ");
					column = "district";
					if (!StringUtils.isEmpty(deviceInfo.getDistrict())) {
						sqlb.append("and district = #{district} ");
						column = "community";
					}
				}
			}
		} else {
			column = "project";
		}
		return sql.append(column).append(sqlb.append(" Group By " + column + " order by offline desc ")).toString();
	}

	/**
	 * 动态生成查询抄表失败聚合SQL
	 * readState = 1 为失败
	 * @param deviceInfo
	 * @return
	 */
	public String selectReadFailed(DeviceInfo deviceInfo) {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlb = new StringBuilder();
		String column = null;
		sql.append("SELECT ");
		sqlb.append(",COUNT(*) as failed from device_info WHERE readState = '1' ");
		if (!StringUtils.isEmpty(deviceInfo.getProject())) {
			sqlb.append("and project = #{project} ");
			column = "province";
			if (!StringUtils.isEmpty(deviceInfo.getProvince())) {
				sqlb.append("and province = #{province} ");
				column = "city";
				if (!StringUtils.isEmpty(deviceInfo.getCity())) {
					sqlb.append("and city = #{city} ");
					column = "district";
					if (!StringUtils.isEmpty(deviceInfo.getDistrict())) {
						sqlb.append("and district = #{district} ");
						column = "community";
					}
				}
			}
		} else {
			column = "project";
		}
		return sql.append(column).append(sqlb.append(" Group By " + column +  " order by failed desc ")).toString();
	}


	/**
	 * 查询可疑用水聚合SQL
	 * status = 1 为可疑
	 * 可疑用水：凌晨使用量不为0
	 * six > one
	 * @param deviceInfo
	 * @return
	 */
	public String selectUsageStatusFailed(DeviceInfo deviceInfo) {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlb = new StringBuilder();
		String column = null;
		sql.append("SELECT ");
		sqlb.append(",COUNT(t1.deviceId) as failed from usage_hour t1,device_info t2  WHERE  t1.deviceId = t2.deviceId  and t1.one > t1.zero and t1.two > t1.one and t1.three > t1.two and t1.four > t1.three and t1.five > t1.four and t1.six > t1.five  ");
		if (!StringUtils.isEmpty(deviceInfo.getProject())) {
			sqlb.append("and t2.project = #{project} ");
			column = "t2.province";
			if (!StringUtils.isEmpty(deviceInfo.getProvince())) {
				sqlb.append("and t2.province = #{province} ");
				column = "t2.city";
				if (!StringUtils.isEmpty(deviceInfo.getCity())) {
					sqlb.append("and t2.city = #{city} ");
					column = "t2.district";
					if (!StringUtils.isEmpty(deviceInfo.getDistrict())) {
						sqlb.append("and t2.district = #{district} ");
						column = "t2.community";
					}
				}
			}
		} else {
			column = "t2.project";
		}
		String temp = sql.append(column).append(sqlb.append(" Group By " + column +  " order by failed desc ")).toString();
		return  temp;
	}


	/**
	 * 根据设备id，整点用量到usage_hour表中，存在设备则更新，不存在则新增；
	 * 每次新增最多同时只有一个用量字段
	 *  防止最后有空数据：
	 * 		 每次收到数据后，先查询数据库中上一小时是否有记录；
	 * 		如果无记录，初始化所有小时的数据；
	 * 		如果有记录，初始化后面小时的数据
	 * @param usageHour
	 * @return
	 */
	public String insertIntoUsageHour(UsageHour usageHour){

		//手动更新时间（防止出现数据无修改情况下，mysql不自动更新时间）
		Date date = new Date();
		String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date);//将时间格式转换成符合Timestamp要求的格式.
		Timestamp updatetime =Timestamp.valueOf(nowTime);//把时间转换
		usageHour.setUpdateTime(updatetime);



		//设备编号不为空
		if(!StringUtils.isEmpty(usageHour.getDeviceId())){
			if(!StringUtils.isEmpty(usageHour.getZero())){
				//0点，收到信息后，将后续时间点用量都初始化，防止后续出现空值情况
				String sql ="INSERT INTO usage_hour(deviceId,zero,one,two,three,four,five,six,status,deviceTime,updateTime) VALUE(#{deviceId},#{zero},#{zero},#{zero},#{zero},#{zero},#{zero},#{zero},#{status},#{deviceTime},#{updateTime}) ON DUPLICATE KEY UPDATE zero=#{zero}, one = #{zero},two = #{zero},three = #{zero},four = #{zero},five = #{zero},six = #{zero},status = #{status} ,deviceTime = #{deviceTime} ,updateTime=#{updateTime};";
				return sql;
			}else if(!StringUtils.isEmpty(usageHour.getOne())){
			//1点,
				String sql ="INSERT INTO usage_hour(deviceId,one,two,three,four,five,six,status,deviceTime,updateTime) VALUE(#{deviceId},#{one},#{one},#{one},#{one},#{one},#{one},#{status},#{deviceTime},#{updateTime}) ON DUPLICATE KEY UPDATE one = #{one},two = #{one},three = #{one},four = #{one},five = #{one},six = #{one},status = #{status} ,deviceTime = #{deviceTime} ,updateTime=#{updateTime};";
				return sql;
			}else if(!StringUtils.isEmpty(usageHour.getTwo())){
			//2点
				String sql ="INSERT INTO usage_hour(deviceId,two,three,four,five,six,status,deviceTime,updateTime) VALUE(#{deviceId},#{two},#{two},#{two},#{two},#{two},#{status},#{deviceTime},#{updateTime}) ON DUPLICATE KEY UPDATE two= #{two},three = #{two},four = #{two},five = #{two},six = #{two},status = #{status} ,deviceTime = #{deviceTime} ,updateTime=#{updateTime};";
				return sql;
			}else if(!StringUtils.isEmpty(usageHour.getThree())){
			//3点
				String sql ="INSERT INTO usage_hour(deviceId,three,four,five,six,status,deviceTime,updateTime) VALUE(#{deviceId},#{three},#{three},#{three},#{three},#{status},#{deviceTime},#{updateTime}) ON DUPLICATE KEY UPDATE three= #{three},four = #{three},five = #{three},six = #{three} , status = #{status} ,deviceTime = #{deviceTime} ,updateTime=#{updateTime};";
				return sql;
			}else if(!StringUtils.isEmpty(usageHour.getFour())){
			//4点
				String sql ="INSERT INTO usage_hour(deviceId,four,five,six,status,deviceTime,updateTime) VALUE(#{deviceId},#{four},#{four},#{four},#{status},#{deviceTime},#{updateTime}) ON DUPLICATE KEY UPDATE four= #{four},five = #{four},six = #{four},status = #{status} ,deviceTime = #{deviceTime} ,updateTime=#{updateTime};";
				return sql;
			}else if(!StringUtils.isEmpty(usageHour.getFive())){
			//5点
				String sql ="INSERT INTO usage_hour(deviceId,five,six,status,deviceTime,updateTime) VALUE(#{deviceId},#{five},#{five},#{status},#{deviceTime},#{updateTime}) ON DUPLICATE KEY UPDATE five= #{five},six = #{five},status = #{status} ,deviceTime = #{deviceTime} ,updateTime=#{updateTime};";
				return sql;
			}else if(!StringUtils.isEmpty(usageHour.getSix())){
			//6点
				String sql ="INSERT INTO usage_hour(deviceId,six,status,deviceTime,updateTime) VALUE(#{deviceId},#{six},#{status},#{deviceTime},#{updateTime}) ON DUPLICATE KEY UPDATE six= #{six},status = #{status} ,deviceTime = #{deviceTime} ,updateTime=#{updateTime};";
				return sql;
			}

		}

		return null;
	}
}
