package com.his.operation.support;

import com.his.common.enums.AnesthesiaFollowupAdverseEnum;
import com.his.common.enums.AnesthesiaFollowupRecoveryEnum;
import com.his.common.enums.AnesthesiaFollowupRoundEnum;
import com.his.common.util.TextUtil;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 麻醉随访并发症要点字典（P134.3）+ 恢复情况/状态/轮次文案单点。
 */
@NoArgsConstructor
public final class FollowupAdverseItems {

    /**
     * 恢复情况：1-良好 2-一般 3-差
     */
    public static String recoveryText(Integer recovery) {
        if (recovery == null) {
            return "—";
        }
        AnesthesiaFollowupRecoveryEnum e = AnesthesiaFollowupRecoveryEnum.fromCode(recovery);
        return e != null ? e.getLabel() : "";
    }

    public static Map<Integer, String> all() {
        return AnesthesiaFollowupAdverseEnum.all();
    }

    public static boolean isValid(int code) {
        return AnesthesiaFollowupAdverseEnum.isValid(code);
    }

    /**
     * "1,3,9" → {1,3,9}；非法/未知码直接抛 IllegalArgumentException（调用方转 BusinessException）
     */
    public static Set<Integer> parse(String raw) {
        Set<Integer> set = new TreeSet<>();
        if (!TextUtil.hasText(raw)) {
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

    /**
     * 列表展示文案："恶心呕吐、尿潴留"；空 → null（渲染侧按"无并发症"处理）
     */
    public static String summaryText(String raw) {
        if (!TextUtil.hasText(raw)) {
            return null;
        }
        return parse(raw).stream()
                .map(code -> AnesthesiaFollowupAdverseEnum.getLabel(code))
                .collect(Collectors.joining("、"));
    }

    /**
     * 随访轮次建议文案
     */
    public static String roundText(Integer roundNo) {
        return AnesthesiaFollowupRoundEnum.getLabel(roundNo);
    }

    public static List<Integer> codes() {
        List<Integer> list = new ArrayList<>();
        for (AnesthesiaFollowupAdverseEnum e : AnesthesiaFollowupAdverseEnum.values()) {
            list.add(e.getCode());
        }
        return list;
    }
}
