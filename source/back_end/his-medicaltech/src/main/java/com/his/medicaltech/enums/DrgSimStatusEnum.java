package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * DRG 模拟分组状态枚举（码值口径 = 模拟结果 sim_status 列注释）。
 */
@Getter
public enum DrgSimStatusEnum {

    GROUPED(1, "已分组"),
    UNGROUPED(2, "未分组");

    private final int code;
    private final String label;

    DrgSimStatusEnum(int code, String label) {
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
