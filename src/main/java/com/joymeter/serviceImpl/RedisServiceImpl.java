package com.joymeter.serviceImpl;

import com.joymeter.service.RedisService;
import com.joymeter.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @ClassName RedisServiceImpl
 * @Description TODO
 * 往redis中发送数据
 * @Author liang
 * @Date 2018/6/26 18:44
 * @Version 1.0
 **/
@Service
public class RedisServiceImpl implements RedisService {
    @Autowired
    private RedisUtils redisUtils;
    private static final Logger logger = Logger.getLogger(AnalysisServiceImpl.class.getName());


    //发送数据到redis中，过滤此项目的设备
    @Override
    public void sendToCJoy(String key, String value) {
        try{
            //先查询redis中是否存在，存在则插入更新
            if(redisUtils.exists(key)){
                redisUtils.setKV(key,value);
                logger.log(Level.INFO,"发送至redis中的值："+value);
            }
        }catch (Exception e){
            logger.log(Level.SEVERE, value, e);
        }


    }
}
