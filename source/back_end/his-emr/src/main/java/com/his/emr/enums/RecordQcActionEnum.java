package com.his.emr.enums;

import lombok.Getter;

/**
 * 病历质控流转动作枚举
 */
@Getter
public enum RecordQcActionEnum {

    START(1, "发起送审"),
    APPROVE(2, "审核通过"),
    RETURN(3, "退回整改"),
    RESUBMIT(4, "整改提交"),
    FINAL(5, "终审通过");

    private final int code;
    private final String label;

    RecordQcActionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordQcActionEnum fromCode(int code) {
        for (RecordQcActionEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        RecordQcActionEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
