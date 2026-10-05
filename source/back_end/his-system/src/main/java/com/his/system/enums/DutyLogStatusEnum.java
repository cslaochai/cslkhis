package com.his.system.enums;

import lombok.Getter;

/**
 * 值班日志/交班本状态枚举（码值口径 = biz_duty_log.status 列注释）。
 *
 * <p>取代原 {@code DutyLogService.ST_*} 接口常量；登记写入口只允许 PENDING/DONE
 * （交班/签收由闭环推进，见 DutyLogServiceImpl）。
 */
@Getter
public enum DutyLogStatusEnum {

    PENDING(0, "待处理"),
    DONE(1, "已处理"),
    HANDED(2, "已交班"),
    ACKED(3, "已签收");

    private final int code;
    private final String label;

    DutyLogStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DutyLogStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DutyLogStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static String labelOf(Integer code) {
        DutyLogStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
