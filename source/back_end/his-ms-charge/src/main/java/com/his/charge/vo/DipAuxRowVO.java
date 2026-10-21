package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DIP 辅助目录一行（biz_dip_aux，装载进分组方案快照）。
 */
@Data
public class DipAuxRowVO implements Serializable {

    /**
     * 绑定病种编码（空=适用于全部病种）
     */
    private String diseaseCode;

    /**
     * 辅助目录类型（1-年龄 2-合并症并发症 3-肿瘤严重程度/恶性程度 4-其他病例属性）
     */
    private Integer auxType;

    /**
     * 属性条件编码（合并症/肿瘤用 ICD 码；年龄型为空）
     */
    private String auxCode;

    /**
     * 属性条件名称（快照）
     */
    private String auxName;

    /**
     * 年龄下限（含，岁；仅年龄型辅助目录使用）
     */
    private Integer ageMin;

    /**
     * 年龄上限（含，岁；仅年龄型辅助目录使用）
     */
    private Integer ageMax;

    /**
     * 病例组合系数（分值调整倍率）
     */
    private BigDecimal coeff;
}
