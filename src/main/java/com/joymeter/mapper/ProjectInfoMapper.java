package com.joymeter.mapper;


import com.joymeter.entity.DeviceInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @ClassName ProjectInfoMapper
 * @Description TODO
 * project_info ,device_info_deleted 表的相关查询
 * @Author liang
 * @Date 2018/7/11 13:36
 * @Version 1.0
 **/
@Repository
public interface ProjectInfoMapper {

    //根据项目查询对应url
    @Select("SELECT url FROM project_info WHERE project = #{project}")
    String getUrl(String project);

    //插入数据到备份表中
    @Insert("INSERT INTO device_info_deleted(deviceId,simId,gatewayId,project,province,city,district,community,address,valveState,deviceState,simState,dataUsed,readState) VALUES(#{deviceId},#{simId},#{gatewayId},#{project},#{province},#{city},#{district},#{community},#{address},#{valveState},#{deviceState},#{simState},#{dataUsed},#{readState})")
    void insertIntoDel(DeviceInfo deviceInfo);
}
