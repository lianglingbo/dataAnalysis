package com.joymeter.mapper;

import com.joymeter.entity.TotalNumBean;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

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

     @Select("  select  (select count(deviceId) from device_info) as totalCount,(select count(deviceId)  from device_info where deviceState = '0' and deviceId != gatewayId) as offDeviceCount,(select count(deviceId)  from device_info where deviceState = '0' and deviceId = gatewayId) as offGatewayCount,(select count(deviceId)  from device_info where readState = '1') as readFaileCount,(select count(deviceId)  from device_info where TIMESTAMPDIFF(HOUR,updateTime,now()) > '24' and deviceId != gatewayId) as noneDataCount  from device_info limit 1 ")
     TotalNumBean getTotalNum();

}
