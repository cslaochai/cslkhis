package com.his.charge.dto;

import lombok.Data;

/**
 * 结算清单诊断明细维护入参
 */
@Data
public class SettlementDiagnosisUpsertDTO {

    /**
     * 主键ID，新增时为空
     */
    private Long id;

    /**
     * 序号
     */
    private Integer seqNo;

    /**
     * 诊断类型：1-主要诊断 2-其他诊断
     */
    private Integer diagType;

    /**
     * ICD-10 编码
     */
    private String icdCode;

    /**
     * 诊断名称
     */
    private String icdName;

    /**
     * 入院病情：1-有 2-临床未确定 3-情况不明 4-无
     */
    private Integer admitCondition;

    /**
     * 并发症合并症级别：NONE / CC / MCC
     */
    private String ccLevel;

    /**
     * 备注
     */
    private String remark;
}
