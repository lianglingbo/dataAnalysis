package com.joymeter.provider;

import com.joymeter.entity.DeviceInfos;
import org.springframework.util.StringUtils;


public class VisualInterfaceProvider {


	/**
	 * 24小时无数据设备列表项目分组,动态聚合查询
	 * @param deviceInfos
	 * @return
	 */
	public String getNoneDataGroupList(DeviceInfos deviceInfos) {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlb = new StringBuilder();
		String column = null;
		sql.append("SELECT ");
		sqlb.append(",COUNT(*) as noneDataCount from device_info WHERE  TIMESTAMPDIFF(HOUR,updateTime,now()) > '24' and deviceId != gatewayId   ");
		if (!StringUtils.isEmpty(deviceInfos.getProject())) {
			sqlb.append("and project = #{project} ");
			column = "province";
			if (!StringUtils.isEmpty(deviceInfos.getProvince())) {
				sqlb.append("and province = #{province} ");
				column = "city";
				if (!StringUtils.isEmpty(deviceInfos.getCity())) {
					sqlb.append("and city = #{city} ");
					column = "district";
					if (!StringUtils.isEmpty(deviceInfos.getDistrict())) {
						sqlb.append("and district = #{district} ");
						column = "community";
					}
				}
			}
		} else {
			column = "project";
		}
		String sqltemp = sql.append(column).append(sqlb.append(" Group By " + column + " order by noneDataCount desc ")).toString();
		System.out.println(sqltemp);
		return sqltemp;
	}


}
