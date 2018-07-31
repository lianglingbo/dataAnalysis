package com.joymeter;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.joymeter.task.Consumer;
import com.joymeter.task.Scheduler;
import com.joymeter.util.ThreadsExecutor;
import com.joymeter.util.common.SpringBean;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.joymeter.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        try {
        	Runnable appScheduler = () -> {
                Scheduler.GetInstance().start();
            };
            ThreadsExecutor.GetPoolExecutorService().execute(appScheduler);
		} catch (Exception e) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, e);
		}
        
        try {
        	Runnable appScheduler = () -> {
                Consumer.GetInstance().start();
            };
            ThreadsExecutor.GetPoolExecutorService().execute(appScheduler);
		} catch (Exception e) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, e);
		}
    }
    
    @Bean
    public SpringBean getSpringContext() {
    	return new SpringBean();
}
}
