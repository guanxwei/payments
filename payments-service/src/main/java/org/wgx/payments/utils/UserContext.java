package org.wgx.payments.utils;

/**
 * A request level context providing some useful information about the user.
 * @author weigu
 *
 */
public final class UserContext {

    private UserContext() { }

    /**
     * 用户id
     */
    private static final ThreadLocal<String> USER = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "";
        }
    };

    /**
     * 用户昵称
     */
    private static ThreadLocal<String> NICKNAME = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "Anonymous";
        }
    };

    /**
     * 客户端ip
     */
    private static ThreadLocal<String> IP = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "";
        }
    };

    public static void clear() {
        USER.remove();
        IP.remove();
    }

    public static String getUser() {
        return UserContext.USER.get();
    }

    public static void setUserId(String userId) {
        UserContext.USER.set(userId);
    }

    public static String getNickName() {
        return UserContext.NICKNAME.get();
    }

    public static void setUserNickName(String nickName) {
        UserContext.NICKNAME.set(nickName);
    }

    public static String getClientIp() {
        return UserContext.IP.get();
    }

    public static void setClientIp(String ip) {
        UserContext.IP.set(ip);
    }

}
