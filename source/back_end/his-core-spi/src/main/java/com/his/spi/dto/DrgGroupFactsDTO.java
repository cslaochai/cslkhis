package com.his.spi.dto;

import lombok.Data;

/**
 * 一次入组所需的病案事实（编码按首页原样传，归一与拆分由分组侧完成）。
 */
@Data
public class DrgGroupFactsDTO {

    /**
     * 主要诊断编码
     */
    private String mainDiag;

    /**
     * 主要手术操作编码（逗号拼接）
     */
    private String mainOperCodes;

    /**
     * 其他诊断编码（逗号拼接）
     */
    private String otherDiagCodes;

    /**
     * 其他手术操作编码（逗号拼接）
     */
    private String otherOperCodes;

    /**
     * 性别（码值权威见 SysGenderEnum）
     */
    private Integer gender;

    /**
     * 年龄数字，配合 ageUnit
     */
    private Integer age;

    /**
     * 年龄单位（1-岁 2-月 3-天）
     */
    private Integer ageUnit;

    /**
     * 入院体重（克）
     */
    private Integer admissionWeightG;
}
