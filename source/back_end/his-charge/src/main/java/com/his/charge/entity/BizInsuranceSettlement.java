package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医保结算清单
 */
@Data
@TableName("biz_insurance_settlement")
public class BizInsuranceSettlement {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 结算清单号
     */
    private String settlementNo;

    /**
     * 关联结算账单ID（sql/136：清单由 L2 出账生成，一张账单一张，唯一键 {@code uk_isb_bill}）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 关联结算账单号（冗余快照，2304 报文的 visit.billNo）
     */
    private String billNo;

    /**
     * 就诊类型（1-门诊 2-住院），与账单同口径；不是 visit_type 初/复诊
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
     * 患者号
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
     * 身份证号
     */
    private String idCard;

    /**
     * 医保卡号
     */
    private String medicalInsuranceNo;

    /**
     * 挂号ID快照（门诊时有值）：合规审核按它找病历与手术依据，不再是定位收费单的入口
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 就诊类型（初诊/复诊）
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
     * 诊断编码（ICD-10）
     */
    private String diagnosisCode;

    /**
     * 诊断名称
     */
    private String diagnosisName;

    /**
     * 医疗总费用 = 账单应收合计 - 院内优惠（医保按实际费用结算，让利不报给医保）
     */
    private BigDecimal totalAmount;

    /**
     * 药品费
     */
    private BigDecimal drugAmount;

    /**
     * 检查费
     */
    private BigDecimal inspectionAmount;

    /**
     * 检验费
     */
    private BigDecimal laboratoryAmount;

    /**
     * 治疗费
     */
    private BigDecimal treatmentAmount;

    /**
     * 材料费
     */
    private BigDecimal materialAmount;

    /**
     * 其他费用
     */
    private BigDecimal otherAmount;

    /**
     * 结算方式（1-自费 2-医保），与账单 {@code settlement_mode} 同口径；险种名称在 insuranceType
     */
    private Integer settlementType;

    /**
     * 医保类型（城镇职工/城乡居民等档案原文）
     */
    private String insuranceType;

    /**
     * 统筹报销比例（%）
     */
    private BigDecimal coverageRatio;

    /**
     * 医保统筹支付 = 账单统筹金额：后付给医保局的钱，不是支付方式
     */
    private BigDecimal insurancePay;

    /**
     * 个人账户支付 = 本账单 {@code pay_method=4} 的成功收款流水合计（刷参保人卡的真钱）
     */
    private BigDecimal personalPay;

    /**
     * 患者自付 = 现金/微信/支付宝/银行卡等流水合计 = total - insurancePay - personalPay
     */
    private BigDecimal selfPay;

    /**
     * 清单状态，字典 {@code his_ins_settlement_status}：1-待结算 2-已结算 3-已上传 4-已审核 5-已作废
     */
    private Integer settlementStatus;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 审核状态（0-待审核 1-审核通过 2-审核驳回）
     */
    private Integer auditStatus;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核意见
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
     * 预估费用
     */
    private BigDecimal estimatedCost;

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
}
