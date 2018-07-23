package com.joymeter.mapper;

import com.joymeter.entity.*;
import com.joymeter.provider.DeviceInfoProvider;
import com.joymeter.provider.VisualInterfaceProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * 为可视化页面提供数据
 *
 *获取设备总数，抄表失败个数，离线设备个数，离线网关个数，24小时无数据设备个数
 select
 (select count(deviceId) from device_info) as totalCount,
 (select count(deviceId)  from device_info where deviceState = '0' and deviceId != gatewayId) as offDeviceCount,
 (select count(deviceId)  from device_info where deviceState = '0' and deviceId = gatewayId) as offGatewayCount,
 (select count(deviceId)  from device_info where readState = '1') as readFaileCount,
 (select count(deviceId)  from device_info where TIMESTAMPDIFF(HOUR,updateTime,now()) > '24' and deviceId != gatewayId) as noneDataCount

 from device_info limit 1 ;
 */
@Repository
public interface VisualInterfaceMapper {

     //获取设备总数，抄表失败个数，离线设备个数，离线网关个数，24小时无数据设备个数
     @Select("SELECT  (select count(deviceId) from device_info) as totalCount,(select count(deviceId)  from device_info where deviceState = '0' and deviceId != gatewayId) as offDeviceCount,(select count(deviceId)  from device_info where deviceState = '0' and deviceId = gatewayId) as offGatewayCount,(select count(deviceId)  from device_info where readState = '1') as readFaileCount,(select count(deviceId)  from device_info where TIMESTAMPDIFF(HOUR,updateTime,now()) > '24' and deviceId != gatewayId) as noneDataCount  from device_info limit 1 ")
     TotalNumBean getTotalNum();

     //获取离线设备个数group by project
     @Select("SELECT a.* FROM (SELECT IFNULL(project,'离线设备总数') AS myProject,count(deviceId) AS myCount FROM device_info WHERE deviceState = 0 and deviceId != gatewayId GROUP BY  project WITH ROLLUP) AS a ORDER BY a.myCount DESC ")
     List<ProjectCountBean> getOffDevCountByProject();

     //获取离线网关个数group by project
     @Select("SELECT a.* FROM (SELECT IFNULL(project,'离线网关总数') AS myProject,count(deviceId) AS myCount FROM device_info WHERE deviceState = 0 and deviceId = gatewayId GROUP BY  project WITH ROLLUP) AS a ORDER BY a.myCount DESC ")
     List<ProjectCountBean> getOffGtwCountByProject();

     //24小时无数据设备group by project
     @Select("  SELECT a.* FROM  (SELECT IFNULL(project,'无数据设备总数') AS myProject,count(deviceId) AS myCount FROM device_info where TIMESTAMPDIFF(HOUR,updateTime,now()) > '24'  and deviceId != gatewayId  GROUP BY project  WITH ROLLUP ) AS a ORDER BY a.myCount DESC ")
     List<ProjectCountBean> getNodeDataByProject();

     //抄表失败group by project
     @Select("  SELECT a.* FROM  (SELECT IFNULL(project,'抄表失败总数') AS myProject,count(deviceId) AS myCount FROM device_info where  readState=1 GROUP BY project  WITH ROLLUP ) AS a ORDER BY a.myCount DESC ")
     List<ProjectCountBean> getReadFailedByProject();

     //设备总数group by project
     @Select("  SELECT a.* FROM  (SELECT IFNULL(project,'总设备数') AS myProject,count(deviceId) AS myCount FROM device_info  GROUP BY project  WITH ROLLUP ) AS a ORDER BY a.myCount DESC ")
     List<ProjectCountBean> getTotalCountByProject();

     //24小时无数据设备列表动态分组,动态聚合查询
     @SelectProvider(type = VisualInterfaceProvider.class,method="getNoneDataGroupList")
     List<HashMap<String, Object>> getNoneDataGroupList(DeviceInfos deviceInfos);

     //24小时无数据查询设备列表详情，按地区信息查询
     @SelectProvider(type = VisualInterfaceProvider.class,method="getNoneDataByParams")
     List<HashMap<String, Object>> getNoneDataByParams(DeviceInfos deviceInfos);

     //设备列表详情页面，传入不同参数，展示不同列表（离线，抄表失败，一天无数据）
     @SelectProvider(type = VisualInterfaceProvider.class,method="getDeviceInfosByArgs")
     List<HashMap<String, Object>> getDeviceInfosByArgs(String  data);

     //通过项目名称获取项目首页地址
     @Select(" select homeurl from project_info where project = #{project}")
     String getURLByProject(String project);

     //查询可疑用水详情列表
     @SelectProvider(type = VisualInterfaceProvider.class,method="getUsageWithProjectByParams")
     List<HashMap<String, Object>> getUsageWithProjectByParams(DeviceInfo deviceInfo);
     //查询可疑用水详情列表所有信息

     @SelectProvider(type = VisualInterfaceProvider.class,method="getAllUsageInfos")
     List<HashMap<String,Object>> getAllUsageInfos();

     //设备分布数量
     @SelectProvider(type = VisualInterfaceProvider.class,method="getDevDistribution")
    List<HashMap<String,Object>> getDevDistribution(String level);

     //查询本级和上级地区码
     @Select(" select * from region_info where name = #{name}")
     RegionBean getRegion(String name);

     //查询本级和上级地区码
     @Select(" select * from region_info where adcode = #{adcode}")
     RegionBean getRegionByadcode(String adcode);
}
