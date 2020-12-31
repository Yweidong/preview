package com.haoyong.preview.util;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.MessageDigest;

/**
 * @program: auth
 * @description: 字符串加密
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-11-13 09:37
 **/
public class DeshfuUtil {
    final static DeshfuUtil INSTANCE = new DeshfuUtil();
    public static DeshfuUtil getInstance() {
        return INSTANCE;
    }
    private static final String PASSWORD_CRYPT_KEY = "TOGETHER!@#$%";


    // 解密数据
    public    String decrypt(String message) {
        try{
            byte[] bytesrc = convertHexString(message);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(JavaMD5.getMD5ofStr(PASSWORD_CRYPT_KEY)
                    .substring(0, 8).getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(JavaMD5.getMD5ofStr(PASSWORD_CRYPT_KEY)
                    .substring(0, 8).getBytes("UTF-8"));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] retByte = cipher.doFinal(bytesrc);
            return new String(retByte);
        }catch (Exception e){
            return null;
        }

    }

    //加密数据
    public String encrypt(String message) {
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(JavaMD5.getMD5ofStr(PASSWORD_CRYPT_KEY)
                    .substring(0, 8).getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);//    根据提供的密钥规范生成 SecretKey 对象。
            IvParameterSpec iv = new IvParameterSpec(JavaMD5.getMD5ofStr(PASSWORD_CRYPT_KEY)
                    .substring(0, 8).getBytes("UTF-8"));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            return toHexString(cipher.doFinal(message.getBytes("UTF-8")));
        }catch (Exception e){
           return null;
        }


    }

    private static byte[] convertHexString(String ss) throws Exception {
        if(ss.length() % 2 == 1) {
            throw new Exception("token is error");
        }
        byte digest[] = new byte[ss.length() / 2];
        for (int i = 0; i < digest.length; i++) {
            String byteString = ss.substring(2 * i, 2 * i + 2);
            int byteValue = Integer.parseInt(byteString, 16);
            digest[i] = (byte) byteValue;
        }
        return digest;
    }

    private static String toHexString(byte b[]) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String plainText = Integer.toHexString(0xff & b[i]);//方法返回为无符号整数基数为16的整数参数的字符串表示形式。
            if (plainText.length() < 2)
                plainText = "0" + plainText;
            hexString.append(plainText);
        }
        return hexString.toString();
    }

    private static class JavaMD5 {

        public static String getMD5ofStr(String passwordCryptKey) {
            try{
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(passwordCryptKey.getBytes());
                byte[] bytes = md5.digest();
                return toHexString(bytes).toUpperCase();
            }catch (Exception e) {
               return null;
            }

        }
    }

//    public static void main(String[] args) throws Exception {
//
//        DeshfuUtil desUtil = new DeshfuUtil();
//        System.out.println("加密:"+encrypt("abc")
//                .toUpperCase());
//        System.out.println("解密:"+decrypt("AA85ADEB848E29791"));
//    }
}
