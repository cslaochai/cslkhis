package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院病案首页
 * <p>与入院记录 1:1。它是 <b>DRG/DIP 分组的唯一输入</b>，也是医保结算清单的上游依据。
 * <p>字段划分参考《医疗保障基金结算清单》与住院病案首页填写规范：基本信息 / 住院过程 / 离院信息 / 费用信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_summary")
public class BizInpatientSummary extends BaseEntity {

    /**
     * 入院ID（入院记录的入院ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 年龄单位：1-岁 2-月 3-天（不足1岁需按月/天，真实首页要求）
     */
    private Integer ageUnit;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 入院科别ID（转科不改；本实体的 deptId/deptName 是**出院科别**）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admitDeptId;

    /**
     * 入院科别名称
     */
    private String admitDeptName;

    /**
     * 医保卡号
     */
    private String medicalInsuranceNo;

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
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床位号
     */
    private String bedNo;

    /**
     * 入院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admitTime;

    /**
     * 出院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dischargeTime;

    /**
     * 实际住院天数（出院日期 - 入院日期，不足 1 天按 1 天）
     */
    private Integer inpatientDays;

    /**
     * 入院途径（1-门诊 2-急诊 3-转院 4-其他）
     */
    private Integer admitWay;

    /**
     * 离院方式：1-医嘱离院 2-医嘱转院 3-医嘱转社区 4-非医嘱离院 5-死亡 9-其他
     */
    private Integer dischargeWay;

    /**
     * 死亡标志（0-否 1-是）
     */
    private Integer deathFlag;

    /**
     * 31日内再入院（0-否 1-是，出院时自动判定，DRG 绩效指标）
     */
    @TableField("readmit_31d")
    private Integer readmit31d;

    /**
     * 是否手术（0-否 1-是，由手术明细自动置位）
     */
    private Integer isSurgery;

    /**
     * 是否输血（0-否 1-是，由输血闭环完成时置位）。
     *
     * <p>与 isSurgery / isRescue / isCritical 同构：首页就是靠这几个
     * "该住院是否发生过某事"的标志位取数的（DRG 入组与首页完整性校验都要用）。
     * 只置 1、不置回 0 —— 输血记录不可取消（完成后只能补报反应），所以这个标志没有回退路径。
     */
    private Integer isTransfusion;

    /**
     * 是否抢救（0-否 1-是）
     */
    private Integer isRescue;

    /**
     * 是否危重（0-否 1-是）
     */
    private Integer isCritical;

    /**
     * 主要诊断编码（冗余，随诊断明细自动同步，便于分组与检索）
     */
    private String mainDiagnosisCode;

    /**
     * 主要诊断名称
     */
    private String mainDiagnosisName;

    /**
     * 住院总费用
     */
    private BigDecimal totalAmount;

    /**
     * 西药费
     */
    private BigDecimal westernDrugAmount;

    /**
     * 中成药费
     */
    private BigDecimal chineseDrugAmount;

    /**
     * 中药饮片费
     */
    private BigDecimal herbalAmount;

    /**
     * 检查费
     */
    private BigDecimal examAmount;

    /**
     * 检验费
     */
    private BigDecimal labAmount;

    /**
     * 治疗费
     */
    private BigDecimal treatmentAmount;

    /**
     * 手术费
     */
    private BigDecimal operationAmount;

    /**
     * 耗材费
     */
    private BigDecimal materialAmount;

    /**
     * 床位费
     */
    private BigDecimal bedAmount;

    /**
     * 护理费
     */
    private BigDecimal nursingAmount;

    /**
     * 其他费用
     */
    private BigDecimal otherAmount;

    /**
     * 首页状态：1-草稿 2-已提交 3-已归档（归档后禁止修改）
     */
    private Integer summaryStatus;
}
