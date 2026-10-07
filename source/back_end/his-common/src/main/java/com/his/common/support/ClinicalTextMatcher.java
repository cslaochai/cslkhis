package com.his.common.support;

import com.his.common.util.TextUtil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 临床文本匹配工具 —— 处理「否定语义」与「空描述」这两件小事。
 * <p>
 * 看起来是个小工具，实际是医疗 NLP 最容易翻车的地方：
 * 病历写「无青霉素过敏史」、写「否认溃疡病史」，规则如果只做
 * contains("青霉素")，就会把「明确否认」读成「明确阳性」，
 * 然后给医生弹一个严重级别 3 的假警报。假警报多了，医生就会关掉提醒，
 * 整套系统随之失效。
 * <p>
 * 所以所有基于字面的规则匹配都必须走这里，禁止直接 {@code String.contains}。
 *
 * <p><b>为什么放在 his-common（2026-09-19 从 his-ai 下移）</b>：
 * 业务模块不允许依赖 his-ai（循环依赖），于是任何业务模块想用否定语义保护，
 * 都只能自己复制一份。his-charge 已经这么干了一次
 * （{@code com.his.charge.support.EvidenceKeywordMatcher}）。
 * 同一个医学语义存在多份实现，是最典型的「改一处、另一处静默失效」温床，
 * 而它保护的恰恰是「不要误报」这件事 —— 所以下移到公共模块，全库共用一份。
 *
 * <p><b>尚未收口的重复实现</b>：his-charge 的 {@code EvidenceKeywordMatcher}
 * 仍在独立维护，且与本类**语义并不相同**（否定窗口 4 vs 本类 6，否定词表也略有出入）。
 * 合并会改变医保合规审核的命中结论，属于行为变更，需要重跑合规验证后再做 ——
 * 不要顺手替换。
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
     * <p>
     * 演示库里的病历字段带「⚠」「✱」这类模板标记（如「⚠ 过敏史 *」），
     * 不剥掉就没法识别出「这只是把字段名抄了一遍」。
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
     * <p>
     * <b>与 {@link #isPlaceholderOnly} 的区别，是医疗语义上真实存在的区别</b>：
     * isPlaceholderOnly("无") 为 true，但「既往史：无」「过敏史：无」
     * 在《病历书写基本规范》下是**合法的显式记录**，不是缺陷。
     * 只有「无」出现在主诉、现病史、诊断这类必须有实质内容的字段上才是缺陷。
     * <p>
     * 所以「史」类字段（既往史/过敏史）要用本方法，而不是 isPlaceholderOnly ——
     * 否则会把正常写「无过敏史」的病历整片误判为不合格。
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
     * <p>
     * 演示库里的病历大量存在「主诉 *」「主诉」「现病史 *」这类填充 ——
     * 这不是笔误，是模板套用后没有替换内容。带字段名的重载就是为了精确抓住它：
     * 精确、可解释，评审时能指着病历原文说「这里确实是空的」。
     */
    public static boolean isPlaceholderOnly(String text, String fieldLabel) {
        return isPlaceholderOnly(text) || isLabelRepeatOnly(text, fieldLabel);
    }

    /**
     * 字段是否只是把字段名重复了一遍（或重复多次），即模板套用后没有替换内容。
     * <p>
     * 不含「没填」与「占位词」判断，便于「史」类字段单独使用
     * （它们允许写「无」，但不允许留着「过敏史 *」这种模板残渣）。
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
