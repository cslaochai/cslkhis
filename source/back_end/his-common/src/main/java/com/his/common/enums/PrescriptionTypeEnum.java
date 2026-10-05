package com.his.common.enums;

import lombok.Getter;

/**
 * 处方类型（处方主表的处方类型列）
 */
@Getter
public enum PrescriptionTypeEnum {

    WESTERN(1, "西药处方"),
    PATENT(2, "中成药处方"),
    TCM(3, "中药饮片处方");

    private final int code;
    private final String label;

    PrescriptionTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PrescriptionTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PrescriptionTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        PrescriptionTypeEnum type = fromCode(code);
        return type == null ? null : type.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        PrescriptionTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
