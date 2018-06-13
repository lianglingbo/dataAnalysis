package com.joymeter.mapper;

import com.joymeter.entity.ProjectCountBean;
import com.joymeter.entity.TotalNumBean;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

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
     @Select("  select  (select count(deviceId) from device_info) as totalCount,(select count(deviceId)  from device_info where deviceState = '0' and deviceId != gatewayId) as offDeviceCount,(select count(deviceId)  from device_info where deviceState = '0' and deviceId = gatewayId) as offGatewayCount,(select count(deviceId)  from device_info where readState = '1') as readFaileCount,(select count(deviceId)  from device_info where TIMESTAMPDIFF(HOUR,updateTime,now()) > '24' and deviceId != gatewayId) as noneDataCount  from device_info limit 1 ")
     TotalNumBean getTotalNum();

     //获取离线设备个数group by project
     @Select("SELECT a.* FROM (SELECT IFNULL(project,'离线设备总数') AS myProject,count(deviceId) AS myCount FROM device_info WHERE deviceState = 0 and deviceId != gatewayId GROUP BY  project WITH ROLLUP) AS a ORDER BY a.myCount DESC ")
     List<ProjectCountBean> getOffDevCountByProject();

     //获取离线网关个数group by project
     @Select("SELECT a.* FROM (SELECT IFNULL(project,'离线网关总数') AS myProject,count(deviceId) AS myCount FROM device_info WHERE deviceState = 0 and deviceId = gatewayId GROUP BY  project WITH ROLLUP) AS a ORDER BY a.myCount DESC ")
     List<ProjectCountBean> getOffGtwCountByProject();

     //24小时无数据设备group by project
     @Select("  SELECT a.* FROM  (SELECT IFNULL(project,'无数据设备总数') AS myProject,count(deviceId) AS myCount FROM device_info where TIMESTAMPDIFF(HOUR,updateTime,now()) > '24' GROUP BY project  WITH ROLLUP ) AS a ORDER BY a.myCount DESC ")
     List<ProjectCountBean> getNodeDataByProject();

     //抄表失败group by project
     @Select("  SELECT a.* FROM  (SELECT IFNULL(project,'抄表失败总数') AS myProject,count(deviceId) AS myCount FROM device_info where  readState=1 GROUP BY project  WITH ROLLUP ) AS a ORDER BY a.myCount DESC ")
     List<ProjectCountBean> getReadFailedByProject();

     //设备总数group by project
     @Select("  SELECT a.* FROM  (SELECT IFNULL(project,'总设备数') AS myProject,count(deviceId) AS myCount FROM device_info  GROUP BY project  WITH ROLLUP ) AS a ORDER BY a.myCount DESC ")
     List<ProjectCountBean> getTotalCountByProject();

}
