package com.joymeter.mapper;

import java.util.HashMap;
import java.util.List;

import com.joymeter.entity.UsageHour;
import org.apache.ibatis.annotations.*;

import com.joymeter.entity.DeviceInfo;
import com.joymeter.provider.DeviceInfoProvider;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceInfoMapper {
	//查询全部设备
	@Select("SELECT * FROM device_info")
    List<DeviceInfo> getAll();
	
	//查询特定设备SQL
	@SelectProvider(type = DeviceInfoProvider.class,method="selectByParams")
    List<HashMap<String, Object>> getByParams(DeviceInfo deviceInfo);

	//查询可疑用水详情列表
    @SelectProvider(type = DeviceInfoProvider.class,method="selectUsageWithProjectByParams")
    List<HashMap<String, Object>> getUsageWithProjectByParams(DeviceInfo deviceInfo);

    //统计数量SQL
    @SelectProvider(type = DeviceInfoProvider.class,method="selectcount")
	int getCount(DeviceInfo deviceInfo);
	
    //查询离线设备聚合SQL
	@SelectProvider(type = DeviceInfoProvider.class,method="selectoffline")
    List<HashMap<String, Object>> getofflineGroup(DeviceInfo deviceInfo);

	//查询抄表失败聚合SQL
    @SelectProvider(type = DeviceInfoProvider.class,method="selectReadFailed")
    List<HashMap<String, Object>> getReadFailedGroup(DeviceInfo deviceInfo);

    //查询可疑用水聚合SQL
    @SelectProvider(type = DeviceInfoProvider.class,method="selectUsageStatusFailed")
    List<HashMap<String, Object>> selectUsageStatusFailed(DeviceInfo deviceInfo);

    //根据Id查询设备
    @Select("SELECT * FROM device_info WHERE deviceId = #{deviceId}")
    DeviceInfo getOne(String deviceId);

    //查询24小时无数据的设备
    @Select(" select * from device_info where   TIMESTAMPDIFF(HOUR,updateTime,now()) > '24'  and deviceId != gatewayId and project in(select project from project_info ) ")
    List<DeviceInfo> getNoneDataAday();

    //插入新的设备数据
    @Insert("INSERT INTO device_info(deviceId,simId,gatewayId,project,province,city,district,community,address,category,valveId,valveProtocol,deviceProtocol,gatewayUrl) VALUES(#{deviceId},#{simId},#{gatewayId},#{project},#{province},#{city},#{district},#{community},#{address},#{category},#{valveId},#{valveProtocol},#{deviceProtocol},#{gatewayUrl})")
    void insert(DeviceInfo deviceInfo);
    
    //更新设备信息SQL
    @UpdateProvider(type = DeviceInfoProvider.class,method="updateDeviceInfo")
    void updateDeviceInfo(DeviceInfo deviceInfo);

    //删除设备
    @Delete("DELETE FROM device_info WHERE deviceId =#{deviceId}")
    void deleteDeviceInfoById(String deviceId);

    //根据设备id，整点用量到usage_hour表中，存在设备则更新，不存在则新增
    @InsertProvider(method = "insertIntoUsageHour",type=DeviceInfoProvider.class)
    void insertIntoUsageHour(UsageHour usageHour);

    //通过deviceId查询usage_hour表
    @Select("SELECT * FROM usage_hour WHERE deviceId = #{deviceId}")
    UsageHour getOneUsageHour(String deviceId);

    //查询usage_hour 所以记录，用于备份
    @Select("SELECT * FROM usage_hour")
    List<UsageHour> selectAllUsgae();

    //查询usage_hour每小时用量不为0，同步到druid中
    @Select("SELECT * from usage_hour where (one-zero)>0 and (two-one)>0 and (three-two)>0 and (four-three)>0 and (five-four)>0 and (six-five)>0")
    List<UsageHour> selectExceptionUsage();
    //清空usage_hour表
    @Select("truncate table usage_hour")
    void  truncateUsageHour();
}
