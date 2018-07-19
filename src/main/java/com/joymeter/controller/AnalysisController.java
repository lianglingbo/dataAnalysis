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

	//接收设备事件数据
	@RequestMapping("/event")
	@ResponseBody
    public void event(@RequestBody String data) {
		analysisService.addData(data);
    }
	
	//注册设备信息
	@RequestMapping("/register")
	@ResponseBody
    public void register(@RequestBody DeviceInfo deviceInfo) {
		analysisService.register(deviceInfo);
    }

	//根据Id删除设备信息
	@RequestMapping("/deleteDeviceInfoById")
	@ResponseBody
	public void deleteDeviceInfoById(@RequestBody DeviceInfo deviceInfo) {
		analysisService.deleteDeviceInfoById(deviceInfo.getDeviceId());
	}

	//更新设备信息
	@RequestMapping("/updateDeviceInfo")
	@ResponseBody
	public void updateDeviceInfo(@RequestBody DeviceInfo deviceInfo) {
		analysisService.updateDeviceInfo(deviceInfo);
	}

	//更新Sim卡信息
	@RequestMapping("/updateSim")
	@ResponseBody
	public void updateSIM(@RequestBody DeviceInfo deviceInfo) {
		analysisService.updateSim(deviceInfo);
	}
	
	//查询离线的设备
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

	//查询可疑用水的设备
	@RequestMapping("/getUsageStatusFailed")
	@ResponseBody
	public List<HashMap<String, Object>> getUsageStatusFailed(@RequestParam("data")String data){
		return analysisService.getUsageStatusFailed(data);
	}


	//查询特定设备
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
	public String getDeviceEvenFromDruid(@RequestParam("data") String data){
		return analysisService.getDeviceEvenFromDruid(data);
	}

	/**
	 * 从druid中获取某天凌晨用水信息，用量
	 * @param
	 * @return
	 */
	@RequestMapping("/getWaterMeterFromDruid")
	@ResponseBody
	public String getWaterMeterFromDruid(@RequestParam("time") String time) {
		return analysisService.getWaterMeterFromDruid(time);
	}

    /**
     * 从druid中获取某天凌晨用水信息,频率
     * @param
     * @return
     */
    @RequestMapping("/getWaterMeterCountFromDruid")
    @ResponseBody
    public String getWaterMeterCountFromDruid() {
        return analysisService.getWaterMeterCountFromDruid();
    }


    /**
     * 从druid中获取近7天可疑用水信息
     * @param
     * @return
     */
    @RequestMapping("/getExceptionWaterMeter")
    @ResponseBody
    public String getExceptionWaterMeter() {
	    return analysisService.getExceptionWaterMeter();
    }

    /**
     * 根据deviceId查询watermeter用水情况列表
     * @param deviceId
     * @return
     */
    @RequestMapping("/getDeviceInfoFromDruid")
    @ResponseBody
    public String getDeviceInfoFromDruid(String deviceId) {
        return analysisService.getDeviceInfoFromDruid(deviceId);
    }

}
