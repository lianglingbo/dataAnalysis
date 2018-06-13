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



}
