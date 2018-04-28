package com.joymeter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.joymeter.service.DruidService;

@CrossOrigin("*")
@RestController
@RequestMapping("joy")
public class DruidController {
	@Autowired
	private DruidService druidService;
	
	@RequestMapping("/addData")
    public void addData(@RequestParam("data") String data) {
        druidService.addData(data);
    }
}
