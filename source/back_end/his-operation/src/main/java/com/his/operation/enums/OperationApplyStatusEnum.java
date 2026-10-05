package com.his.operation.enums;

import lombok.Getter;

/**
 * 手术申请单状态枚举（码值口径 = sql/38 列注释，前端筛选值必须逐一对齐）。
 *
 * <p>取代原 {@code OperationApplyLabels.ST_*} int 常量；文案单点仍在 OperationApplyLabels。
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

    public static OperationApplyStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperationApplyStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
