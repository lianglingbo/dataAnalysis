/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.joymeter.util;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author Administrator
 */
public class ThreadsExecutor {

    private static ExecutorService executor = null;
    private static ExecutorService singleExecutor = null;
    private static ExecutorService fixedExecutor = null;
    private static ScheduledExecutorService scheduledExecutor = null;
    private static CompletionService<ByteBuffer> completion = null;

    /**
     * 获取Executor Service
     *
     * @return
     */
    public static ExecutorService GetPoolExecutorService() {
        if (executor == null) {
            executor = Executors.newCachedThreadPool();
        }
        return executor;
    }

    /**
     * 获取Executor Service
     *
     * @return
     */
    public static ExecutorService GetSingleExecutorService() {
        if (singleExecutor == null) {
            singleExecutor = Executors.newSingleThreadExecutor();
        }
        return singleExecutor;
    }

    /**
     * 获取Executor Service
     *
     * @return
     */
    public static ExecutorService GetFixedExecutorService() {
        if (fixedExecutor == null) {
            fixedExecutor = Executors.newFixedThreadPool(16);
        }
        return fixedExecutor;
    }

    /**
     * 获取Executor Service
     *
     * @return
     */
    public static ScheduledExecutorService GetScheduledService() {
        if (scheduledExecutor == null) {
            scheduledExecutor = Executors.newScheduledThreadPool(1);
        }
        return scheduledExecutor;
    }

    /**
     * 获取Completion Service
     *
     * @return
     */
    public static CompletionService<ByteBuffer> GetCompletionService() {
        if (completion == null) {
            if (executor == null) {
                ThreadsExecutor.GetPoolExecutorService();
            }
            completion = new ExecutorCompletionService<>(executor);
        }
        return completion;
    }
}
