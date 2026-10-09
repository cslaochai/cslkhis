package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;

/**
 * 营养膳食月度指标（sql/168 §4）。
 */
@Data
@TableName("biz_nutrition_stats")
public class BizNutritionStats {
    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 统计月份 yyyy-MM
     */
    private String statMonth;
    /**
     * 统计范围（1-全院 2-科室）
     */
    private Integer scopeType;
    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 同期出院患者数
     */
    private Integer dischargeCount;
    /**
     * 其中出院前做过 NRS2002 筛查的患者数
     */
    private Integer screenedCount;
    /**
     * 营养风险筛查率（%）
     */
    private BigDecimal screenRate;
    /**
     * 筛查阳性
     */
    private Integer riskCount;
    /**
     * 筛查阳性率（%）
     */
    private BigDecimal riskRate;

    /**
     * 膳食方案总数
     */
    private Integer dietPlanCount;
    /**
     * 其中营养科已接收
     */
    private Integer dietConfirmCount;
    /**
     * 膳食医嘱执行率（%）
     */
    private BigDecimal dietConfirmRate;

    /**
     * 营养会诊单数
     */
    private Integer consultCount;
    // 列名是 consult_ontime_*（sql/168 §4），MP 默认按驼峰翻成 consult_on_time_* 会 Unknown column
    /**
     * 其中按时应答的条数
     */
    @TableField("consult_ontime_count")
    private Integer consultOnTimeCount;
    /**
     * 营养会诊及时应答率（%）
     */
    @TableField("consult_ontime_rate")
    private BigDecimal consultOnTimeRate;

    /**
     * 订餐明细数
     */
    private Integer mealOrderCount;
    /**
     * 其中已签收的明细数
     */
    private Integer mealSignedCount;
    /**
     * 订餐签收率（%）
     */
    private BigDecimal mealSignRate;
    /**
     * 退订明细数
     */
    private Integer mealCancelCount;

    /**
     * 生成人
     */
    private String generateBy;
    /**
     * 生成时间
     */
    private LocalDateTime generateTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 备注
     */
    private String remark;
}
