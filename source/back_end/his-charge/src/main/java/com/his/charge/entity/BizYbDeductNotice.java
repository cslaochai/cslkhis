package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医保扣款通知单（申诉 → 确认追责 → 缴回闭环）。
 *
 * <p>状态机：1-待确认 →（申诉）2-申诉中 →（结果）3-申诉成功 / 4-维持扣款待缴 →（缴回）5-已缴回；
 * 1 →（直接确认）4；1 →（作废）6。超期是展示态（handle_deadline 早于今天且状态 1/2），不落列。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_yb_deduct_notice")
public class BizYbDeductNotice extends BaseEntity {

    /**
     * 扣款单号（DK+yyyyMMdd+4位）
     */
    private String deductNo;

    /**
     * 来源（字典 his_yb_deduct_source：1-飞检现场发现 2-智能审核/事后复核转来）
     */
    private Integer sourceType;

    /**
     * 关联飞检批次ID
     */
    private Long inspectionId;

    /**
     * 飞检批次号快照
     */
    private String inspectionNo;

    /**
     * 关联医保结算清单ID（可选）
     */
    private Long settlementId;

    /**
     * 结算清单号快照
     */
    private String settlementNo;

    /**
     * 就诊类型（1-门诊 2-住院）
     */
    private Integer encounterType;

    /**
     * 就诊标识
     */
    private Long encounterId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 患者姓名快照
     */
    private String patientName;

    /**
     * 患者编号快照
     */
    private String patientNo;

    /**
     * 被审科室ID
     */
    private Long deptId;

    /**
     * 被审科室名称快照
     */
    private String deptName;

    /**
     * 责任医师姓名快照
     */
    private String doctorName;

    /**
     * 违规类型（字典 his_yb_violation_type）
     */
    private Integer violationType;

    /**
     * 违规事实描述
     */
    private String violationDesc;

    /**
     * 扣款金额（>0）
     */
    private BigDecimal deductAmount;

    /**
     * 通知日期
     */
    private LocalDate noticeDate;

    /**
     * 处理期限
     */
    private LocalDate handleDeadline;

    /**
     * 状态（1-待确认 2-申诉中 3-申诉成功 4-维持扣款待缴 5-已缴回 6-已作废）
     */
    private Integer deductStatus;

    /**
     * 申诉理由
     */
    private String appealReason;

    /**
     * 申诉材料说明
     */
    private String appealMaterial;

    /**
     * 申诉发起人
     */
    private String appealBy;

    /**
     * 申诉时间
     */
    private LocalDateTime appealTime;

    /**
     * 申诉结果（字典 his_yb_appeal_result：1-成功 2-驳回）
     */
    private Integer appealResult;

    /**
     * 申诉结果说明
     */
    private String appealResultRemark;

    /**
     * 申诉结果录入人
     */
    private String appealResultBy;

    /**
     * 申诉结果录入时间
     */
    private LocalDateTime appealResultTime;

    /**
     * 责任科室ID
     */
    private Long liableDeptId;

    /**
     * 责任科室名称
     */
    private String liableDeptName;

    /**
     * 责任人姓名
     */
    private String liableEmpName;

    /**
     * 损失承担方式（1-院方承担 2-科室承担 3-个人承担 4-科室+个人共担）
     */
    private Integer lossBearType;

    /**
     * 科室承担金额
     */
    private BigDecimal bearDeptAmount;

    /**
     * 个人承担金额
     */
    private BigDecimal bearEmpAmount;

    /**
     * 确认经办人
     */
    private String confirmBy;

    /**
     * 确认时间
     */
    private LocalDateTime confirmTime;

    /**
     * 实际缴回金额
     */
    private BigDecimal paidAmount;

    /**
     * 缴回日期
     */
    private LocalDate paybackDate;

    /**
     * 缴回凭证号/转账流水
     */
    private String paybackVoucher;

    /**
     * 缴回经办人
     */
    private String paybackBy;

    /**
     * 缴回录入时间
     */
    private LocalDateTime paybackTime;

    /**
     * 作废原因（必填）
     */
    private String cancelReason;

    /**
     * 作废经办人
     */
    private String cancelBy;

    /**
     * 作废时间
     */
    private LocalDateTime cancelTime;
}
