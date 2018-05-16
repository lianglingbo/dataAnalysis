package com.joymeter.provider;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.util.logging.Level;
import java.util.logging.Logger;

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
				if (!StringUtils.isEmpty(deviceInfo.getProject())) {
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
				if (!StringUtils.isEmpty(deviceInfo.getCommunity())) {
					SET("address = #{address}");
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
			}
		}.toString();
	}

	/**
	 * 动态生成查询离线数量SQL
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
		return sql.append(column).append(sqlb.append(" Group By " + column)).toString();
	}
}
