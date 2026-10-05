package com.his.system.enums;

import lombok.Getter;

/**
 * 操作日志业务类型枚举（{@code sys_oper_log.business_type}，0~7）。
 */
@Getter
public enum OperBusinessTypeEnum {

    OTHER(0, "其他"),
    INSERT(1, "新增"),
    UPDATE(2, "修改"),
    DELETE(3, "删除"),
    GRANT(4, "授权"),
    EXPORT(5, "导出"),
    IMPORT(6, "导入"),
    CLEAR(7, "清空");

    private final int code;
    private final String label;

    OperBusinessTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OperBusinessTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperBusinessTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用码值 → 文案。null / 越界码值返回空串，不回落到「其他」。 */
    public static String getText(Integer code) {
        OperBusinessTypeEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或越界码值返回「未知(n)」（null 本身渲染成「未知」）。 */
    public static String labelOrUnknown(Integer code) {
        OperBusinessTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
