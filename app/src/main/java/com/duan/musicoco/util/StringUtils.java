package com.duan.musicoco.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by DuanJiaNing on 2017/6/9.
 */

public class StringUtils {

    public static String stringToMd5(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String getGenTime(int misec) {
        int min = misec / 1000 / 60;
        int sec = (misec / 1000) % 60;
        String minStr = min < 10 ? "0" + min : min + "";
        String secStr = sec < 10 ? "0" + sec : sec + "";
        return minStr + ":" + secStr;
    }

}
