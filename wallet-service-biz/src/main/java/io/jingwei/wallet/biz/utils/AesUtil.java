package io.jingwei.wallet.biz.utils;

import io.jingwei.wallet.biz.exception.CryptException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.LoggerFactory;


import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AesUtil {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AesUtil.class);

    /**
     * @param content  需要加密的内容
     * @param password 加密密码 16bytes长度
     * @return 返回是加密的bytes流
     */
    private static byte[] encryptToBytes(String content, String password) {
        SecretKey key = new SecretKeySpec(password.getBytes(), "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器 AES/ECB/PKCS5Padding
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            return cipher.doFinal(byteContent);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            logger.error("AES encrypt to bytes failed" + e.getMessage(), e);
            throw new CryptException("AES encrypt to bytes failed");
        }
    }

    /**
     * 解密
     *
     * @param content  待解密内容
     * @param password 解密密钥
     * @return
     */
    private static byte[] decryptToBytes(byte[] content, String password) {
        try {
            SecretKey key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            return cipher.doFinal(content);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            logger.error("AES decrypt to bytes failed" + e.getMessage(), e);
            throw new CryptException("AES decrypt to bytes failed");
        }
    }

    /**
     * 生成加密文件
     *
     * @param content
     * @param password
     * @param filePath
     */
    public static void encryptToFile(String content, String password, String filePath) {
        File file = new File(filePath);
        OutputStream output = null;
        BufferedOutputStream bufferedOutput = null;
        try {
            output = new FileOutputStream(file);
            bufferedOutput = new BufferedOutputStream(output);
            //加密形成文件
            byte[] bytes = encryptToBytes(content, password);
            bufferedOutput.write(bytes);
        } catch (IOException e) {
            // todo lzp MonitorLogUtils
            logger.error("AES encrypt to file failed " + e.getMessage(), e);
            throw new CryptException("AES encrypt to file failed");
        } finally {
            if (bufferedOutput != null) {
                try {
                    bufferedOutput.close();
                } catch (IOException e) {
                    logger.error("BufferedOutputStream closed failed " + e.getMessage(), e);
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    logger.error("FileOutputStream closed failed " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 从加密文件解密成为string
     *
     * @param password
     * @param filePath
     * @return
     */
    public static String decryptFromFile(String password, String filePath) {
        File file = new File(filePath);
        InputStream input = null;
        ByteArrayOutputStream bos = null;
        try {
            input = new FileInputStream(file);
            bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = input.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            byte[] contentBytes = bos.toByteArray();
            //解密成byte
            // decryptToBytes(contentBytes, password);
            return new String(decryptToBytes(contentBytes, password));
        } catch (IOException e) {
            // todo lzp MonitorLogUtils
            logger.error("AES decrypt to file failed " + e.getMessage(), e);
            throw new CryptException("AES decrypt to file failed");
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    logger.error("ByteArrayOutputStream closed failed " + e.getMessage(), e);
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("FileInputStream closed failed " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * BASE64解码
     *
     * @param key
     * @return
     */
    private static byte[] decryptBASE64(String key) {
        return Base64.decodeBase64(key);
    }

    /**
     * BASE64编码
     *
     * @param key
     * @return
     */
    private static String encryptBASE64(byte[] key) {
        return Base64.encodeBase64String(key);
    }

    /**
     * 先aes再base64
     *
     * @param content
     * @param password
     * @return
     */
    public static String encryptToString(String content, String password) {
        byte[] encryptBase64Bytes = encryptToBytes(content, password);
        return encryptBASE64(encryptBase64Bytes);
    }

    /**
     * 先base64 再aes
     *
     * @param content
     * @param password
     * @return
     */
    public static String decryptToString(String content, String password) {
        byte[] decryptBase64Bytes = decryptBASE64(content);
        byte[] decryptBytes = decryptToBytes(decryptBase64Bytes, password);
        return new String(decryptBytes);
    }

    public static void main(String[] args) {
        String content = "";
        String resul = content.trim();
        System.out.println("【" + resul + "】");

    }
}
