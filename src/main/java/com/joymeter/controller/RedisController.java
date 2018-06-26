package com.joymeter.controller;

import com.joymeter.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.joymeter.entity.DeviceInfo;
import com.joymeter.service.AnalysisService;

@CrossOrigin("*")
@RestController
@RequestMapping("redis/joy")
public class RedisController {
	@Autowired
	private AnalysisService analysisService;
	@Autowired
	private RedisUtils redisUtils;

	@RequestMapping("/addData")
    public void addData(@RequestParam("data") String data) {
		analysisService.addData(data);
    }
	
	@RequestMapping("/event")
    public void event(@RequestBody String data) {
		analysisService.addData(data);
    }
	
	@RequestMapping("/register")
    public void register(@RequestBody DeviceInfo deviceInfo) {
		analysisService.register(deviceInfo);
    }
	
	@RequestMapping("/updateSim")
	public void updateSIM(@RequestBody DeviceInfo deviceInfo) {
		analysisService.updateSim(deviceInfo);
	}


}
