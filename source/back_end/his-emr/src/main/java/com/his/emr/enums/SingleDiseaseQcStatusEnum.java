package com.his.emr.enums;

import lombok.Getter;

/**
 * 单病种质控状态枚举
 */
@Getter
public enum SingleDiseaseQcStatusEnum {

    ABNORMAL(2, "异常"),
    PASS(1, "通过"),
    PENDING(0, "待质控");

    private final int code;
    private final String label;

    SingleDiseaseQcStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SingleDiseaseQcStatusEnum fromCode(int code) {
        for (SingleDiseaseQcStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。
     */
    public static String getText(Integer code) {
        SingleDiseaseQcStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        SingleDiseaseQcStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
