package com.his.operation.support;

import com.his.operation.enums.OperationSafetyCheckItemEnum;
import com.his.operation.enums.OperationSafetyPhaseEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 手术安全核查单（三方 × 三时段）的核查项口径（sql/134）。
 *
 * <p>与 {@link OperationCheckItems}（申请单上的术前核对，单人一次勾选）不是一回事：
 * 手术安全核查制度的事实是<b>手术医师 / 麻醉医师 / 手术室护士三方</b>在
 * <b>麻醉诱导前（Sign In）→ 手术开始前（Time Out）→ 患者离开手术室前（Sign Out）</b>
 * 三个时点当面共同核对并各自签名。飞检查的是"哪一方、哪个时段没签"。
 *
 * <p>核查结果同样是<b>码值集合</b>（逗号分隔），必核项缺失直接拒收 ——
 * 与"术前核对 4 项必核"同一条原则：逐项确认才是核对，一段自由文本只是"看起来核过了"。
 * 码值→文案见 {@link OperationSafetyPhaseEnum} 与 {@link OperationSafetyCheckItemEnum}。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SafetyCheckItems {

    /**
     * 时段：1-麻醉诱导前(Sign In) 2-手术开始前(Time Out) 3-患者离开手术室前(Sign Out)
     */
    public static final List<Integer> ALL_PHASES = phaseCodes();

    public static String phaseText(Integer phase) {
        if (phase == null) {
            return "—";
        }
        return OperationSafetyPhaseEnum.getText(phase);
    }

    public static boolean isValidPhase(Integer phase) {
        return OperationSafetyPhaseEnum.isValid(phase);
    }

    /**
     * 某时段的全部核查项（码 → 文案），供前端渲染三张核查卡
     */
    public static Map<Integer, String> itemsOf(int phase) {
        Map<Integer, String> items = OperationSafetyCheckItemEnum.itemsOf(phase);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("核查时段「" + phase + "」不存在");
        }
        return items;
    }

    /**
     * 某时段的必核项：缺任何一项拒收
     */
    public static List<Integer> requiredOf(int phase) {
        List<Integer> required = OperationSafetyCheckItemEnum.requiredOf(phase);
        if (required.isEmpty()) {
            throw new IllegalArgumentException("核查时段「" + phase + "」不存在");
        }
        return required;
    }

    public static boolean isValid(int phase, Integer code) {
        return OperationSafetyCheckItemEnum.isValid(phase, code);
    }

    /**
     * 解析逗号分隔的码值串。
     *
     * <p>遇到不属于该时段的码值<b>直接抛</b>，不静默丢弃：把 Time Out 的项勾进 Sign In，
     * 要么是没核对，要么是按错了单 —— 两种都不能变成"已核查"。
     */
    public static Set<Integer> parse(int phase, String items) {
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
                throw new IllegalArgumentException("核查项「" + s + "」不是合法码值");
            }
            if (!isValid(phase, code)) {
                throw new IllegalArgumentException("核查项「" + code + "」不属于「" + phaseText(phase) + "」时段");
            }
            set.add(code);
        }
        return set;
    }

    /**
     * 序列化为逗号分隔串（按码值升序，保证同一组勾选写出的字符串稳定可比）
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
     * 未核的必核项文案（拒绝时的可执行提示）
     */
    public static List<String> missingRequired(int phase, Set<Integer> codes) {
        List<String> missing = new ArrayList<>();
        for (Integer req : requiredOf(phase)) {
            if (codes == null || !codes.contains(req)) {
                missing.add(OperationSafetyCheckItemEnum.getText(phase, req));
            }
        }
        return missing;
    }

    /**
     * 核查结果的完整文案（详情展示用；未知码值返回空串，不伪装成某一项）
     */
    public static String summaryText(int phase, String items) {
        Map<Integer, String> labels = OperationSafetyCheckItemEnum.itemsOf(phase);
        List<String> texts = new ArrayList<>();
        for (String part : (items == null ? "" : items).split(",")) {
            String s = part.trim();
            if (s.isEmpty()) {
                continue;
            }
            try {
                int code = Integer.parseInt(s);
                texts.add(labels.getOrDefault(code, ""));
            } catch (NumberFormatException e) {
                texts.add(s);
            }
        }
        return String.join("；", texts);
    }

    private static List<Integer> phaseCodes() {
        List<Integer> list = new ArrayList<>();
        for (OperationSafetyPhaseEnum e : OperationSafetyPhaseEnum.values()) {
            list.add(e.getCode());
        }
        return list;
    }
}
