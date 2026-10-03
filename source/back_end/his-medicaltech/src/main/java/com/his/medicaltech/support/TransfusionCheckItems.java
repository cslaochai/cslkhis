package com.his.medicaltech.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 输血前双人核对要点（输血安全核查单的可核对部分）。
 *
 * <p>为什么必须是"码值集合"而不是一段自由备注：
 * 输血是<b>唯一</b>要求双人核对的护理操作，《临床输血技术规范》规定
 * 输血前由两名医护人员核对<b>受血者身份、血型、血袋号、血液外观、有效期、配血结果</b>。
 * 写成一句话，就变成"看起来核过了" —— 与术前核对、模型输出"没事"是同一种不可审计的证据。
 *
 * <p>所以：核对结果是码值集合（如 {@code 1,2,3,4,5,6}），1~6 必核项缺任何一项直接拒绝提交，
 * 前端按 {@link #all()} 渲染勾选框。第 7、8 项为附加项（同意书、输注前生命体征），
 * 各地要求不一，不设为必核但可勾选。
 */
public final class TransfusionCheckItems {

    private TransfusionCheckItems() {
    }

    /** 必核项：这 6 项缺任何一项都不允许开始输注 */
    public static final List<Integer> REQUIRED = List.of(1, 2, 3, 4, 5, 6);

    private static final Map<Integer, String> LABELS = new LinkedHashMap<>();

    static {
        LABELS.put(1, "受血者姓名、住院号与腕带信息一致");
        LABELS.put(2, "受血者与血袋血型（ABO + Rh）相符");
        LABELS.put(3, "血袋号、血液品种与规格与发血单一致");
        LABELS.put(4, "交叉配血结果相合（主侧/次侧）");
        LABELS.put(5, "血液外观无异常（无溶血、无凝块、无气泡、无变色）");
        LABELS.put(6, "血袋有效期与包装完好");
        LABELS.put(7, "输血知情同意书已签署");
        LABELS.put(8, "输注前生命体征已测量并记录");
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
     * 结果就是"这次输血的核对记录少了一项"，而且没有任何痕迹。
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
                throw new IllegalArgumentException("输血核对项「" + s + "」不是合法码值");
            }
            if (!isValid(code)) {
                throw new IllegalArgumentException("输血核对项「" + code + "」不是合法码值");
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
