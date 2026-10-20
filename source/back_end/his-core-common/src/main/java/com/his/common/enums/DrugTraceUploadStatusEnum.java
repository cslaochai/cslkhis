package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 药品追溯码上传状态枚举（0-待上传 1-已上传 2-上传失败）。
 */
@Getter
public enum DrugTraceUploadStatusEnum {

    PENDING(0, "待上传"),
    DONE(1, "已上传"),
    FAILED(2, "上传失败");

    private final int code;
    private final String label;

    DrugTraceUploadStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static DrugTraceUploadStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DrugTraceUploadStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DrugTraceUploadStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DrugTraceUploadStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DrugTraceUploadStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
