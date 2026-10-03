package com.his.operation.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 术前核对要点（手术安全核查单的可核对部分）。
 *
 * <p>为什么要有"必核项"而不是一个自由文本框：
 * 术前核对的价值在于<b>逐项确认</b>——"患者身份/手术部位/术式/知情同意/过敏史"这四项
 * 少核任何一项，都会在飞检时变成"该核未核"。写成一段自由文本，
 * 就变成"看起来核过了"，与"模型说没事"是同一种不可审计的证据。
 *
 * <p>因此：核对结果是<b>码值集合</b>（如 {@code 1,2,3,4}），必核项缺失直接拒绝提交，
 * 前端按 {@link #all()} 渲染勾选框。
 */
public final class OperationCheckItems {

    private OperationCheckItems() {
    }

    /** 必核项：这 4 项缺任何一项都不允许进入"术前核对完成" */
    public static final List<Integer> REQUIRED = List.of(1, 2, 3, 4);

    private static final Map<Integer, String> LABELS = new LinkedHashMap<>();

    static {
        LABELS.put(1, "患者身份与手术部位标识已核对");
        LABELS.put(2, "手术术式与知情同意书已核对");
        LABELS.put(3, "麻醉方式与麻醉同意书已核对");
        LABELS.put(4, "过敏史与术前用药已核对");
        LABELS.put(5, "备血、器械与植入物已到位");
        LABELS.put(6, "影像资料与化验结果已确认");
    }

    /** 全部核对项（码 → 文案），供前端渲染勾选框 */
    public static Map<Integer, String> all() {
        return LABELS;
    }

    public static String text(Integer code) {
        if (code == null) {
            return "—";
        }
        return LABELS.getOrDefault(code, "未知(" + code + ")");
    }

    public static boolean isValid(Integer code) {
        return code != null && LABELS.containsKey(code);
    }

    /**
     * 解析逗号分隔的码值串。
     *
     * <p>遇到不认识的码值**直接抛**，不静默丢弃：一个拼错的核对项被忽略掉，
     * 结果就是"这条手术的核对记录少了一项"，而且没有任何痕迹。
     */
    public static Set<Integer> parse(String items) {
        Set<Integer> set = new LinkedHashSet<>();
        if (items == null || items.isBlank()) {
            return set;
        }
        for (String part : items.split(",")) {
            String s = part.trim();
            if (s.isEmpty()) {
                continue;
            }
            int code;
            try {
                code = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("术前核对项「" + s + "」不是合法码值");
            }
            if (!isValid(code)) {
                throw new IllegalArgumentException("术前核对项「" + code + "」不是合法码值");
            }
            set.add(code);
        }
        return set;
    }

    /** 序列化为逗号分隔串（按码值升序，保证同一组勾选写出来的字符串稳定可比） */
    public static String serialize(Set<Integer> codes) {
        if (codes == null || codes.isEmpty()) {
            return null;
        }
        List<Integer> sorted = new ArrayList<>(codes);
        sorted.sort(Integer::compareTo);
        StringBuilder sb = new StringBuilder();
        for (Integer c : sorted) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /** 未核的必核项文案（用于拒绝时的可执行提示） */
    public static List<String> missingRequired(Set<Integer> codes) {
        List<String> missing = new ArrayList<>();
        for (Integer req : REQUIRED) {
            if (codes == null || !codes.contains(req)) {
                missing.add(text(req));
            }
        }
        return missing;
    }

    /** 核对结果的完整文案（列表展示用） */
    public static String summaryText(String items) {
        Set<Integer> codes;
        try {
            codes = parse(items);
        } catch (IllegalArgumentException e) {
            return "未知核对结果(" + items + ")";
        }
        if (codes.isEmpty()) {
            return "—";
        }
        List<String> texts = new ArrayList<>();
        for (Integer c : codes) {
            texts.add(text(c));
        }
        return String.join("；", texts);
    }
}
