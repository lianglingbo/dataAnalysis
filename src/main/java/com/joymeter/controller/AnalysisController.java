package com.joymeter.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.joymeter.entity.DeviceInfo;
import com.joymeter.service.AnalysisService;

@CrossOrigin("*")
@RestController
public class AnalysisController {
	@Autowired
	private AnalysisService analysisService;
	
	@RequestMapping("/event")
    public void addData(@RequestParam("data") String data) {
		analysisService.event(data);
    }
	
	@RequestMapping("/register")
    public void register(@RequestParam("data") String data) {
		analysisService.register(data);
    }
	
	@RequestMapping("/updateSIM")
	public void updateSIM(@RequestParam("data")String data) {
		analysisService.updateSIM(data);
	}
	
	@RequestMapping("/updateStatus")
	public void updateStatus(@RequestParam("data")String data) {
		analysisService.updateStatus(data);
	}
	
	@RequestMapping("/getOffline")
	public List<HashMap<String, Object>> getOffline(@RequestParam("data")String data){
		return analysisService.getOffline(data);
	}
	
	@RequestMapping("/getOfflineDevice")
	public List<DeviceInfo> getOfflineDevice(@RequestParam("data")String data){
		return analysisService.getOfflineDevice(data);
	}
}
