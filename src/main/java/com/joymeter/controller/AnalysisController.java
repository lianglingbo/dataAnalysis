package com.joymeter.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
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
@Api("数据分析")
public class AnalysisController {
	@Autowired
	private AnalysisService analysisService;

 	@RequestMapping("/offlinetable")
	public String index(){
		return "offlinetable";
	}

 	@RequestMapping("/event")
	@ResponseBody
	@ApiOperation(value="设备状态信息接收", notes=" from网关，设备实时状态接收，接收信息发送至druid，解析发现状态变更后，发送到本地mysql和业务层")
	@ApiImplicitParam(name = "event", value = "设备信息", required = true, dataType = "json")
	public Map<String, Object> event(@RequestBody String data) {
		return analysisService.addData(data);
    }
	
 	@ApiOperation(value="注册设备信息", notes="网关每天晚上定时注册，包括设备的编号，项目地址，设备协议等信息")
	@ApiImplicitParam(name = "deviceInfo", value = "设备信息实体类", required = true, dataType = "DeviceInfo")
	@RequestMapping("/register")
	@ResponseBody
    public Map<String, Object> register(@RequestBody DeviceInfo deviceInfo) {
		return analysisService.register(deviceInfo);
    }

 	@RequestMapping("/deleteDeviceInfoById")
	@ResponseBody
	@ApiOperation(value="设备删除", notes="根据Id删除设备信息")
	public void deleteDeviceInfoById(@RequestBody DeviceInfo deviceInfo) {
		analysisService.deleteDeviceInfoById(deviceInfo.getDeviceId());
	}

 	@RequestMapping("/updateDeviceInfo")
	@ResponseBody
	@ApiOperation(value="更新设备信息", notes="暂时有使用")
	public void updateDeviceInfo(@RequestBody DeviceInfo deviceInfo) {
		analysisService.updateDeviceInfo(deviceInfo);
	}

 	@RequestMapping("/updateSim")
	@ResponseBody
	@ApiOperation(value="更新Sim卡信息", notes="暂时有使用")
	public void updateSIM(@RequestBody DeviceInfo deviceInfo) {
		analysisService.updateSim(deviceInfo);
	}
	
 	@RequestMapping("/getOffline")
	@ResponseBody
	@ApiOperation(value="查询离线的设备", notes="做可视化页面时调用")
	public List<HashMap<String, Object>> getOffline(@RequestParam("data")String data){
		return analysisService.getOffline(data);
	}
	

	@RequestMapping("/getReadFailed")
	@ResponseBody
	@ApiOperation(value="查询抄表失败的设备", notes="做可视化页面时调用")
	public List<HashMap<String, Object>> getReadFailed(@RequestParam("data")String data){
		return analysisService.getReadFailed(data);
	}


	@RequestMapping("/getUsageStatusFailed")
	@ResponseBody
	@ApiOperation(value="查询可疑用水的设备", notes="做可视化页面时调用")
	public List<HashMap<String, Object>> getUsageStatusFailed(@RequestParam("data")String data){
		return analysisService.getUsageStatusFailed(data);
	}


 	@RequestMapping("/getDeviceByParams")
	@ResponseBody
	@ApiOperation(value="查询特定设备", notes="做可视化页面时调用")
	public List<HashMap<String, Object>> getDeviceByParams(@RequestParam("data")String data){
		return analysisService.getDeviceByParams(data);
	}


	@RequestMapping("/getDeviceEvenFromDruid")
	@ResponseBody
	@ApiOperation(value="从druid中获取设备的历史事件信息", notes="做可视化页面时调用")
	public String getDeviceEvenFromDruid(@RequestParam("data") String data){
		return analysisService.getDeviceEvenFromDruid(data);
	}


	@RequestMapping("/getWaterMeterFromDruid")
	@ResponseBody
	@ApiOperation(value="从druid中获取某天凌晨用水信息", notes="做可视化页面时调用")

	public String getWaterMeterFromDruid(@RequestParam("time") String time) {
		return analysisService.getWaterMeterFromDruid(time);
	}


    @RequestMapping("/getWaterMeterCountFromDruid")
    @ResponseBody
	@ApiOperation(value="从druid中获取某天凌晨用水信息", notes="做可视化页面时调用")

	public String getWaterMeterCountFromDruid() {
        return analysisService.getWaterMeterCountFromDruid();
    }



    @RequestMapping("/getExceptionWaterMeter")
    @ResponseBody
	@ApiOperation(value="从druid中获取近7天可疑用水信息", notes="做可视化页面时调用")

	public String getExceptionWaterMeter() {
	    return analysisService.getExceptionWaterMeter();
    }


    @RequestMapping("/getDeviceInfoFromDruid")
    @ResponseBody
	@ApiOperation(value="根据deviceId查询watermeter用水情况列表", notes="做可视化页面时调用")

	public String getDeviceInfoFromDruid(String deviceId) {
        return analysisService.getDeviceInfoFromDruid(deviceId);
    }

}
