package com.joymeter.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回信息工具类
 * @author Pan Shuoting
 *
 */
public class ResultUtil {
    /**
     * 请求成功返回
     * @param object
     * @return
     */
    public static Map<String, Object> success(Object object){
        Map<String, Object> msg=new HashMap<String, Object>();
        msg.put("code",200);
        msg.put("msg","请求成功");
        msg.put("data",object);
        return msg;
    }
    public static Map<String, Object>  success(){
        return success(null);
    }
 
    public static Map<String, Object>  error(Integer code,String resultmsg){
    	Map<String, Object> msg=new HashMap<String, Object>();
        msg.put("code",code);
        msg.put("msg",resultmsg);
        return msg;
    }
}
