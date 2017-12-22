package org.wgx.payments.utils;

import java.security.MessageDigest;

import org.apache.log4j.Logger;

/**
 * Wechat used MD5 helper.
 */
public final class WechatPayMD5 {

    private static final String[] HEX = {"0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "a", "b", "c", "d", "e", "f"};

    private static Logger logger = Logger.getLogger("wechat");

    private WechatPayMD5() { }

    /**
     * 转换字节数组为16进制字串.
     * @param b 字节数组
     * @return 16进制字串
     */
    public static String byteArrayToHexString(final byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (byte aB : b) {
            resultSb.append(byteToHexString(aB));
        }
        return resultSb.toString();
    }

    /**
     * 转换byte到16进制.
     * @param b 要转换的byte
     * @return 16进制格式
     */
    private static String byteToHexString(final byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return HEX[d1] + HEX[d2];
    }

    /**
     * MD5编码.
     * @param origin 原始字符串
     * @return 经过MD5加密之后的结果
     */
    public static String md5Encode(final String origin) {
        String resultString = null;
        try {
            resultString = origin;
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes("UTF-8")));
        } catch (Exception e) {
            logger.error("error>md5Encode", e);
        }
        return resultString;
    }

}
