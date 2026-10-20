package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static PrescriptionStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PrescriptionStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        PrescriptionStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        PrescriptionStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        PrescriptionStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
