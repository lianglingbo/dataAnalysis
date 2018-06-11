package com.joymeter.serviceImpl;

import com.joymeter.service.VisualInterfaceService;
import org.springframework.stereotype.Service;

/**
 * @ClassName VisualInterfaceServiceImpl
 * @Description TODO
 * 为前台可视化页面提供数据接口
 * @Author liang
 * @Date 2018/6/11 17:46
 * @Version 1.0
 **/
@Service
public class VisualInterfaceServiceImpl implements VisualInterfaceService {

    /**
     *  获取设备总数，抄表失败个数，离线设备个数，离线网关个数，24小时无数据设备个数
     * @return
     */
    @Override
    public String getTotalNum() {

        return null;
    }
}
