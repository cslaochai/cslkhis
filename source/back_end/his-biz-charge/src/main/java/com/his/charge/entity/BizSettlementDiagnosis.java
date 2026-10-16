package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 结算清单诊断明细（编码依据链）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_settlement_diagnosis")
public class BizSettlementDiagnosis extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



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
     * 诊断类型（1-主要诊断 2-其他诊断）
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
     * 入院病情（1-有 2-临床未确定 3-情况不明 4-无）
     */
    private Integer admitCondition;

    /**
     * 并发症合并症级别：NONE / CC / MCC
     */
    private String ccLevel;

    /**
     * 依据核对结果。注意这是**三态**，不是布尔：
     * 1-命中 2-通过 3-不适用（缺依据，未评估）。
     * 用 0/1 布尔存会把「没评估」压成「通过」。
     */
    private Integer evidenceStatus;

    /**
     * 依据核对说明（"不适用"的原因必须写在这里，不允许空着冒充通过）
     */
    private String evidenceNote;
}
