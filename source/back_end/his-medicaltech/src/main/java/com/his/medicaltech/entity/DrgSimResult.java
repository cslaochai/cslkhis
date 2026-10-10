package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DRG 分组模拟结果实体（每个病案首页一条，重跑覆盖）。
 */
@Data
@TableName("biz_drg_sim_result")
public class DrgSimResult {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 是否手术
     */
    private Integer isSurgery;

    /**
     * 住院天数
     */
    private Integer inpatientDays;

    /**
     * 入组编码
     */
    private String drgCode;

    /**
     * 组名称
     */
    private String drgName;

    /**
     * MDC 大类
     */
    private String mdcCode;

    /**
     * 权重 RW
     */
    private BigDecimal weight;

    /**
     * 病组支付标准（元）
     */
    private BigDecimal payStandard;

    /**
     * 实际住院费用
     */
    private BigDecimal actualAmount;

    /**
     * 盈亏 = 支付标准 - 实际费用
     */
    private BigDecimal profitAmount;

    /**
     * 结果（1-已入组 2-未入组）
     */
    private Integer simStatus;

    /**
     * 命中规则说明
     */
    private String ruleNote;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人 ID
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createById;

    /**
     * 更新人 ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateById;
}
