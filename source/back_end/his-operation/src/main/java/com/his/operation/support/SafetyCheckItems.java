package com.his.operation.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.*;

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
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SafetyCheckItems {

    /**
     * 时段：1-麻醉诱导前(Sign In) 2-手术开始前(Time Out) 3-患者离开手术室前(Sign Out)
     */
    public static final int PHASE_SIGN_IN = 1;
    public static final int PHASE_TIME_OUT = 2;
    public static final int PHASE_SIGN_OUT = 3;
    public static final List<Integer> ALL_PHASES = List.of(PHASE_SIGN_IN, PHASE_TIME_OUT, PHASE_SIGN_OUT);
    private static final Map<Integer, String> PHASE_LABELS = new LinkedHashMap<>();
    /**
     * 每时段的核查项：phase → (码 → 文案)
     */
    private static final Map<Integer, Map<Integer, String>> PHASE_ITEMS = new LinkedHashMap<>();
    /**
     * 每时段的必核项：缺任何一项拒收
     */
    private static final Map<Integer, List<Integer>> PHASE_REQUIRED = new LinkedHashMap<>();

    static {
        PHASE_LABELS.put(PHASE_SIGN_IN, "麻醉诱导前（Sign In）");
        PHASE_LABELS.put(PHASE_TIME_OUT, "手术开始前（Time Out）");
        PHASE_LABELS.put(PHASE_SIGN_OUT, "患者离开手术室前（Sign Out）");

        Map<Integer, String> signIn = new LinkedHashMap<>();
        signIn.put(1, "患者身份已核对（姓名 / 住院号 / 腕带）");
        signIn.put(2, "手术部位与标识已核对（含左右侧标记）");
        signIn.put(3, "手术术式、麻醉方式与知情同意书（手术 + 麻醉）已核对");
        signIn.put(4, "麻醉风险评估、气道评估与麻醉设备药品已准备就绪");
        signIn.put(5, "过敏史与既往手术史已确认");
        signIn.put(6, "静脉通道通畅，术前用药与抗生素皮试已确认");
        signIn.put(7, "备血、植入物与特殊器械可用性已确认");
        PHASE_ITEMS.put(PHASE_SIGN_IN, signIn);
        PHASE_REQUIRED.put(PHASE_SIGN_IN, List.of(1, 2, 3, 4, 5));

        Map<Integer, String> timeOut = new LinkedHashMap<>();
        timeOut.put(1, "在场人员均已自我介绍（姓名与角色）");
        timeOut.put(2, "患者身份 / 手术部位 / 术式 / 体位再次确认");
        timeOut.put(3, "影像资料已调阅并与术式核对");
        timeOut.put(4, "预计重要事件已提示（出血量、手术时长、禁忌与应急准备）");
        timeOut.put(5, "麻醉安全已确认（SpO2、气道方案、负压吸引）");
        timeOut.put(6, "预防性抗生素给药时机已确认（切皮前 30~60 分钟）");
        timeOut.put(7, "无菌物品、器械与敷料基数已确认");
        PHASE_ITEMS.put(PHASE_TIME_OUT, timeOut);
        PHASE_REQUIRED.put(PHASE_TIME_OUT, List.of(1, 2, 4, 5, 6));

        Map<Integer, String> signOut = new LinkedHashMap<>();
        signOut.put(1, "器械 / 敷料 / 缝针清点已完成且数目一致");
        signOut.put(2, "手术标本已标记并口头复述确认");
        signOut.put(3, "设备管线与患者潜在风险已确认（引流、管路、皮肤）");
        signOut.put(4, "术后疼痛管理与麻醉苏醒情况已评估");
        signOut.put(5, "患者转运与交接注意事项已沟通（接收护士 / 医师在场确认）");
        PHASE_ITEMS.put(PHASE_SIGN_OUT, signOut);
        PHASE_REQUIRED.put(PHASE_SIGN_OUT, List.of(1, 2, 4, 5));
    }

    public static String phaseText(Integer phase) {
        if (phase == null) {
            return "—";
        }
        return PHASE_LABELS.getOrDefault(phase, "");
    }

    public static boolean isValidPhase(Integer phase) {
        return phase != null && PHASE_ITEMS.containsKey(phase);
    }

    /**
     * 某时段的全部核查项（码 → 文案），供前端渲染三张核查卡
     */
    public static Map<Integer, String> itemsOf(int phase) {
        Map<Integer, String> items = PHASE_ITEMS.get(phase);
        if (items == null) {
            throw new IllegalArgumentException("核查时段「" + phase + "」不存在");
        }
        return items;
    }

    public static List<Integer> requiredOf(int phase) {
        List<Integer> required = PHASE_REQUIRED.get(phase);
        if (required == null) {
            throw new IllegalArgumentException("核查时段「" + phase + "」不存在");
        }
        return required;
    }

    public static boolean isValid(int phase, Integer code) {
        Map<Integer, String> items = PHASE_ITEMS.get(phase);
        return items != null && code != null && items.containsKey(code);
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
        Map<Integer, String> items = itemsOf(phase);
        for (Integer req : requiredOf(phase)) {
            if (codes == null || !codes.contains(req)) {
                missing.add(items.get(req));
            }
        }
        return missing;
    }

    /**
     * 核查结果的完整文案（详情展示用；未知码值返回空串，不伪装成某一项）
     */
    public static String summaryText(int phase, String items) {
        Map<Integer, String> labels = itemsOf(phase);
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
}
