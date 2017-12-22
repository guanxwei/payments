package org.wgx.payments.utils;

public class UserContext {
    /**
     * 用户id
     */
    private static ThreadLocal<String> user = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "";
        }
    };
    
    /**
     * 用户昵称
     */
    private static ThreadLocal<String> nickName = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "Anonymous";
        }
    };

    /**
     * 客户端ip
     */
    private static ThreadLocal<String> clientIp = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "";
        }
    };

    private static ThreadLocal<Long> clientVersion = new ThreadLocal<>();

    public static void clear() {
        user.remove();
        clientIp.remove();
    }

    public static String getUser() {
        return UserContext.user.get();
    }

    public static void setUserId(String userId) {
        UserContext.user.set(userId);
    }

    public static String getNickName() {
        return UserContext.nickName.get();
    }

    public static void setUserNickName(String nickName) {
        UserContext.nickName.set(nickName);
    }

    public static String getClientIp() {
        return UserContext.clientIp.get();
    }

    public static void setClientIp(String ip) {
        UserContext.clientIp.set(ip);
    }

    public static Long getClientVersion() {
        return clientVersion.get();
    }

    public static void setClientVersion(Long clientVersion) {
        UserContext.clientVersion.set(clientVersion);
    }
}
