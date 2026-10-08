package com.his.operation.support;

import com.his.common.util.TextUtil;
import com.his.operation.enums.OperationPreCheckItemEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 术前核对要点（手术安全核查单的可核对部分）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OperationCheckItems {

    /**
     * 必核项：这 4 项缺任何一项都不允许进入"术前核对完成"
     */
    public static final List<Integer> REQUIRED = OperationPreCheckItemEnum.requiredCodes();

    /**
     * 全部核对项（码 → 文案），供前端渲染勾选框
     */
    public static Map<Integer, String> all() {
        return OperationPreCheckItemEnum.all();
    }

    public static String text(Integer code) {
        if (code == null) {
            return "—";
        }
        return OperationPreCheckItemEnum.getText(code);
    }

    public static boolean isValid(Integer code) {
        return OperationPreCheckItemEnum.isValid(code);
    }

    /**
     * 解析逗号分隔的码值串。
     *
     * <p>遇到不认识的码值**直接抛**，不静默丢弃：一个拼错的核对项被忽略掉，
     * 结果就是"这条手术的核对记录少了一项"，而且没有任何痕迹。
     */
    public static Set<Integer> parse(String items) {
        Set<Integer> set = new LinkedHashSet<>();
        if (!TextUtil.hasText(items)) {
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

    /**
     * 序列化为逗号分隔串（按码值升序，保证同一组勾选写出来的字符串稳定可比）
     */
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

    /**
     * 未核的必核项文案（用于拒绝时的可执行提示）
     */
    public static List<String> missingRequired(Set<Integer> codes) {
        List<String> missing = new ArrayList<>();
        for (Integer req : REQUIRED) {
            if (codes == null || !codes.contains(req)) {
                missing.add(text(req));
            }
        }
        return missing;
    }

    /**
     * 核对结果的完整文案（列表展示用）
     */
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
