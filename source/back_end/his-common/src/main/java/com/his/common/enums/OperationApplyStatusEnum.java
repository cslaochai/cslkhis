package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 手术申请单状态枚举（码值口径 = sql/38 列注释，前端筛选值必须逐一对齐）。
 */
@Getter
public enum OperationApplyStatusEnum {

    PENDING_SCHEDULE(0, "待排期"),
    SCHEDULED(1, "已排期"),
    PREOP_CHECKED(2, "术前核对完成"),
    FINISHED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String label;

    OperationApplyStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }

    /**
     * 是否处于"在途"（会占手术间时段、算未完成数）
     */
    public static boolean isActive(Integer status) {
        return SCHEDULED.is(status) || PREOP_CHECKED.is(status) || FINISHED.is(status);
    }

    /**
     * 是否"未完成"（待排期 / 已排期 / 术前核对完成）—— 工作台角标用
     */
    public static boolean isUnfinished(Integer status) {
        return PENDING_SCHEDULE.is(status) || SCHEDULED.is(status) || PREOP_CHECKED.is(status);
    }

    public static OperationApplyStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (OperationApplyStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        OperationApplyStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        OperationApplyStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        OperationApplyStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
