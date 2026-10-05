package com.his.patient.enums;

import lombok.Getter;

/**
 * 床位调配类型枚举（1-本科室预留 2-跨科调配 3-急诊占床）
 */
@Getter
public enum BedAllocTypeEnum {

    DEPT_RESERVE(1, "本科室预留"),
    CROSS_DEPT(2, "跨科调配"),
    EMERGENCY(3, "急诊占床");

    private final int code;
    private final String label;

    BedAllocTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedAllocTypeEnum fromCode(int code) {
        for (BedAllocTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        BedAllocTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        BedAllocTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
