package com.his.common.util;

/**
 * 敏感字段脱敏 —— 手机号 / 证件号 / 邮箱。
 */
public final class SensitiveMaskUtil {

    /** 直辖市：省与市同一级，地址常写成「北京市朝阳区…」 */
    private static final String[] MUNICIPALITIES = {"北京", "上海", "天津", "重庆"};

    /** 省级后缀（按最长优先匹配） */
    private static final String[] PROVINCE_MARKS = {"特别行政区", "自治区", "省"};

    /** 市级后缀（按最长优先匹配） */
    private static final String[] CITY_MARKS = {"自治州", "地区", "盟", "市"};

    /**
     * 手机号：18878885878 → 188****5878；带区号固话保住区号 0731-****6666
     */
    public static String maskPhone(String v) {
        String s = TextUtil.trimToEmpty(v);
        if (s.isEmpty()) {
            return null;
        }
        int dash = s.indexOf('-');
        if (dash > 0) {
            return s.substring(0, dash + 1) + maskMiddle(s.substring(dash + 1), 0, 4);
        }
        return maskMiddle(s, 3, 4);
    }

    /**
     * 身份证号：430726199709180511 → 4307**********0511（前 4 是省市，后 4 足以核对是否同一张证）
     */
    public static String maskIdCard(String v) {
        String s = TextUtil.trimToEmpty(v);
        return s.isEmpty() ? null : maskMiddle(s, 4, 4);
    }

    /**
     * 邮箱：1688888@qq.com → 168****8@qq.com（域名保留，用户名打码）
     */
    public static String maskEmail(String v) {
        String s = TextUtil.trimToEmpty(v);
        if (s.isEmpty()) {
            return null;
        }
        int at = s.indexOf('@');
        if (at <= 0) {
            return maskMiddle(s, 1, 1);
        }
        return maskMiddle(s.substring(0, at), 3, 0) + s.substring(at);
    }

    /**
     * 卡号（医保卡号 / 就诊卡号）：固定 4 星而不是等长星号，长度本身不泄露。
     * <p>短号只留首尾各 2 位，7 位以下全遮 —— 露出过半就不算脱敏了。
     */
    public static String maskCardNo(String v) {
        String s = TextUtil.trimToEmpty(v);
        if (s.isEmpty()) {
            return null;
        }
        int len = s.length();
        if (len >= 12) {
            return s.substring(0, 4) + "****" + s.substring(len - 4);
        }
        if (len >= 8) {
            return s.substring(0, 2) + "****" + s.substring(len - 2);
        }
        return "****";
    }

    /**
     * 姓名：留姓（首字），其余打星。单字名全遮。例：张三 → 张*；欧阳修 → 欧**。
     */
    public static String maskName(String v) {
        String s = TextUtil.trimToEmpty(v);
        if (s.isEmpty()) {
            return null;
        }
        int len = s.length();
        if (len == 1) {
            return "*";
        }
        return s.substring(0, 1) + repeat('*', len - 1);
    }

    /**
     * 住址：只保留省、市两级行政区划，区县/街道/门牌一律打星。
     *
     * <p>口径：直辖市取到「北京市」，省级取到「省 / 自治区 / 特别行政区」，市级取到
     * 「市 / 自治州 / 地区 / 盟」；认不出省市的自由文本整段打码（宁可看不到，也不漏门牌）。
     * 星号与原文字等长，便于「是同一条档案」的核对，但不足以还原门牌。
     */
    public static String maskAddress(String v) {
        String s = TextUtil.trimToEmpty(v);
        if (s.isEmpty()) {
            return null;
        }
        int keep = regionPrefixLength(s);
        if (keep <= 0) {
            return repeat('*', s.length());
        }
        if (keep >= s.length()) {
            return s;
        }
        return s.substring(0, keep) + repeat('*', s.length() - keep);
    }

    /**
     * 住址里省、市两级行政区划的结束位置（即可以保留的前缀长度）。
     *
     * <p>「省」和「市」在中文地址里是仅有的两级稳定边界，区县以下写法五花八门
     * （区/县/旗/镇/街道/乡/村/路/号/栋/室都有），按固定字数截断必然把某类地址漏出去，
     * 所以只认这两个行政区划后缀。
     *
     * @return 可保留的字符数；0 = 认不出行政区划，整条打码
     */
    private static int regionPrefixLength(String s) {
        // 直辖市：省与市同一级，取「北京市」三个字（不写「市」的老地址退化为保留三字）
        for (String city : MUNICIPALITIES) {
            if (s.startsWith(city)) {
                int cityEnd = s.indexOf('市');
                return cityEnd >= 0 && cityEnd <= city.length() ? cityEnd + 1 : Math.min(city.length(), s.length());
            }
        }
        int provinceEnd = endOfFirst(s, 0, PROVINCE_MARKS);
        if (provinceEnd > 0) {
            int cityEnd = endOfFirst(s, provinceEnd, CITY_MARKS);
            return cityEnd > 0 ? cityEnd : provinceEnd;
        }
        // 没写省，只写了市（「长沙市岳麓区…」）：市级后缀至少落在第 3 个字才认
        int cityEnd = endOfFirst(s, 0, CITY_MARKS);
        return cityEnd >= 3 ? cityEnd : 0;
    }

    /**
     * 从 from 起找最早出现的某个后缀，返回该后缀之后的字符位置。
     *
     * @return -1 = 一个都没找到
     */
    private static int endOfFirst(String s, int from, String... marks) {
        int bestStart = -1;
        int bestLength = 0;
        for (String mark : marks) {
            int index = s.indexOf(mark, from);
            if (index < 0) {
                continue;
            }
            if (bestStart < 0 || index < bestStart || (index == bestStart && mark.length() > bestLength)) {
                bestStart = index;
                bestLength = mark.length();
            }
        }
        return bestStart < 0 ? -1 : bestStart + bestLength;
    }

    /**
     * 中间打码：保留前 head 位 + 后 tail 位，中间补 {@code *}。
     *
     * <p>长度不足 head+tail 时退化为只留首尾各一位，避免出现「全露出」或「负数星号」。
     */
    public static String maskMiddle(String v, int head, int tail) {
        String s = TextUtil.trimToEmpty(v);
        if (s.isEmpty()) {
            return "";
        }
        int len = s.length();
        if (len <= 2) {
            return repeat('*', len);
        }
        if (len <= head + tail) {
            return s.substring(0, 1) + repeat('*', len - 2) + s.substring(len - 1);
        }
        return s.substring(0, head) + repeat('*', len - head - tail) + s.substring(len - tail);
    }

    private static String repeat(char c, int n) {
        return n <= 0 ? "" : String.valueOf(c).repeat(n);
    }

}
