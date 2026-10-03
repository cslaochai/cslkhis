package com.his.patient.enums;

import lombok.Getter;

/**
 * 医嘱类别枚举
 */
@Getter
public enum OrderClassEnum {

    DRUG(1, "药品"),
    EXAMINATION(2, "检查"),
    LABORATORY(3, "检验"),
    TREATMENT(4, "治疗"),
    NURSING(5, "护理"),
    OPERATION(6, "手术"),
    TRANSFUSION(7, "输血"),
    MONITORING(8, "监护"),
    OTHER(9, "其他"),
    NUTRITION(10, "临床营养");

    private final int code;
    private final String label;

    OrderClassEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OrderClassEnum fromCode(int code) {
        for (OrderClassEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        OrderClassEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
