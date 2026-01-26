package com.flex.common_module.security.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/25/2026
 */
public class CryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "23309Yvg@13fdrh5";

    public static String encrypt(String value) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    public static String decrypt(String value) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(value)));
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}
