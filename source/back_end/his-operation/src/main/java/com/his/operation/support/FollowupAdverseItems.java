package com.his.operation.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import com.his.operation.support.SafetyCheckItems;

/**
 * 麻醉随访并发症要点字典（P134.3）+ 恢复情况/状态文案单点。
 *
 * <p>与 {@link SafetyCheckItems} 同口径：<b>不建字典</b>，后端 AnesthesiaLabels/
 * 本文件与前端 {@code lib/anesthesia.js} 各一份单点。未知码值一律「未知(n)」，
 * 绝不回落成某个看起来合法的值。
 */
public final class FollowupAdverseItems {

    private FollowupAdverseItems() {
    }

    /** 码值 → 文案（顺序即渲染顺序） */
    private static final Map<Integer, String> ALL = new LinkedHashMap<>();

    static {
        ALL.put(1, "恶心呕吐");
        ALL.put(2, "咽痛");
        ALL.put(3, "尿潴留");
        ALL.put(4, "头痛");
        ALL.put(5, "头晕");
        ALL.put(6, "神经症状");
        ALL.put(7, "呼吸并发症");
        ALL.put(8, "低血压/心律失常");
        ALL.put(9, "其他");
    }

    /** 恢复情况：1-良好 2-一般 3-差 */
    public static String recoveryText(Integer recovery) {
        if (recovery == null) {
            return "—";
        }
        return switch (recovery) {
            case 1 -> "良好";
            case 2 -> "一般";
            case 3 -> "差";
            default -> "未知(" + recovery + ")";
        };
    }

    /** 状态：0-草稿 1-已完成 */
    public static String statusText(Integer status) {
        if (status == null) {
            return "—";
        }
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "已完成";
            default -> "未知(" + status + ")";
        };
    }

    public static Map<Integer, String> all() {
        return ALL;
    }

    public static boolean isValid(int code) {
        return ALL.containsKey(code);
    }

    /** "1,3,9" → {1,3,9}；非法/未知码直接抛 IllegalArgumentException（调用方转 BusinessException） */
    public static Set<Integer> parse(String raw) {
        Set<Integer> set = new TreeSet<>();
        if (raw == null || raw.isBlank()) {
            return set;
        }
        for (String part : raw.split(",")) {
            if (part.isBlank()) {
                continue;
            }
            int code;
            try {
                code = Integer.parseInt(part.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("并发症码值不是数字：" + part);
            }
            if (!isValid(code)) {
                throw new IllegalArgumentException("并发症码值不存在：" + code);
            }
            set.add(code);
        }
        return set;
    }

    public static String serialize(Set<Integer> codes) {
        return codes == null || codes.isEmpty() ? null
                : codes.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /** 列表展示文案："恶心呕吐、尿潴留"；空 → null（渲染侧按"无并发症"处理） */
    public static String summaryText(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return parse(raw).stream()
                .map(code -> ALL.getOrDefault(code, "未知(" + code + ")"))
                .collect(Collectors.joining("、"));
    }

    /** 随访轮次建议文案 */
    public static String roundText(Integer roundNo) {
        if (roundNo == null) {
            return "—";
        }
        return switch (roundNo) {
            case 1 -> "第1轮·术后即刻";
            case 2 -> "第2轮·术后24h";
            case 3 -> "第3轮·术后48h";
            default -> "第" + roundNo + "轮·追加随访";
        };
    }

    public static List<Integer> codes() {
        return List.copyOf(ALL.keySet());
    }
}
