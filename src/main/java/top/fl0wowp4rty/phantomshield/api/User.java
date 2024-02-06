package top.fl0wowp4rty.phantomshield.api;

import top.fl0wowp4rty.phantomshield.annotations.Native;

import java.security.SecureRandom;

/**
 * 获取授权登陆后信息
 */

@Native
public class User {
    private static final SecureRandom RANDOM = new SecureRandom();
    public static User INSTANCE = new User();
    private String cachedUsername;
    private String cachedHwid;
    private byte[] cachedData;
    private String cachedUid;
    private short[] cachedDate;

    private User() {
    }

    /**
     * 获取用户HWID
     *
     * @return 用户硬件信息，默认为dev
     */
    public String getHwid() {
        if (cachedHwid != null) return cachedHwid;
        try {
            Class<?> clz = Class.forName("xyz.blackfaithfully.License");
            return cachedHwid = (String)
                clz.getDeclaredMethod("invoke", int.class, Object.class).invoke(null, -2095861531, null);
        } catch (Throwable ignored) {
        }

        return cachedHwid = "dev";
    }

    /**
     * @return 获取用户UID或者卡密信息，默认为dev
     */
    public String getUid() {
        if (cachedUid != null) return cachedUid;
        try {
            Class<?> clz = Class.forName("xyz.blackfaithfully.License");
            return cachedUid = (String) clz.getDeclaredField("uid").get(null);
        } catch (Throwable ignored) {
        }

        return cachedUid = "dev";
    }

    /**
     * @return 返回日期数组，数组大小为3<br>
     * date[0] - 年<br>
     * date[1] - 月<br>
     * date[2] - 日<br>
     * 永久用户则返回null
     */
    public short[] getExpiredDate() {
        if (cachedDate != null) return cachedDate;
        try {
            Class<?> clz = Class.forName("xyz.blackfaithfully.License");
            short year = (short)
                clz.getDeclaredMethod("invoke", int.class, Object.class).invoke(null, -1311305163, null);
            if (year == 0)
                return null;
            short month = (short)
                clz.getDeclaredMethod("invoke", int.class, Object.class).invoke(null, -1389533409, null);
            short day = (short)
                clz.getDeclaredMethod("invoke", int.class, Object.class).invoke(null, -1346052582, null);
            return cachedDate = new short[]{year, month, day};
        } catch (Throwable ignored) {
        }

        return cachedDate = new short[]{9999, 12, 31};
    }

    /**
     * @param defaultName 开发环境默认返回值，如果为null则默认为dev
     * @return 用户昵称
     */
    public String getUsername(String defaultName) {
        if (cachedUsername != null) return cachedUsername;
        try {
            Class<?> clz = Class.forName("xyz.blackfaithfully.License");
            return cachedUsername = (String)
                clz.getDeclaredMethod("invoke", int.class, Object.class).invoke(null, -1056577427, null);
        } catch (Throwable ignored) {
        }

        return cachedUsername = defaultName == null ? "dev" : defaultName;
    }

    /**
     * @param code 由keygen生成的激活码
     * @return 返回0则正常 <br>
     * 返回0x00000008 激活码到期 <br>
     * 返回0x00000020 hwid不匹配 <br>
     * 返回其他值则异常
     */
    public int authenticate(String code) {
        try {
            Class<?> clz = Class.forName("xyz.blackfaithfully.License");
            return (int)
                clz.getDeclaredMethod("invoke", int.class, Object.class).invoke(null, -264292592, code);
        } catch (Throwable ignored) {
        }

        return -1;
    }

    /**
     * @return 用户数据
     */
    public byte[] getUserData() {
        if (cachedData != null) return cachedData;
        try {
            Class<?> clz = Class.forName("xyz.blackfaithfully.License");
            byte[] bytes = (byte[])
                clz.getDeclaredMethod("invoke", int.class, Object.class).invoke(null, -226895209, null);
            return cachedData = bytes;
        } catch (Throwable ignored) {
        }

        return cachedData = new byte[0];
    }
}
