package com.joymeter.service;

public interface RedisService {
    void sendToCJoy(String key,String value);
}