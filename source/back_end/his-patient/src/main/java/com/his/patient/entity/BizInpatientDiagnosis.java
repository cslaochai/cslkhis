package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 住院诊断明细（病案首页诊断明细）—— 病案首页的诊断侧
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_diagnosis")
public class BizInpatientDiagnosis extends BaseEntity {

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 序号（主要诊断固定为 1）
     */
    private Integer seqNo;

    /**
     * 诊断类型（1-主要诊断 2-其他诊断）
     */
    private Integer diagType;

    /**
     * ICD-10 编码
     */
    private String icdCode;

    /**
     * 诊断名称（必须与病历书写一致）
     */
    private String icdName;

    /**
     * 入院病情（1-有 2-临床未确定 3-情况不明 4-无）
     */
    private Integer admitCondition;

    /**
     * 并发症合并症级别：NONE / CC / MCC
     */
    private String ccLevel;

    /**
     * 诊断依据（病历中的支持性描述）
     */
    private String diagnosisBasis;
}
