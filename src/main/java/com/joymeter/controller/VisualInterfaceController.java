package com.joymeter.controller;

import com.joymeter.service.VisualInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

/**
 * @ClassName VisualInterfaceController
 * @Description TODO
 * 为前台可视化页面提供数据接口
 * @Author liang
 * @Date 2018/6/11 17:41
 * @Version 1.0
 **/
@CrossOrigin("*")
@Controller
@RequestMapping("visual")
public class VisualInterfaceController {
    @Autowired
    private VisualInterfaceService visualInterfaceService;

    //获取设备总数，抄表失败个数，离线设备个数，离线网关个数，24小时无数据设备个数
    @RequestMapping("/getTotalNum")
    @ResponseBody
    public String  getTotalNum() {
        return visualInterfaceService.getTotalNum();
    }

    //获取离线设备个数group by project
    @RequestMapping("/getOffDevCountByProject")
    @ResponseBody
    public String getOffDevCountByProject(){
        return visualInterfaceService.getOffDevCountByProject();
    }

    //获取离线网关个数group by project
    @RequestMapping("/getOffGtwCountByProject")
    @ResponseBody
    public String getOffGtwCountByProject(){
        return visualInterfaceService.getOffGtwCountByProject();
    }

    //24小时无数据设备group by project
    @RequestMapping("/getNodeDataByProject")
    @ResponseBody
    public String getNodeDataByProject(){
        return visualInterfaceService.getNodeDataByProject();
    }

    //抄表失败group by project
    @RequestMapping("/getReadFailedByProject")
    @ResponseBody
    public String getReadFailedByProject(){
        return visualInterfaceService.getReadFailedByProject();
    }

    //设备总数group by project
    @RequestMapping("/getTotalCountByProject")
    @ResponseBody
    public String getTotalCountByProject(){
        return visualInterfaceService.getTotalCountByProject();
    }

    //24小时无数据设备列表分组详情
    @RequestMapping("/getNoneDataGroupList")
    @ResponseBody
    public List<HashMap<String, Object>> getNoneDataGroupList(@RequestParam("data")String data){
        return visualInterfaceService.getNoneDataGroupList(data);
    }

    //24小时无数据查询设备列表详情，按地区信息查询
    @RequestMapping("/getNoneDataByParams")
    @ResponseBody
    public List<HashMap<String, Object>> getNoneDataByParams(@RequestParam("data")String data){
        return visualInterfaceService.getNoneDataByParams(data);
    }

    //设备列表详情页面，传入不同参数，展示不同列表（离线，抄表失败，一天无数据）
    @RequestMapping("/getDeviceInfosByArgs")
    @ResponseBody
    public List<HashMap<String, Object>> getDeviceInfosByArgs(@RequestParam("data")String data){
        return visualInterfaceService.getDeviceInfosByArgs(data);
    }

}
