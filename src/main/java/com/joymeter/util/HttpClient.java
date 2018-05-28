package com.joymeter.util;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpClient {

    private static final Logger logger = Logger.getLogger(HttpClient.class.getName());
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(20000).setConnectTimeout(20000).build();

    private HttpClient() {
    }

    /**
     * 向服务器发送get请求
     *
     * @param url
     * @return
     */
    public static String sendGet(String url) {
        if (StringUtils.isEmpty(url)) return "";
        String result = "";
        //实例化一个HttpGet对象
        HttpGet httpGet = new HttpGet(url);
        //设置请求响应配置
        httpGet.setConfig(requestConfig);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            logger.log(Level.INFO, "当前请求的地址为：" + httpGet.getURI());
            //如果返回值为200，则请求成功，可以通过TestNG做判断 HttpStatus.SC_OK
            int status = response.getStatusLine().getStatusCode();
            logger.log(Level.INFO, "当前请求URL状态：" + status);
            //获取Http Headers信息
            Header[] headers = response.getAllHeaders();
            int headerLength = headers.length;
            for (int i = 0; i < headerLength; i++) {
                logger.log(Level.INFO, "Header内容为：" + headers[i]);
            }
            //获取到请求的内容
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            logger.log(Level.INFO, "获取请求响应的内容为：" + result);
        } catch (IOException | ParseException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } finally {
            httpGet.releaseConnection();//关闭响应
            //httpClient.close();
        }
        return result;
    }

    /**
     * 向服务器发送post请求，参数类型为key-value
     *
     * @param url
     * @param params
     * @return
     */
    public static String sendPost(String url, Map<String, String> params) {
        if (StringUtils.isEmpty(url) || params == null || params.isEmpty()) return "";
        String result = "";
        HttpPost httpPost = new HttpPost(url);
        logger.log(Level.INFO, "当前请求的地址为: " + httpPost.getURI());
        logger.log(Level.INFO, "当前请求的参数为: " + params);
        httpPost.setConfig(requestConfig);
        List<NameValuePair> list = new ArrayList<>();
        params.entrySet().forEach(e -> list.add(new BasicNameValuePair(e.getKey(), e.getValue())));
        CloseableHttpResponse response = null;
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            logger.log(Level.INFO, "获取请求响应的内容为：" + result);
        } catch (IOException | ParseException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } finally {
            try {
                if (response != null) response.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
            httpPost.releaseConnection();
            //httpClient.close();
        }
        return result;
    }

    /**
     * 向服务器发送post请求，参数类型为JSON格式字符串
     *
     * @param url
     * @param jsonParams
     * @return
     */
    public static String sendPost(String url, String jsonParams) {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(jsonParams)) return "";
        String result = "";
        HttpPost httpPost = new HttpPost(url);
        //logger.log(Level.INFO, "当前请求的地址为: " + httpPost.getURI());
        //logger.log(Level.INFO, "当前请求的参数为: " + jsonParams);
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-Type", "application/json");
        CloseableHttpResponse response = null;
        try {
            httpPost.setEntity(new StringEntity(jsonParams, "UTF-8"));
            response = httpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            result = EntityUtils.toString(httpEntity, "UTF-8");
            //logger.log(Level.INFO, "获取请求响应的内容为：" + result);
        } catch (IOException | ParseException e) {
            logger.log(Level.SEVERE, e.getMessage()+"请求内容为："+jsonParams+"请求结果为:"+response);
        } finally {
            try {
                if (response != null) response.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
            httpPost.releaseConnection();
            //httpClient.close();
        }
        return result;
    }
}
