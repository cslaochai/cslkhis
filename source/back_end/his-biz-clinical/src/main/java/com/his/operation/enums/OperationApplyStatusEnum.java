package com.his.operation.enums;

import lombok.Getter;

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

    /**
     * 码值 → 展示文案（本枚举文案唯一出口）。
     *
     * <p><b>本枚举声明的缺省展示文案是「—」</b>：null（未填写）渲染为「—」；
     * 合法码值取 label；脏值（不在枚举内的越界码值）返回空串 {@code ""}，
     * 绝不回落合法文案（状态回落成"已完成"等于把没核实的事记成核实了），也绝不返回 null。
     * 异常 / 审计场景需保留原始码值时用 {@link #labelOrUnknown(Integer)}。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "—";
        }
        OperationApplyStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 渲染「未知」，脏值渲染「未知(n)」保留原始码值；
     * 绝不用于前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        OperationApplyStatusEnum e = code == null ? null : fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }
}
