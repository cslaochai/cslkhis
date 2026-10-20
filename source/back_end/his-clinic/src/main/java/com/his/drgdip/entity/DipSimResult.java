package com.his.drgdip.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * DIP 分组模拟结果实体（每个病案首页一条，按版本隔离，重跑覆盖）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_dip_sim_result")
public class DipSimResult extends BaseEntity {

    /**
     * 病案首页ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long summaryId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 分组时使用的主诊断编码
     */
    private String mainDiagCode;

    /**
     * 主诊断名称
     */
    private String mainDiagName;

    /**
     * 分组时使用的手术操作编码（快照，取主要操作）
     */
    private String operCode;

    /**
     * 手术操作名称
     */
    private String operName;

    /**
     * 入组病种类型（1-核心病种 2-综合病种）
     */
    private Integer diseaseType;

    /**
     * 病种编码
     */
    private String dipCode;

    /**
     * 病种名称
     */
    private String dipName;

    /**
     * 病种库版本
     */
    private String version;

    /**
     * 基准分值
     */
    private BigDecimal baseScore;

    /**
     * 病例组合系数（辅助目录命中系数，未命中为 1.0000）
     */
    private BigDecimal coeff;

    /**
     * 结算分值 = 基准分值 × 病例组合系数
     */
    private BigDecimal score;

    /**
     * 点值（元/分值，统筹区公布，快照）
     */
    private BigDecimal pointValue;

    /**
     * 预测结算金额 = 结算分值 × 点值
     */
    private BigDecimal settleAmount;

    /**
     * 实际住院费用
     */
    private BigDecimal actualAmount;

    /**
     * 盈亏 = 结算金额 - 实际费用
     */
    private BigDecimal profitAmount;

    /**
     * 结果（1-已入组 2-未入组）
     */
    private Integer simStatus;

    /**
     * 命中路径说明
     */
    private String ruleNote;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

}
