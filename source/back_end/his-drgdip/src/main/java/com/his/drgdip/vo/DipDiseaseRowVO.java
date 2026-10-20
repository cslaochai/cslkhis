package com.his.drgdip.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DIP 病种目录一行（biz_dip_disease，病种目录页与分组方案快照共用）。
 */
@Data
public class DipDiseaseRowVO implements Serializable {

    /**
     * 病种行主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 病种编码（核心病种=下发病种库编码；综合病种=按主诊断归组的目录码）
     */
    private String diseaseCode;

    /**
     * 病种名称
     */
    private String diseaseName;

    /**
     * 病种类型（1-核心病种 2-综合病种）
     */
    private Integer diseaseType;

    /**
     * 主诊断编码（医保版贯标码；综合病种=归组用的目录码/前缀码）
     */
    private String mainDiagCode;

    /**
     * 主诊断名称（快照）
     */
    private String mainDiagName;

    /**
     * 手术操作编码（核心病种的手术操作，可空=内科/保守治疗病种）
     */
    private String operCode;

    /**
     * 手术操作名称（快照）
     */
    private String operName;

    /**
     * 主诊断匹配方式（1-整码 2-前缀；综合病种一般为前缀归组）
     */
    private Integer diagMatch;

    /**
     * 病种基准分值（统筹区下发，未下发为 null）
     */
    private BigDecimal baseScore;

    /**
     * 数据来源
     */
    private String source;

    /**
     * 病种库版本
     */
    private String version;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
