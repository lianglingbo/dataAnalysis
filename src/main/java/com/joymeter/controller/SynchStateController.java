package com.joymeter.controller;

import com.joymeter.service.SynchStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName SynchStateController
 * @Description TODO
 * @Author liang
 * @Date 2018/6/8 10:33
 * @Version 1.0
 **/
@CrossOrigin("*")
@Controller
@RequestMapping("synch")
public class SynchStateController {
    @Autowired
    private SynchStateService synchStateService;

    @RequestMapping("/queryDruidFromMysql")
    @ResponseBody
    public String  queryDruidFromMysql( ) {
        return synchStateService.queryDruidFromMysql();
    }


    /**
     * 同步usage_hour 表中数据到druid
     */
    @RequestMapping("/synchUsageToDruid")
    @ResponseBody
    public void  synchUsageToDruid( ) {
          synchStateService.SynchUsageToDruid();
    }

}
