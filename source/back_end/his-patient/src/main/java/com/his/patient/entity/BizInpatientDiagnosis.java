package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 住院诊断明细（病案首页诊断明细）—— 病案首页的诊断侧
 * <p>与结算清单诊断明细的区别（不是重复表）：
 * 本表是<b>病历侧原始依据</b>；医保结算清单上的诊断是<b>申报口径</b>，由本表带过去后可能微调。
 * <p>{@code diagnosis_basis} 是四核对里「病历」一侧的落点：诊断必须能在病历中找到支持性描述。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_diagnosis")
public class BizInpatientDiagnosis extends BaseEntity {

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 序号（主要诊断固定为 1） */
    private Integer seqNo;

    /** 诊断类型（1-主要诊断 2-其他诊断） */
    private Integer diagType;

    /** ICD-10 编码 */
    private String icdCode;

    /** 诊断名称（必须与病历书写一致） */
    private String icdName;

    /** 入院病情（1-有 2-临床未确定 3-情况不明 4-无） */
    private Integer admitCondition;

    /** 并发症合并症级别：NONE / CC / MCC */
    private String ccLevel;

    /** 诊断依据（病历中的支持性描述） */
    private String diagnosisBasis;
}
