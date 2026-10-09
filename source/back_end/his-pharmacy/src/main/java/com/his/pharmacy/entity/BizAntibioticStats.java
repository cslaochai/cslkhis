package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 抗菌药物使用监测指标（月度快照：使用率 / 使用强度 AUD / 微生物送检率）。
 */
@Data
@TableName("biz_antibiotic_stats")
public class BizAntibioticStats implements Serializable {
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

    private static final long serialVersionUID = 1L;

    /** 统计范围：全院 */
    public static final int SCOPE_HOSPITAL = 1;
    /** 统计范围：科室 */
    public static final int SCOPE_DEPT = 2;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 统计月份（yyyy-MM） */
    private String statMonth;

    /** 统计范围（1-全院 2-科室） */
    private Integer scopeType;

    /** 科室ID（scopeType=2 必有） */
    private Long deptId;

    /** 科室名称 */
    private String deptName;

    /** 门急诊处方总数（处方状态 3/4，源 1/2） */
    private Integer opRxCount;

    /** 含抗菌药物的门急诊处方数 */
    private Integer opAbxRxCount;

    /** 门诊抗菌药物使用率（%） */
    private BigDecimal opUsageRate;

    /** 同期出院患者数 */
    private Integer ipDischargeCount;

    /** 出院患者中使用抗菌药物的人数 */
    private Integer ipAbxPatientCount;

    /** 住院抗菌药物使用率（%） */
    private BigDecimal ipUsageRate;

    /** 收治患者人天数 */
    private Integer patientDays;

    /** 抗菌药物累计 DDD 数 */
    private BigDecimal ddds;

    /** 使用强度 AUD（DDDs/100人天） */
    private BigDecimal aud;

    /** 使用抗菌药物的住院患者数（送检率分母） */
    private Integer abxTreatCount;

    /** 其中送检微生物标本的患者数（送检率分子） */
    private Integer microSubmitCount;

    /** 微生物标本送检率（%） */
    private BigDecimal microSubmitRate;

    /** 未匹配到抗菌药物目录的住院药品医嘱数（数据质量提示） */
    private Integer unmatchedOrderCount;

    /** 生成人 */
    private String generateBy;

    /** 生成时间 */
    private LocalDateTime generateTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;
}
