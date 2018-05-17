/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.joymeter.task;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.joymeter.cache.DataCache;
import com.joymeter.util.HttpClient;
import com.joymeter.util.PropertiesUtils;

/**
 *
 * @author Administrator
 */
public class Scheduler {

    private static Scheduler scheduler = null;
	private static String druidUrl = PropertiesUtils.getProperty("druidUrl", "");

    /**
     * 单例模式
     *
     * @return
     */
    public static Scheduler GetInstance() {
        if (scheduler == null) {
            scheduler = new Scheduler();
        }
        return scheduler;
    }

    /**
     * 线程等待
     */
    public void waitme() {
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 唤醒线程
     */
    public void weekup() {
        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
     * 调度执行
     */
    public void start() {
        while (true) {
            final String value = DataCache.poll();
            if (value == null || value.isEmpty()) {
                continue;
            }
			HttpClient.sendPost(druidUrl, value); // 向Druid发送数据
        }
    }
}