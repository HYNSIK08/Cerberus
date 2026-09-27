package com.secauth.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    public static String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean verify(String plaintext, String hash) {
        if (hash == null || !hash.startsWith("$2")) return false;
        return BCrypt.checkpw(plaintext, hash);
    }
}
