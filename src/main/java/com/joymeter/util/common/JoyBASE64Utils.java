package com.joymeter.util.common;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @ClassName JoyBASE64Utils
 * @Description TODO
 * base64 加密解密
 * @Author liang
 * @Date 2018/7/18 17:43
 * @Version 1.0
 **/
public class JoyBASE64Utils {
    public static String decode(String base64String){
        Base64.Decoder decoder = Base64.getDecoder();
        try {
            String decodeString = new String(decoder.decode(base64String), "UTF-8");
            return decodeString;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

    }
}
