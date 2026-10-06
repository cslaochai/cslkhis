package com.his.charge.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 医疗文本关键词匹配（带否定语义保护）。
 *
 * <p><b>为什么不直接用 String.contains：</b>病历里「否认手术史」「无手术」「未见吻合口」这类否定表述
 * 会让裸 contains 把「没有依据」误判成「有依据」，方向正好相反 —— 审核结论会偏松，而这正是
 * 医保飞检要抓的。所以匹配前先看关键词前面若干字符内有没有否定词。</p>
 *
 * <p><b>为什么这里自带一份而不是复用 his-ai 的 ClinicalTextMatcher：</b>
 * 项目依赖方向硬约束「业务模块不得依赖 his-ai」，his-charge 引用不到它。
 * 两份实现语义必须保持一致；若要根治，应把匹配器上移到 his-common 供两侧共用。</p>
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
