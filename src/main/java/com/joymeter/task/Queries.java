/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.joymeter.task;

/**
 *
 * @author dell
 */
public class Queries {
    public static final String QUERY_TYPE_DATA ="{\"query\":\"select sum(maxdata) as sumdata from (select deviceId,max(data) as maxdata from dataTest2 where (type like '%s%%') AND (__time < CURRENT_TIMESTAMP - Interval '1' DAY) group by deviceId)\"}";
    public static final String QUERY_OFFLINE_DEVICEID = "{\"query\":\"select deviceId from dataTest2 where event = 'offline' group by deviceId\"}";
    public static final String QUERY_DEVICEID_STATUS = "{\"query\":\"select Max(__time) as Maxtime,event from dataTest2 where deviceId = '%s' and (event = 'online' or event = 'offline') group by event order by Maxtime DESC limit 1\"}";
}
