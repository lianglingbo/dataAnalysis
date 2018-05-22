package com.joymeter.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import com.joymeter.entity.DeviceInfo;
import com.joymeter.provider.DeviceInfoProvider;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceInfoMapper {
	@Select("SELECT * FROM device_info")
    List<DeviceInfo> getAll();
	
	@SelectProvider(type = DeviceInfoProvider.class,method="selectByParams")
    List<HashMap<String, Object>> getByParams(DeviceInfo deviceInfo);
	
	@SelectProvider(type = DeviceInfoProvider.class,method="selectcount")
	int getCount(DeviceInfo deviceInfo);
	
	@SelectProvider(type = DeviceInfoProvider.class,method="selectoffline")
    List<HashMap<String, Object>> getofflineGroup(DeviceInfo deviceInfo);

	//查询抄表失败聚合SQL
    @SelectProvider(type = DeviceInfoProvider.class,method="selectReadFailed")
    List<HashMap<String, Object>> getReadFailedGroup(DeviceInfo deviceInfo);

    @Select("SELECT * FROM device_info WHERE deviceId = #{deviceId}")
    DeviceInfo getOne(String deviceId);
    
    @Insert("INSERT INTO device_info(deviceId,simId,gatewayId,project,province,city,district,community,address) VALUES(#{deviceId},#{simId},#{gatewayId},#{project},#{province},#{city},#{district},#{community},#{address})")
    void insert(DeviceInfo deviceInfo);
    
    @UpdateProvider(type = DeviceInfoProvider.class,method="updateDeviceInfo")
    void updateDeviceInfo(DeviceInfo deviceInfo);

    @Delete("DELETE FROM device_info WHERE deviceId =#{deviceId}")
    void delete(Long deviceId);
}
