package com.joymeter.controller;

import com.joymeter.service.VisualInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

}
