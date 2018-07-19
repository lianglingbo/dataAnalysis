package com.joymeter.provider;

import com.joymeter.entity.DeviceInfo;
import com.joymeter.entity.DeviceInfos;
import org.apache.ibatis.jdbc.SQL;
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
		System.out.println("下拉框查询"+sqltemp);
		return sqltemp;
	}


	/**
	 * 动态生成查询数据SQL
	 *
	 * @param deviceInfos
	 * @return
	 */
	public String getNoneDataByParams(DeviceInfos deviceInfos) {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlb = new StringBuilder();
		String column = null;
		sql.append("SELECT ");
		sqlb.append(" address,DATE_FORMAT(updateTime,'%Y-%m-%d %T')  AS lastUpdate,TIMESTAMPDIFF(HOUR,updateTime,now()) AS diffTime,deviceId,gatewayId,deviceState,readState, readFaile from device_info WHERE  TIMESTAMPDIFF(HOUR,updateTime,now()) > '24' and deviceId != gatewayId   ");
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
		String sqltemp = sql.append(column).append(sqlb.append( " order by project desc ")).toString();
		System.out.println("下拉框设备列表"+sqltemp);
		return sqltemp;
	}

	/**
	 * 设备列表详情页面，传入不同参数，展示不同列表（离线，抄表失败，一天无数据）
	 * @param args
	 * @return
	 */
	public String getDeviceInfosByArgs(String args){
		StringBuffer sql = new StringBuffer();
		StringBuffer sqlArgs = new StringBuffer();
		//解析参数，是否带项目信息查询格式：条件+项目； noneDataDevice,钱江
		String projectTemp = null;
		if(args.indexOf(",") > 0){
			projectTemp = args.substring(args.indexOf(",") + 1);
			args = args.substring(0,args.indexOf(","));
		}
		sql.append("SELECT project, province, city, district, community,address,DATE_FORMAT(updateTime,'%Y-%m-%d %T')  AS lastUpdate,TIMESTAMPDIFF(HOUR,updateTime,now()) AS diffTime,deviceId,gatewayId,deviceState,readState, readFaile FROM device_info  where  1=1 ");
		if("offlineDevice".equals(args)){
			//查询离线设备列表
			sqlArgs.append("and deviceState = 0 ");
		}else if("readFailedDevice".equals(args)){
			//查询抄表失败列表
			sqlArgs.append("and readState = 1 ");
		}else if("noneDataDevice".equals(args)){
			//查询一天无数据列表
			sqlArgs.append(" and deviceId != gatewayId and TIMESTAMPDIFF(HOUR,updateTime,now()) > '24' ");
		}
		if(projectTemp!=null){
			//解析带project参数
			sqlArgs.append("and project = "+ "'"+projectTemp+"'");
		}
		sqlArgs.append("   order by project ");
		String sqltemp = sql.append(sqlArgs).toString();
		System.out.println("详情页面："+sqltemp);
		return sqltemp;
	}


	/**
	 * 动态生成可疑用水列表查询
	 *
	 * @param deviceInfo
	 * @return
	 */
	public String getUsageWithProjectByParams(DeviceInfo deviceInfo) {
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
	 * 可疑用水列表所有信息
	 *
	 * @param deviceInfo
	 * @return
	 */
	public String getAllUsageInfos( ) {
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


			}
		}.toString();
	}


}
