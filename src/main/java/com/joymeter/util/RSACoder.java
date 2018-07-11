package com.joymeter.util;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Base64;


/**
 * @ClassName RSACoder
 * @Description TODO
 * 加密获取token
 * @Author liang
 * @Date 2018/7/11 11:09
 * @Version 1.0
 **/
public class RSACoder {
    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data 加密数据
     * @param privateKey 私钥
     *
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = decryptBASE64(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(priKey);
        signature.update(data);
        return encryptSafeBASE64(signature.sign());
    }

    /**
     * 校验数字签名
     *
     * @param data 加密数据
     * @param publicKey 公钥
     * @param sign 数字签名
     *
     * @return 校验成功返回true 失败返回false
     * @throws Exception
     *
     */
    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
        // 解密由base64编码的公钥
        byte[] keyBytes = decryptBASE64(publicKey);
        // 构造X509EncodedKeySpec对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        // 取公钥匙对象
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(data);
        // 验证签名是否正常
        return signature.verify(decryptBASE64(sign));
    }

    /**
     * 解密<br>
     * 用私钥解密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] data, String key) throws Exception {
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);
        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 解密<br>
     * 用私钥解密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] data, String key) throws Exception {
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);
        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);
        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * 加密<br>
     * 用公钥加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String key) throws Exception {
        // 对公钥解密
        byte[] keyBytes = decryptBASE64(key);
        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * 加密<br>
     * 用私钥加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, String key) throws Exception {
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);
        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 取得私钥
     *
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return encryptSafeBASE64(key.getEncoded());
    }

    /**
     * 取得公钥
     *
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return encryptSafeBASE64(key.getEncoded());
    }

    /**
     * 初始化密钥
     *
     * @return
     * @throws Exception
     */
    public static Map<String, Object> initKey() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(512);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();// 公钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();// 私钥
        Map<String, Object> keyMap = new HashMap<>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * Base64解码
     *
     * @param key
     * @return
     */
    public static byte[] decryptBASE64(String key) {
        return Base64.decodeBase64(key);
    }

    /**
     * Base64编码
     *
     * @param sign
     * @return
     */
    public static String encryptSafeBASE64(byte[] sign) {
        return Base64.encodeBase64URLSafeString(sign);
    }

    /**
     * Base64编码
     *
     * @param sign
     * @return
     */
    public static String encryptBASE64(byte[] sign) {
        return Base64.encodeBase64String(sign);
    }

    /**
     * 私钥转换成C#格式
     *
     * @param encodedPublicKey
     * @return
     */
    public static String getRSAPrivateKeyAsNetFormat(byte[] encodedPublicKey) {
        try {
            StringBuilder buff = new StringBuilder(1024);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey pukKey = (RSAPublicKey) keyFactory
                    .generatePublic(new X509EncodedKeySpec(encodedPublicKey));
            buff.append("<RSAKeyValue>");
            buff.append("<Modulus>").append(encryptSafeBASE64(removeMSZero(pukKey.getModulus()
                    .toByteArray()))).append("</Modulus>");
            buff.append("<Exponent>").append(encryptSafeBASE64(removeMSZero(pukKey.getPublicExponent()
                    .toByteArray()))).append("</Exponent>");
            buff.append("</RSAKeyValue>");
            return buff.toString();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(RSACoder.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * @param data
     * @return
     */
    private static byte[] removeMSZero(byte[] data) {
        byte[] data1;
        int len = data.length;
        if (data[0] == 0) {
            data1 = new byte[data.length - 1];
            System.arraycopy(data, 1, data1, 0, len - 1);
        } else {
            data1 = data;
        }
        return data1;
    }

    /**
     * 获取access_token
     *
     * @return
     * @author lijian
     */
    public static String getSafeAccessToken() {
        try {
            String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJQeFrVhmHoWYNwPkXFVScpdwsZ/BnVhsUuGGvozfgcyde6Q7nFaTmvNBGuxbSqsSmatQLKEZWkPDDzP/Yv7zPcCAwEAAQ==";
            String inputStr = String.format("{\"client_id\":\"joy000001\",\"datetime\":\"%s\"}", new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis()));
            byte[] data = inputStr.getBytes();
            byte[] encodedData;
            encodedData = RSACoder.encryptByPublicKey(data, publicKey);
            String access_token = encryptSafeBASE64(encodedData);
            return access_token;
        } catch (Exception e) {
            Logger.getLogger(RSACoder.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }

    }

    /**
     * 获取access_token
     *
     * @return
     * @author lijian
     */
    public static String getAccessToken() {
        try {
            String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJQeFrVhmHoWYNwPkXFVScpdwsZ/BnVhsUuGGvozfgcyde6Q7nFaTmvNBGuxbSqsSmatQLKEZWkPDDzP/Yv7zPcCAwEAAQ==";
            String inputStr = String.format("{\"client_id\":\"joy000001\",\"datetime\":\"%s\"}", new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis()));
            byte[] data = inputStr.getBytes();
            byte[] encodedData;
            encodedData = RSACoder.encryptByPublicKey(data, publicKey);
            String access_token = RSACoder.encryptBASE64(encodedData);
            return access_token;
        } catch (Exception e) {
            Logger.getLogger(RSACoder.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
}
