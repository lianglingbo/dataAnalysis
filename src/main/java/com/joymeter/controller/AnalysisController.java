package com.joymeter.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.joymeter.service.AnalysisService;

@CrossOrigin("*")
@RestController
@RequestMapping("joy")
public class AnalysisController {
	@Autowired
	private AnalysisService analysisService;
	
	@RequestMapping("/addData")
    public void addData(@RequestParam("data") String data) {
		analysisService.addData(data);
    }
	
	@RequestMapping("/updateStatus")
	public void updateStatus(@RequestParam("data")String data) {
		analysisService.updateStatus(data);
	}
	
	@RequestMapping("/getOffline")
	public List<HashMap<String, Object>> getOffline(@RequestParam("params")String params){
		return analysisService.getOffline(params);
	}
}
