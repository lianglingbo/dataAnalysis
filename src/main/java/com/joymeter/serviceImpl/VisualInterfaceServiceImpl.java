package com.joymeter.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.joymeter.entity.DeviceInfo;
import com.joymeter.entity.DeviceInfos;
import com.joymeter.entity.ProjectCountBean;
import com.joymeter.entity.TotalNumBean;
import com.joymeter.mapper.VisualInterfaceMapper;
import com.joymeter.service.VisualInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

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

    /**
     * 24小时无数据设备列表动态分组,动态聚合查询
     * @return
     */
    @Override
    public List<HashMap<String, Object>> getNoneDataGroupList(String data) {
        if (StringUtils.isEmpty(data))return null;
        //打印日志
        try {
            DeviceInfos deviceInfos = JSONObject.parseObject(data,DeviceInfos.class);

           return  visualInterfaceMapper.getNoneDataGroupList(deviceInfos);
        } catch (Exception e) {
            //日志
        }
        return null;
    }

    /**
     * 24小时无数据查询设备列表详情，按地区信息查询
     * @param data
     * @return
     */
    @Override
    public List<HashMap<String, Object>> getNoneDataByParams(String data) {
        if (StringUtils.isEmpty(data))return null;
        //打印日志
        try {
            DeviceInfos deviceInfos = JSONObject.parseObject(data,DeviceInfos.class);
            System.out.println(deviceInfos.toString());
            return  visualInterfaceMapper.getNoneDataByParams(deviceInfos);
        } catch (Exception e) {
            //日志
        }

        return null;
    }

    /**
     * 设备列表详情页面，传入不同参数，展示不同列表（离线，抄表失败，一天无数据）
     * @param data
     * @return
     */
    @Override
    public List<HashMap<String, Object>> getDeviceInfosByArgs(String data) {
        if (StringUtils.isEmpty(data))return null;
        //打印日志
        try {
            return  visualInterfaceMapper.getDeviceInfosByArgs(data);
        } catch (Exception e) {
            //日志
        }
        return null;
    }

    /**
     * 通过项目名称获取项目首页地址
     * @param project
     * @return
     */
    @Override
    public String getURLByProject(String project) {
        if (StringUtils.isEmpty(project))return null;
        //打印日志
        try {
            return  visualInterfaceMapper.getURLByProject(project);
        } catch (Exception e) {
            //日志
        }
        return null;
    }


    /**
     * 获取可疑用水的用水和项目信息
     * @param data
     * @return
     */
    @Override
    public List<HashMap<String, Object>> getUsageWithProjectByParams(String data) {
        if (StringUtils.isEmpty(data))return null;


        try {
            JSONObject jsonObject = JSONObject.parseObject(data);
            DeviceInfo deviceInfo = new DeviceInfo(jsonObject);

            return visualInterfaceMapper.getUsageWithProjectByParams(deviceInfo);
        } catch (Exception e) {

            return null;
        }
    }

    //getAllUsageInfos查询可疑用水的详细信息展示所有
    @Override
    public List<HashMap<String, Object>> getAllUsageInfos( ) {
        try {
            return visualInterfaceMapper.getAllUsageInfos();
        } catch (Exception e) {
            return null;
        }
    }
}
