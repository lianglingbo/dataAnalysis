package com.joymeter.service;


import java.util.HashMap;
import java.util.List;

public interface VisualInterfaceService {

    //获取设备总数，抄表失败个数，离线设备个数，离线网关个数，24小时无数据设备个数
    String getTotalNum( );
    //获取离线设备个数，group by project
    String getOffDevCountByProject();
    //获取离线网关个数，group by project
    String getOffGtwCountByProject();
    //24小时无数据设备group by project
    String getNodeDataByProject();
    //抄表失败group by project
    String getReadFailedByProject();
    //设备总数group by project
    String getTotalCountByProject();
    //24小时无数据设备列表动态分组,动态聚合查询
    List<HashMap<String, Object>> getNoneDataGroupList(String data);
    //24小时无数据查询设备列表详情，按地区信息查询
    List<HashMap<String, Object>> getNoneDataByParams(String data);
    //设备列表详情页面，传入不同参数，展示不同列表（离线，抄表失败，一天无数据）
    List<HashMap<String, Object>> getDeviceInfosByArgs(String  args);

    //通过项目名称获取项目首页地址
    String getURLByProject(String project);

    //获取可疑用水的用水和项目信息
    List<HashMap<String, Object>> getUsageWithProjectByParams(String data);

    //getAllUsageInfos查询可疑用水的详细信息展示所有
    List<HashMap<String, Object>> getAllUsageInfos();

}
