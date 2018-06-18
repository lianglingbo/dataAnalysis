package com.joymeter.provider;

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
}
