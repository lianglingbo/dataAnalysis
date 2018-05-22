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
    public void event(@RequestBody String data) {
		analysisService.addData(data);
    }
	
	@RequestMapping("/register")
	@ResponseBody
    public void register(@RequestBody DeviceInfo deviceInfo) {
		analysisService.register(deviceInfo);
    }
	
	@RequestMapping("/updateSim")
	@ResponseBody
	public void updateSIM(@RequestBody DeviceInfo deviceInfo) {
		analysisService.updateSim(deviceInfo);
	}
	
	@RequestMapping("/getOffline")
	@ResponseBody
	public List<HashMap<String, Object>> getOffline(@RequestParam("data")String data){
		return analysisService.getOffline(data);
	}
	//查询抄表失败的设备
	@RequestMapping("/getReadFailed")
	@ResponseBody
	public List<HashMap<String, Object>> getReadFailed(@RequestParam("data")String data){
		return analysisService.getReadFailed(data);
	}
	@RequestMapping("/getDeviceByParams")
	@ResponseBody
	public List<HashMap<String, Object>> getDeviceByParams(@RequestParam("data")String data){
		return analysisService.getDeviceByParams(data);
	}
	/**
	 * 从druid中获取设备的历史事件信息
	 */
	@RequestMapping("/getDeviceEvenFromDruid")
	@ResponseBody
	public String getDeviceEvenFromDruid(@RequestParam("data")String data){
		System.out.println(data);
		return analysisService.getDeviceEvenFromDruid(data);
	}
}
