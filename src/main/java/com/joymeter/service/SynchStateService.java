package com.joymeter.service;

public interface SynchStateService {

    //遍历方法mysql，查询druid
    String queryDruidFromMysql( );

    //同步usage_hour 表中数据到druid
    void SynchUsageToDruid();
}
