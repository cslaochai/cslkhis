package com.his.common.enums;

import lombok.Getter;

/**
 * 护理质量检查类别枚举（sql/168，落在护理质控检查项目录的检查类别列）
 *
 * <p>一次现场检查按类别开单（一张单 = 病区 × 月份 × 类别），因为护理部的排班是
 * 「单周基础护理、双周专科、每月安全和文书」轮着查，混在一张单里就没法算各自的目标值。
 *
 * <p>只有 {@link #BASIC_NURSING} 与 {@link #DOC} 挂台账指标
 * （{@link NursingIndicatorEnum#BASIC_NURSING} / {@link NursingIndicatorEnum#NURSING_DOC}），
 * 其余三类是检查表内容 + 得分率，不单独出月度指标。
 * <br>字典权威在本枚举，码值改动必须同步 {@code sql/168} 的 {@code his_nursing_qc_category} 段。
 */
@Getter
public enum NursingQcCategoryEnum {

    /** 基础护理（8 项 ×12.5 分） */
    BASIC_NURSING(1, "基础护理"),
    /** 专科护理（5 项 ×20 分） */
    SPECIALTY(2, "专科护理"),
    /** 安全管理（5 项 ×20 分，含跌倒/压疮防范措施，与不良事件互为因果） */
    SAFETY(3, "安全管理"),
    /** 护理文书（4 项 ×25 分） */
    DOC(4, "护理文书"),
    /** 院感防控（4 项 ×25 分） */
    INFECTION(5, "院感防控");

    private final int code;
    private final String label;

    NursingQcCategoryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NursingQcCategoryEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NursingQcCategoryEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        NursingQcCategoryEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** 白名单文案：入参非法时直接回给页面「该选哪个」，比只说「参数错误」少一轮来回 */
    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (NursingQcCategoryEnum e : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(e.code).append("-").append(e.label);
        }
        return sb.toString();
    }
}
