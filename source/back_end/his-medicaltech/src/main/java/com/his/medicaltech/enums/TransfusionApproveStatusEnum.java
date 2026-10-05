package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 用血分级审批状态枚举（sql/93；《医疗机构临床用血管理办法》）。
 *
 * <p>审批级别（上级医师 / 科主任 / 医务科）由申请量折算，见 {@code TransfusionApproveLevelEnum}。
 */
@Getter
public enum TransfusionApproveStatusEnum {

    PENDING(0, "待审批"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回"),
    MAKEUP_PENDING(3, "急诊待补审");

    private final int code;
    private final String label;

    TransfusionApproveStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TransfusionApproveStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransfusionApproveStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用码值 → 文案。null 返回「—」；脏值返回空串，不回落成合法状态。 */
    public static String getText(Integer code) {
        if (code == null) {
            return "—";
        }
        TransfusionApproveStatusEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」）。 */
    public static String labelOrUnknown(Integer code) {
        TransfusionApproveStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
