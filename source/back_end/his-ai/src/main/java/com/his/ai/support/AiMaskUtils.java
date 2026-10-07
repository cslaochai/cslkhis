package com.his.ai.support;

import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * 脱敏兜底工具。
 * <p>
 * <b>这不是主要手段，是最后一道网。</b> 正确的做法是从源头规避 ——
 * 组装上下文时只按 patientId 取临床字段，根本不去查姓名、身份证、手机号。
 * 但「难免有人图省事把整行数据塞进来」，所以在写审计日志前再过一道正则。
 * <p>
 * 注意：正则只能挡住格式规整的号码。姓名是挡不住的，只能靠源头不查。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
