package com.joymeter.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 属性文件工具类
 *
 * @author Wu Wei
 * @version 2018-01-08 17:15:19
 */
public class PropertiesUtils {

    private static final Logger logger = Logger.getLogger(PropertiesUtils.class.getName());
    private static final String filePath = System.getProperty("user.dir") + "//serverAnalysis.properties";

    private PropertiesUtils() {
    }

    /**
     * 读取属性值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getProperty(String key, String defaultValue) {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            prop.load(fis);
            return prop.getProperty(key, defaultValue);
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
        return defaultValue;
    }

    /**
     * 修改属性值
     *
     * @param key
     * @param value
     */
    public static void setProperty(String key, String value) {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath);
             FileOutputStream fos = new FileOutputStream(filePath)) {
            prop.load(fis);
            prop.setProperty(key, value);
            prop.store(fos, null);
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }
}
