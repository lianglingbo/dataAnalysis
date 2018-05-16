package com.joymeter.controller;

import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.joymeter.entity.DeviceInfo;
import com.joymeter.service.AnalysisService;

@CrossOrigin("*")
@Controller
@RequestMapping("monitor")
public class AnalysisController {
	@Autowired
	private AnalysisService analysisService;
	
	@RequestMapping("/offlinetable")
	public String index(){
		return "offlinetable";
	}
	
	@RequestMapping("/event")
	@ResponseBody
    public void event(@RequestParam("data") String data) {
		analysisService.addData(data);
    }
	
	@RequestMapping("/register")
	@ResponseBody
    public void register(@RequestBody String data) {
		analysisService.register(data);
    }
	
	@RequestMapping("/updateSim")
	@ResponseBody
	public void updateSIM(@RequestBody String data) {
		analysisService.updateSim(data);
	}
	
	@RequestMapping("/getOfflineNum")
	@ResponseBody
	public int getOfflineNum() {
		return analysisService.getOfflineNum();
	}
	
	@RequestMapping("/getOffline")
	@ResponseBody
	public List<HashMap<String, Object>> getOffline(@RequestParam("data")String data){
		return analysisService.getOffline(data);
	}
	
	@RequestMapping("/getOfflineDevice")
	@ResponseBody
	public List<DeviceInfo> getOfflineDevice(@RequestParam("data")String data){
		return analysisService.getOfflineDevice(data);
	}
}
