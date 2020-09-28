package io.jingwei.wallet.rest;

import org.jasypt.util.text.BasicTextEncryptor;

public class EncriptTest {
    public static void main(String[] args) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        // 加密密钥
        textEncryptor.setPassword("fin-wallet-service*");
        // 要加密的数据（如数据库的用户名或密码）
        String username = textEncryptor.encrypt("fin-wallet-service");
        String password = textEncryptor.encrypt("123");
        System.out.println("username:" + username);
        System.out.println("password:" + password);
    }
}
