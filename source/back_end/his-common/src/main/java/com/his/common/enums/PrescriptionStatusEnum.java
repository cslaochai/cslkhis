package com.his.common.enums;

import lombok.Getter;

/**
 * 门诊处方状态（处方主表的处方状态列）
 */
@Getter
public enum PrescriptionStatusEnum {

    DRAFT(1, "草稿"),
    SUBMITTED(2, "已提交待审方"),
    AUDITED(3, "已审方"),
    DISPENSED(4, "已发药"),
    CANCELLED(5, "已作废"),
    RETURNED(6, "已退药"),
    /**
     * L7 审方退回重开闭环：药师审方不通过退回医生（区别于 6-已退药 = 发药后退货）
     */
    RETURNED_AUDIT(7, "审方退回");

    private final int code;
    private final String label;

    PrescriptionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PrescriptionStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PrescriptionStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由前端渲染「未知(n)」，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        PrescriptionStatusEnum status = fromCode(code);
        return status == null ? null : status.getLabel();
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        PrescriptionStatusEnum status = fromCode(code);
        return status == null ? (code == null ? "未知" : "未知(" + code + ")") : status.getLabel();
    }
}
