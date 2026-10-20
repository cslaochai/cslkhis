package com.his.common.enums;

import lombok.Getter;

/**
 * DIP 病种类型枚举（码值口径 = biz_dip_disease.disease_type 列注释）。
 */
@Getter
public enum DipDiseaseTypeEnum {

    /**
     * 核心病种：主诊断（整码）+ 手术操作（整码）组合精确匹配
     */
    CORE(1, "核心病种"),
    /**
     * 综合病种：未进入核心病种的病例按主诊断前缀归组
     */
    COMPREHENSIVE(2, "综合病种");

    private final int code;
    private final String label;

    DipDiseaseTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }

    /**
     * 码值反查枚举；未知码返回 null（目录数据有脏值时不崩，交由调用方按空处理）
     */
    public static DipDiseaseTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DipDiseaseTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
