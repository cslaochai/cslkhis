package com.his.common.support;

import com.his.common.util.TextUtil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 临床文本匹配工具 —— 处理「否定语义」与「空描述」这两件小事。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClinicalTextMatcher {

    /**
     * 否定词。判定窗口取匹配位置前若干个字符，窗口内出现否定词即认为该表述被否定。
     */
    private static final Set<String> NEGATION_WORDS = Set.of(
            "无", "否认", "未见", "未闻", "没有", "未诉", "不含", "排除", "阴性", "不");

    /**
     * 否定判定的回看窗口长度。太短抓不到「否认有青霉素」这类表述，太长会误踩前一句话。
     */
    private static final int NEGATION_WINDOW = 6;

    /**
     * 占位词：字段「有值」但「没有内容」。
     */
    private static final Set<String> PLACEHOLDER_WORDS = Set.of(
            "无", "未见", "无异常", "未见异常", "未查", "不详", "暂缺", "暂无", "待补充",
            "正常", "阴性", "同上", "略", "略述", "遵医嘱", "见上", "以上", "同前");

    /**
     * 剥离空白与占位符号。
     */
    private static final String STRIP_PATTERN = "[\\s　*＊#\\-—_、,，.。;；:：()（）\\[\\]【】/\\\\|⚠✱※★☆○●]";

    /**
     * 带否定语义保护的包含判断。
     * <p>
     * 「无青霉素过敏史」「否认青霉素过敏」返回 false；
     * 「青霉素过敏」返回 true。
     */
    public static boolean containsAffirmed(String text, String keyword) {
        if (!TextUtil.hasText(text) || !TextUtil.hasText(keyword)) {
            return false;
        }
        String needle = keyword.trim();
        int fromIndex = 0;
        while (true) {
            int hit = text.indexOf(needle, fromIndex);
            if (hit < 0) {
                return false;
            }
            if (!isNegated(text, hit)) {
                return true;
            }
            fromIndex = hit + needle.length();
        }
    }

    /**
     * 判断匹配位置是否处于否定语境
     */
    public static boolean isNegated(String text, int matchIndex) {
        int windowStart = Math.max(0, matchIndex - NEGATION_WINDOW);
        String window = text.substring(windowStart, matchIndex);
        for (String negation : NEGATION_WORDS) {
            if (window.contains(negation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 字段是否为「空描述」——没填，或只用占位词敷衍。
     */
    public static boolean isPlaceholderOnly(String text) {
        return isBlank(text) || isPlaceholderWord(text);
    }

    /**
     * 字段是否根本没填（空白 / 全占位符号）。
     */
    public static boolean isBlank(String text) {
        if (!TextUtil.hasText(text)) {
            return true;
        }
        return strip(text).isEmpty();
    }

    /**
     * 字段是否只用占位词敷衍（「无」「未见异常」「遵医嘱」…），不含「没填」。
     */
    public static boolean isPlaceholderWord(String text) {
        if (!TextUtil.hasText(text)) {
            return false;
        }
        return PLACEHOLDER_WORDS.contains(strip(text));
    }

    /**
     * 字段是否只是把字段名重复了一遍（或重复多次）。
     */
    public static boolean isPlaceholderOnly(String text, String fieldLabel) {
        return isPlaceholderOnly(text) || isLabelRepeatOnly(text, fieldLabel);
    }

    /**
     * 字段是否只是把字段名重复了一遍（或重复多次），即模板套用后没有替换内容。
     */
    public static boolean isLabelRepeatOnly(String text, String fieldLabel) {
        if (!TextUtil.hasText(text) || !TextUtil.hasText(fieldLabel)) {
            return false;
        }
        String label = strip(fieldLabel);
        if (label.isEmpty()) {
            return false;
        }
        return strip(text).replace(label, "").isEmpty();
    }

    /**
     * 去掉空白与常见占位符号后的长度，用于判断字段是否被真正填写
     */
    public static int effectiveLength(String text) {
        if (!TextUtil.hasText(text)) {
            return 0;
        }
        return strip(text).length();
    }

    private static String strip(String text) {
        return text.replaceAll(STRIP_PATTERN, "");
    }
}
