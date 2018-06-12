package com.joymeter.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.joymeter.entity.ProjectCountBean;
import com.joymeter.entity.TotalNumBean;
import com.joymeter.mapper.VisualInterfaceMapper;
import com.joymeter.service.VisualInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Autowired
    private VisualInterfaceMapper visualInterfaceMapper;
    /**
     *  获取设备总数，抄表失败个数，离线设备个数，离线网关个数，24小时无数据设备个数
     * @return
     */
    @Override
    public String getTotalNum() {
        try{
            TotalNumBean totalNum = visualInterfaceMapper.getTotalNum();
            //javaBean转json
            String result = JSONObject.toJSONString(totalNum);
            return result;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 获取离线设备个数，group by project
     * @return
     */
    @Override
    public String getOffDevCountByProject() {
        try{
            List<ProjectCountBean> offCount = visualInterfaceMapper.getOffDevCountByProject();
            //javaBean转json
            String result = JSONObject.toJSONString(offCount);
            return result;
        }catch (Exception e){

        }
        return null;
    }

    /**
     * 离线网关
     * @return
     */
    @Override
    public String getOffGtwCountByProject() {
        try{
            List<ProjectCountBean> gtwCount = visualInterfaceMapper.getOffGtwCountByProject();
            //javaBean转json
            String result = JSONObject.toJSONString(gtwCount);
            return result;
         }catch (Exception e){

        }
        return null;
    }

    //24小时无数据设备group by project
    @Override
    public String getNodeDataByProject() {
        try{
            List<ProjectCountBean> nodeData = visualInterfaceMapper.getNodeDataByProject();
            //javaBean转json
            String result = JSONObject.toJSONString(nodeData);
            return result;
        }catch (Exception e){

        }
        return null;
    }

    /**
     * 抄表失败group by project
     * @return
     */
    @Override
    public String getReadFailedByProject() {
        try{
            List<ProjectCountBean> readFailed = visualInterfaceMapper.getReadFailedByProject();
            //javaBean转json
            String result = JSONObject.toJSONString(readFailed);
            return result;
        }catch (Exception e){

        }
        return null;
    }

    /**
     * 设备总数group by project
     * @return
     */
    @Override
    public String getTotalCountByProject() {
        try{
            List<ProjectCountBean> totalCount = visualInterfaceMapper.getTotalCountByProject();
            //javaBean转json
            String result = JSONObject.toJSONString(totalCount);
            return result;
        }catch (Exception e){

        }
        return null;
    }
}
