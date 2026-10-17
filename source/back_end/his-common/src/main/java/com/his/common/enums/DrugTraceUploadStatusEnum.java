package com.his.common.enums;

import lombok.Getter;

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

    public static DrugTraceUploadStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DrugTraceUploadStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(Integer code) {
        DrugTraceUploadStatusEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        DrugTraceUploadStatusEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
