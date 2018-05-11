package com.joymeter.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import com.joymeter.entity.DeviceInfo;
import com.joymeter.provider.DeviceInfoProvider;

public interface DeviceInfoMapper {
	@Select("SELECT * FROM device_info")
    List<DeviceInfo> getAll();
	
	@SelectProvider(type = DeviceInfoProvider.class,method="selectByParams")
    List<DeviceInfo> getByParams(DeviceInfo deviceInfo);
	
	@SelectProvider(type = DeviceInfoProvider.class,method="selectoffline")
    List<HashMap<String, Object>> getofflineCount(DeviceInfo deviceInfo);

    @Select("SELECT * FROM device_info WHERE deviceId = #{deviceId}")
    DeviceInfo getOne(Long deviceId);
    
    @Insert("INSERT INTO device_info(deviceId,gatewayId,simId,project,province,city,district,community,address,valveState,deviceState,simState,dataUsed) VALUES(#{deviceId},#{gatewayId},#{simId},#{project},#{province},#{city},#{district},#{community},#{address},#{valveState},#{deviceState},#{simState},#{dataUsed})")
    void insert(DeviceInfo deviceInfo);

    @Update("UPDATE device_info SET deviceState=#{deviceState} WHERE deviceId =#{deviceId}")
    void updateDevice(DeviceInfo deviceInfo);
    
    @Update("UPDATE device_info SET simState=#{simState},dataUsed=#{dataUsed} WHERE simId =#{simId}")
    void updateSim(DeviceInfo deviceInfo);

    @Delete("DELETE FROM device_info WHERE deviceId =#{deviceId}")
    void delete(Long deviceId);
}
