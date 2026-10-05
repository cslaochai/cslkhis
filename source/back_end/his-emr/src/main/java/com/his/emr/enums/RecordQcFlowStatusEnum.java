package com.his.emr.enums;

import lombok.Getter;

/**
 * 病历三级质控流转状态枚举
 */
@Getter
public enum RecordQcFlowStatusEnum {

    DEPT_PENDING(1, "科级待审"),
    ARCHIVE_PENDING(2, "病案室待审"),
    MEDAFFAIRS_PENDING(3, "医务处待审"),
    FINAL_APPROVED(4, "终审通过"),
    REWORKING(5, "整改中");

    private final int code;
    private final String label;

    RecordQcFlowStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RecordQcFlowStatusEnum fromCode(int code) {
        for (RecordQcFlowStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        RecordQcFlowStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
