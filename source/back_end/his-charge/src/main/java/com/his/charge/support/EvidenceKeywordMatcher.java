package com.his.charge.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 医疗文本关键词匹配（带否定语义保护）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EvidenceKeywordMatcher {

    /**
     * 否定词：出现在关键词前 windowSize 个字符内则视为否定
     */
    private static final List<String> NEGATIONS = Arrays.asList(
            "否认", "无", "未", "不", "没有", "未见", "未行", "未做", "排除", "除外", "阴性");

    /**
     * 否定回看窗口（字符数）
     */
    private static final int WINDOW = 4;

    /**
     * 在文本中匹配任一关键词，且该次出现不是被否定的。
     *
     * @param text     待匹配文本，可为空
     * @param keywords 关键词（任一命中即返回 true）
     * @return true 表示存在肯定语境下的关键词
     */
    public static boolean hits(String text, String... keywords) {
        if (text == null || text.isEmpty() || keywords == null) {
            return false;
        }
        for (String kw : keywords) {
            if (kw == null || kw.isEmpty()) {
                continue;
            }
            int from = 0;
            while (true) {
                int idx = text.indexOf(kw, from);
                if (idx < 0) {
                    break;
                }
                if (!isNegated(text, idx)) {
                    return true;
                }
                from = idx + kw.length();
            }
        }
        return false;
    }

    /**
     * 命中并返回命中的那个关键词，未命中返回 null（用于把命中词写进依据说明）
     */
    public static String firstHit(String text, String... keywords) {
        if (text == null || text.isEmpty() || keywords == null) {
            return null;
        }
        for (String kw : keywords) {
            if (kw == null || kw.isEmpty()) {
                continue;
            }
            int from = 0;
            while (true) {
                int idx = text.indexOf(kw, from);
                if (idx < 0) {
                    break;
                }
                if (!isNegated(text, idx)) {
                    return kw;
                }
                from = idx + kw.length();
            }
        }
        return null;
    }

    /**
     * 关键词出现位置之前 WINDOW 个字符内是否含否定词
     */
    private static boolean isNegated(String text, int kwIndex) {
        int start = Math.max(0, kwIndex - WINDOW);
        String prefix = text.substring(start, kwIndex);
        for (String neg : NEGATIONS) {
            if (prefix.contains(neg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 拼接多个可空文本，供整体匹配用
     */
    public static String join(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isEmpty()) {
                sb.append(p).append(' ');
            }
        }
        return sb.toString();
    }
}
