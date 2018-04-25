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
    public static final String QUERY_TYPE_DATA ="select sum(maxdata) as sumdata from (select deviceId,max(data) as maxdata from dataTest2 where (type like '%s%%') AND (__time < CURRENT_TIMESTAMP - Interval '1' DAY) group by deviceId)";

}
