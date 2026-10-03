package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.charge.entity.BizSettlementBillItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 医保结算清单详情 VO
 * <p>
 * 结算清单表本身只存了冗余快照，很多字段为空，这里按 patientId / registId / billId
 * 关联患者、挂号、病历与结算账单（含账单行），补齐清单所需的完整信息。
 */
@Data
public class InsuranceSettlementDetailVO implements Serializable {

    // 清单信息

    /**
     * 结算清单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 结算单号
     */
    private String settlementNo;

    /**
     * 清单状态，字典 {@code his_ins_settlement_status}：1-待结算 2-已结算 3-已上传 4-已审核 5-已作废
     */
    private Integer settlementStatus;

    /**
     * 结算方式（1-自费 2-医保），险种名称在 insuranceType
     */
    private Integer settlementType;

    /**
     * 医保类型（城镇职工/城镇居民/新农合等）
     */
    private String insuranceType;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    // 审核信息

    /**
     * 审核状态：0-待审核 1-审核通过 2-审核驳回
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

    // 患者信息

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
     * 身份证号（清单是纯展示口径，明文不出响应体）
     */
    private String idCard;

    /**
     * 身份证号（脱敏展示）
     */
    private String idCardMasked;

    /**
     * 医保卡号（明文不出响应体）
     */
    private String medicalInsuranceNo;

    /**
     * 医保卡号（脱敏展示）
     */
    private String medicalInsuranceNoMasked;

    /**
     * 联系电话（明文不出响应体）
     */
    private String phone;

    /**
     * 联系电话（脱敏展示）
     */
    private String phoneMasked;

    /**
     * 患者类型：1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-其他
     */
    private Integer patientType;

    // 就诊信息

    /**
     * 挂号ID快照
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 挂号单号
     */
    private String registNo;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 就诊类型：1-初诊 2-复诊
     */
    private String visitType;

    /**
     * 科室名称
     */
    private String deptName;

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

    // 关联结算账单（L2）

    /**
     * 结算账单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 结算账单号
     */
    private String billNo;

    /**
     * 账单状态，字典 {@code his_bill_status}：1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费
     */
    private Integer billStatus;

    /**
     * 账单应收合计（医保前总价）
     */
    private BigDecimal billTotalAmount;

    /**
     * 院内优惠/抹零（不含统筹）
     */
    private BigDecimal discountAmount;

    /**
     * 患者应缴 = 应收 - 优惠 - 统筹
     */
    private BigDecimal payableAmount;

    /**
     * 已收合计（权威在支付资金流水，这是账单上的镜像值）
     */
    private BigDecimal paidAmount;

    /**
     * 已退合计
     */
    private BigDecimal refundAmount;

    /**
     * 账务归属日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    /**
     * 收讫时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /**
     * 出账人姓名
     */
    private String billByName;

    /**
     * 发票号（L4 票据，挂在账单上）
     */
    private String invoiceNo;

    /**
     * 入院单号（住院清单时有值）
     */
    private String admissionNo;

    // 费用明细

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

    // 医保结算

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
     * 自费金额，单位：元
     */
    private BigDecimal selfPay;

    // DRG

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

    // 账单行（逐项目 split）

    /**
     * 账单行列表：2304 报文与合规审核共用的同一份明细口径
     */
    private List<BizSettlementBillItem> items;
}
