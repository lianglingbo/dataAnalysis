package com.joymeter.provider;

import org.apache.ibatis.jdbc.SQL;

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
				if (deviceInfo.getProject() != null) {
					WHERE("projectName = #{projectName}");
				}
				if (deviceInfo.getProvince() != null) {
					WHERE("province = #{province}");
				}
				if (deviceInfo.getCity() != null) {
					WHERE("city = #{city}");
				}
				if (deviceInfo.getDistrict() != null) {
					WHERE("district = #{district}");
				}
				if (deviceInfo.getCommunity() != null) {
					WHERE("community = #{community}");
				}
				if (deviceInfo.getDeviceState() != null) {
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
		if (deviceInfo.getProject() != null) {
			sqlb.append("and projectName = #{projectName} ");
			column = "province";
		} else {
			column = "projectName";
		}
		if (deviceInfo.getProvince() != null) {
			sqlb.append("and province = #{province} ");
			column = "city";
		}
		if (deviceInfo.getCity() != null) {
			sqlb.append("and city = #{city} ");
			column = "district";
		}
		if (deviceInfo.getDistrict() != null) {
			sqlb.append("and district = #{district} ");
			column = "community";
		}
		return sql.append(column).append(sqlb.append(" Group By " + column)).toString();
	}
}
