package com.joymeter.provider;

import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import com.joymeter.entity.DeviceInfo;

public class DeviceInfoProvider {
	/**
	 * 动态生成更新数据SQL
	 * 
	 * @param deviceInfo
	 * @return
	 */
	public String updateDeviceInfo(DeviceInfo deviceInfo) {
		return new SQL() {
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
					if(deviceInfo.getReadState()=="1") {
						SET("readFaile = readFaile+1");
					}
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
					WHERE("deviceId = #{deviceId}");
				}
			}
		}.toString();
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
	 * 态生成查询抄表失败聚合SQL
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
		String resultSql = sql.append(column).append(sqlb.append(" Group By " + column +  " order by failed desc ")).toString();
		System.out.println(resultSql);
		return resultSql;
	}
}
