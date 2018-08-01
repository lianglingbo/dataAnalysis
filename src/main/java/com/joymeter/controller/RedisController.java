package com.joymeter.controller;

import com.joymeter.util.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.joymeter.entity.DeviceInfo;
import com.joymeter.service.AnalysisService;

import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("redis/joy")
@Api("数据接收信息处理")
public class RedisController {
	@Autowired
	private AnalysisService analysisService;
	@Autowired
	private RedisUtils redisUtils;

	@RequestMapping("/addData")
    public Map<String, Object> addData(@RequestParam("data") String data) {
		return analysisService.addData(data);
    }

	@ApiOperation(value="设备状态信息接收", notes=" from网关，设备实时状态接收，接收信息发送至druid，解析发现状态变更后，发送到本地mysql和业务层")
	@ApiImplicitParam(name = "event", value = "设备信息", required = true, dataType = "json")
	@RequestMapping("/event")
    public Map<String, Object> event(@RequestBody String data) {
		return analysisService.addData(data);
    }
	
	@RequestMapping("/register")
	@ApiOperation(value="注册设备信息", notes="网关每天晚上定时注册，包括设备的编号，项目地址，设备协议等信息")
	@ApiImplicitParam(name = "deviceInfo", value = "设备信息实体类", required = true, dataType = "DeviceInfo")
	public Map<String, Object> register(@RequestBody DeviceInfo deviceInfo) {
		return analysisService.register(deviceInfo);
    }
	
	@RequestMapping("/updateSim")
	public void updateSIM(@RequestBody DeviceInfo deviceInfo) {
		analysisService.updateSim(deviceInfo);
	}


}
