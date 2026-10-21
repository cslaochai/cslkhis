package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 结算清单诊断明细出参
 */
@Data
public class SettlementDiagnosisVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 结算清单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settlementId;

    /**
     * 序号
     */
    private Integer seqNo;

    /**
     * 诊断类型：1-主要诊断 2-其他诊断
     */
    private Integer diagType;

    /**
     * 诊断类型中文
     */
    private String diagTypeText;

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
     * 入院病情中文
     */
    private String admitConditionText;

    /**
     * 并发症合并症级别：NONE / CC / MCC
     */
    private String ccLevel;

    /**
     * 依据核对结果：1-命中 2-通过 3-不适用（缺依据，未评估）
     */
    private Integer evidenceStatus;

    /**
     * 依据核对结果中文（三态：命中/通过/不适用）
     */
    private String evidenceStatusText;

    /**
     * 依据核对说明
     */
    private String evidenceNote;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
