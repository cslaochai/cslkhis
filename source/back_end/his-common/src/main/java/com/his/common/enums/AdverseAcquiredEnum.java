package com.his.common.enums;

import lombok.Getter;

/**
 * 不良事件来源枚举（sql/168，落在不良事件上报的获得方式列）
 *
 * <p>压疮必须分「院内获得」和「入院带入」：带入的压疮是患者入院时已存在的皮肤问题，
 * 计入本院发生率等于把社区/转诊医院的问题算到自己头上，评审专家第一个就问这个。
 * 所以 {@link NursingIndicatorEnum#UPPR_RATE} 的分子只认 {@link #HOSPITAL_ACQUIRED}，
 * {@link #ADMITTED_WITH} 的行照样留档上报（护理部要知道收了多少这样的病人），只是不进分子。
 *
 * <p>跌倒/坠床不区分：入院 24 小时内的跌倒照样是院内防范不到位，一律按院内事件计。
 * <br>字典权威在本枚举，码值改动必须同步 {@code sql/168} 的 {@code his_adverse_acquired} 段。
 */
@Getter
public enum AdverseAcquiredEnum {

    /**
     * 院内获得（进发生率分子）
     */
    HOSPITAL_ACQUIRED(1, "院内获得"),
    /**
     * 入院带入（留档不进分子）
     */
    ADMITTED_WITH(2, "入院带入");

    private final int code;
    private final String label;

    AdverseAcquiredEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AdverseAcquiredEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AdverseAcquiredEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        AdverseAcquiredEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        AdverseAcquiredEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
