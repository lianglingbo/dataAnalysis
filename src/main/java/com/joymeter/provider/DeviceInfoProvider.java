package com.joymeter.provider;

import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import com.joymeter.entity.DeviceInfo;

public class DeviceInfoProvider {
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
		} else {
			column = "project";
		}
		if (!StringUtils.isEmpty(deviceInfo.getProvince())) {
			sqlb.append("and province = #{province} ");
			column = "city";
		}
		if (!StringUtils.isEmpty(deviceInfo.getCity())) {
			sqlb.append("and city = #{city} ");
			column = "district";
		}
		if (!StringUtils.isEmpty(deviceInfo.getDistrict())) {
			sqlb.append("and district = #{district} ");
			column = "community";
		}
		return sql.append(column).append(sqlb.append(" Group By " + column)).toString();
	}
}
