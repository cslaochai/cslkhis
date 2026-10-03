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

    public static String labelOf(Integer code) {
        PrescriptionTypeEnum type = fromCode(code);
        return type == null ? null : type.getLabel();
    }
}
