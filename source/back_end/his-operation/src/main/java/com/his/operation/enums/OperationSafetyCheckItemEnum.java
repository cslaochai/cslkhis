package com.his.operation.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 手术安全核查单核查项（三方 × 三时段）。
 *
 * <p>码值→文案的唯一出口（原 {@code SafetyCheckItems} 的 {@code PHASE_ITEMS} 映射已上移至此）。
 * 与术前核对（{@link OperationPreCheckItemEnum}）不同：安全核查是手术医师 / 麻醉医师 / 护士三方
 * 在三个时点当面共同核对并各自签名，结果同样是码值集合，必核项缺失直接拒收。
 */
@Getter
public enum OperationSafetyCheckItemEnum {

    // 麻醉诱导前（Sign In）
    SIGN_IN_1(1, 1, "患者身份已核对（姓名 / 住院号 / 腕带）", true),
    SIGN_IN_2(1, 2, "手术部位与标识已核对（含左右侧标记）", true),
    SIGN_IN_3(1, 3, "手术术式、麻醉方式与知情同意书（手术 + 麻醉）已核对", true),
    SIGN_IN_4(1, 4, "麻醉风险评估、气道评估与麻醉设备药品已准备就绪", true),
    SIGN_IN_5(1, 5, "过敏史与既往手术史已确认", true),
    SIGN_IN_6(1, 6, "静脉通道通畅，术前用药与抗生素皮试已确认", false),
    SIGN_IN_7(1, 7, "备血、植入物与特殊器械可用性已确认", false),

    // 手术开始前（Time Out）
    TIME_OUT_1(2, 1, "在场人员均已自我介绍（姓名与角色）", true),
    TIME_OUT_2(2, 2, "患者身份 / 手术部位 / 术式 / 体位再次确认", true),
    TIME_OUT_3(2, 3, "影像资料已调阅并与术式核对", false),
    TIME_OUT_4(2, 4, "预计重要事件已提示（出血量、手术时长、禁忌与应急准备）", true),
    TIME_OUT_5(2, 5, "麻醉安全已确认（SpO2、气道方案、负压吸引）", true),
    TIME_OUT_6(2, 6, "预防性抗生素给药时机已确认（切皮前 30~60 分钟）", true),
    TIME_OUT_7(2, 7, "无菌物品、器械与敷料基数已确认", false),

    // 患者离开手术室前（Sign Out）
    SIGN_OUT_1(3, 1, "器械 / 敷料 / 缝针清点已完成且数目一致", true),
    SIGN_OUT_2(3, 2, "手术标本已标记并口头复述确认", true),
    SIGN_OUT_3(3, 3, "设备管线与患者潜在风险已确认（引流、管路、皮肤）", false),
    SIGN_OUT_4(3, 4, "术后疼痛管理与麻醉苏醒情况已评估", true),
    SIGN_OUT_5(3, 5, "患者转运与交接注意事项已沟通（接收护士 / 医师在场确认）", true);

    private final int phase;
    private final int code;
    private final String label;
    private final boolean required;

    OperationSafetyCheckItemEnum(int phase, int code, String label, boolean required) {
        this.phase = phase;
        this.code = code;
        this.label = label;
        this.required = required;
    }

    public static OperationSafetyCheckItemEnum fromCode(int phase, Integer code) {
        if (code == null) {
            return null;
        }
        for (OperationSafetyCheckItemEnum e : values()) {
            if (e.phase == phase && e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValid(int phase, Integer code) {
        return fromCode(phase, code) != null;
    }

    public static String getText(int phase, Integer code) {
        OperationSafetyCheckItemEnum e = fromCode(phase, code);
        return e != null ? e.label : "";
    }

    public static String labelOrUnknown(int phase, Integer code) {
        OperationSafetyCheckItemEnum e = fromCode(phase, code);
        return e != null ? e.label : "未知(" + code + ")";
    }

    /** 某时段的必核项码值（缺任意一项拒收） */
    public static List<Integer> requiredOf(int phase) {
        List<Integer> list = new ArrayList<>();
        for (OperationSafetyCheckItemEnum e : values()) {
            if (e.phase == phase && e.required) {
                list.add(e.code);
            }
        }
        return list;
    }

    /** 某时段的全部核查项（码→文案），供前端渲染勾选框 */
    public static Map<Integer, String> itemsOf(int phase) {
        Map<Integer, String> m = new LinkedHashMap<>();
        for (OperationSafetyCheckItemEnum e : values()) {
            if (e.phase == phase) {
                m.put(e.code, e.label);
            }
        }
        return m;
    }
}
