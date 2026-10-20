package com.his.common.enums;

import lombok.Getter;

/**
 * DIP 模拟分组状态枚举（码值口径 = biz_dip_sim_result.sim_status 列注释）。
 */
@Getter
public enum DipSimStatusEnum {

    GROUPED(1, "已入组"),
    UNGROUPED(2, "未入组");

    private final int code;
    private final String label;

    DipSimStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
