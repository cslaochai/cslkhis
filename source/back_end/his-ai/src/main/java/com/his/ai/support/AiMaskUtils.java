package com.his.ai.support;

import com.his.common.util.TextUtil;

import java.util.regex.Pattern;

/**
 * 脱敏兜底工具。
 */
public final class AiMaskUtils {

    private static final String MASK_ID_CARD = "[身份证号]";
    private static final String MASK_MOBILE = "[手机号]";

    private static final Pattern ID_CARD_18 = Pattern.compile("(?<!\\d)\\d{17}[\\dXx](?!\\d)");
    private static final Pattern ID_CARD_15 = Pattern.compile("(?<!\\d)\\d{15}(?!\\d)");
    private static final Pattern MOBILE = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");

    private static final int DEFAULT_MAX_LENGTH = 512;

    /**
     * 屏蔽身份证号与手机号。先处理 18 位再处理 15 位，避免 18 位被截成 15 位误判。
     */
    public static String mask(String text) {
        if (!TextUtil.hasText(text)) {
            return "";
        }
        String result = ID_CARD_18.matcher(text).replaceAll(MASK_ID_CARD);
        result = ID_CARD_15.matcher(result).replaceAll(MASK_ID_CARD);
        return MOBILE.matcher(result).replaceAll(MASK_MOBILE);
    }

    /**
     * 生成入库用的摘要：脱敏 → 压缩空白 → 截断
     */
    public static String digest(String text) {
        return digest(text, DEFAULT_MAX_LENGTH);
    }

    public static String digest(String text, int maxLength) {
        if (!TextUtil.hasText(text)) {
            return "";
        }
        String value = mask(text).replaceAll("\\s+", " ").trim();
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }
}
