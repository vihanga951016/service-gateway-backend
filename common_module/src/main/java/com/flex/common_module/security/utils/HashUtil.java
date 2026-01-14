package com.flex.common_module.security.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
public class HashUtil {
    public static String hash(String pw) {

        return BCrypt.hashpw(pw, BCrypt.gensalt(12));

    }

    public static boolean checkEncrypted(String candidate, String hash) {

        return BCrypt.checkpw(candidate, hash);

    }
}
