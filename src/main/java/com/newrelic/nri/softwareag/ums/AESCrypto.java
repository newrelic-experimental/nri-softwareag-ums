
package com.newrelic.nri.softwareag.ums;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCrypto {

    private static final String AES_ALGORITHM = "AES";
    private static final int KEY_SIZE = 128; // 128-bit key size
    private static String MANGLED_PREFIX = "-UMS-";

    public static String generateRandomAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(KEY_SIZE, new SecureRandom());
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static String encrypt(String plainText, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM + "/CBC/PKCS5Padding");

        byte[] ivBytes = new byte[cipher.getBlockSize()];
        new SecureRandom().nextBytes(ivBytes);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(ivBytes) + MANGLED_PREFIX + Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText, String key) throws Exception {
        String[] parts = encryptedText.split(MANGLED_PREFIX);
        byte[] ivBytes = Base64.getDecoder().decode(parts[0]);
        byte[] encryptedBytes = Base64.getDecoder().decode(parts[1]);

        SecretKeySpec secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM + "/CBC/PKCS5Padding");

        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage:");
            System.out.println("encyptPwd.sh <option> [arguments]");
            System.out.println("Options:");
            System.out.println("  generateKey");
            System.out.println("  encryptPassword <aesKey> <password>");
            return;
        }

        String option = args[0];
        switch (option) {
            case "generateKey":
                generateAESKey();
                break;
            case "encryptPassword":
                if (args.length != 3) {
                    System.out.println("Usage: encyptPwd.sh encryptPassword <aesKey> <password>");
                    return;
                }
                String aesKey = args[1];
                String password = args[2];
                encryptPassword(aesKey, password);
                break;
            default:
                System.out.println("Invalid option. Use 'generateKey' or 'encryptPassword'");
        }
    }

    private static void generateAESKey() {
        try {
            String aesKey = AESCrypto.generateRandomAESKey();
            System.out.println("Generated AES Key: " + aesKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void encryptPassword(String aesKey, String password) {
        try {
            String encryptedPassword = AESCrypto.encrypt(password, aesKey);
            System.out.println("Encrypted Password: " + encryptedPassword);
            if (AESCrypto.decrypt(encryptedPassword,aesKey).compareTo(password)==0)
            		System.out.println("Success ! " );
            else
            	System.out.println("Failure ! " );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
