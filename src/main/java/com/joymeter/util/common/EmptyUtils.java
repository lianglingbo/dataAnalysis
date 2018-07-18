package com.joymeter.util.common;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * @ClassName EmptyUtils
 * @Description TODO
 * 1.对象是否为空
 * 2.字符串长度是否为0
 * 3.数组长度是否为0
 * 4.集合是否为空
 *
 * @Author liang
 * @Date 2018/7/18 10:55
 * @Version 1.0
 **/
public class EmptyUtils {

    public static boolean isEmpty(Object obj) {
        if(obj == null){
            return  true;
        }
        if(obj instanceof  String && obj.toString().length() == 0){
            return true;
        }
        if(obj.getClass().isArray() && Array.getLength(obj) == 0){
            return true;
        }
        if(obj instanceof Collection && ((Collection) obj).isEmpty()){
            return true;
        }
        if (obj instanceof Map && ((Map) obj).isEmpty()) {
            return true;
        }
        return false;

    }
}

