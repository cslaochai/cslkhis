package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 住院病历/护理文书状态枚举（1-草稿 2-已提交 3-已归档，归档后禁改）
 */
@Getter
public enum InpatientRecordStatusEnum {

    DRAFT(1, "草稿"),
    SUBMITTED(2, "已提交"),
    ARCHIVED(3, "已归档");

    private final int code;
    private final String label;

    InpatientRecordStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 病历文书状态：已归档后禁止修改
     */
    public static boolean isArchived(Integer code) {
        return code != null && code == ARCHIVED.code;
    }

    public static InpatientRecordStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InpatientRecordStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InpatientRecordStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InpatientRecordStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        InpatientRecordStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
