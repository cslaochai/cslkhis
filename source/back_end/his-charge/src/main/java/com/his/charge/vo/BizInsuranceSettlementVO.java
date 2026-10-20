package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医保结算清单出参
 */
@Data
public class BizInsuranceSettlementVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 结算单号
     */
    private String settlementNo;

    /**
     * 结算账单ID（清单由 L2 出账生成，一张账单一张）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 结算账单号
     */
    private String billNo;

    /**
     * 就诊类型（1-门诊 2-住院），不是初/复诊
     */
    private Integer encounterType;

    /**
     * 就诊标识（门诊=挂号ID，住院=入院ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long encounterId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

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
     * 身份证号（明文只留在库与报盘报文里，出参一律置空）
     */
    private String idCard;

    /**
     * 身份证号（脱敏展示）
     */
    private String idCardMasked;

    /**
     * 医保卡号（明文同上，不出现在响应里）
     */
    private String medicalInsuranceNo;

    /**
     * 医保卡号（脱敏展示）
     */
    private String medicalInsuranceNoMasked;

    /**
     * 挂号ID快照
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 就诊类型：1-初诊 2-复诊
     */
    private String visitType;

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
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 诊断编码
     */
    private String diagnosisCode;

    /**
     * 诊断名称
     */
    private String diagnosisName;

    /**
     * 医疗总费用 = 账单应收合计 - 院内优惠
     */
    private BigDecimal totalAmount;

    /**
     * 药品费，单位：元
     */
    private BigDecimal drugAmount;

    /**
     * 检查费，单位：元
     */
    private BigDecimal inspectionAmount;

    /**
     * 检验费，单位：元
     */
    private BigDecimal laboratoryAmount;

    /**
     * 治疗费，单位：元
     */
    private BigDecimal treatmentAmount;

    /**
     * 材料费，单位：元
     */
    private BigDecimal materialAmount;

    /**
     * 其他费用，单位：元
     */
    private BigDecimal otherAmount;

    /**
     * 结算方式（1-自费 2-医保）
     */
    private Integer settlementType;

    /**
     * 医保类型（城镇职工/城镇居民/新农合等）
     */
    private String insuranceType;

    /**
     * 统筹报销比例（%）
     */
    private BigDecimal coverageRatio;

    /**
     * 医保统筹支付 = 账单 pool_amount
     */
    private BigDecimal insurancePay;

    /**
     * 个人账户支付 = 本账单 pay_method=4 的成功收款流水合计
     */
    private BigDecimal personalPay;

    /**
     * 自付金额，单位：元
     */
    private BigDecimal selfPay;

    /**
     * 清单状态（1-待结算 2-已结算 3-已上传 4-已审核 5-已作废）
     */
    private Integer settlementStatus;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    /**
     * 审核状态（0-待审核 1-审核通过 2-审核驳回）
     */
    private Integer auditStatus;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * DRG分组编码
     */
    private String drgCode;

    /**
     * DRG权重
     */
    private BigDecimal drgWeight;

    /**
     * 预估费用，单位：元
     */
    private BigDecimal estimatedCost;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

}
