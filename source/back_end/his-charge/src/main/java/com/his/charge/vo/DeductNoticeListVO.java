package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 扣款通知单行（列表/详情共用；详情再挂 logs）。
 */
@Data
public class DeductNoticeListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 扣款单号
     */
    private String deductNo;

    /**
     * 来源（字典 his_yb_deduct_source）
     */
    private Integer sourceType;

    /**
     * 关联飞检批次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionId;

    /**
     * 飞检批次号快照
     */
    private String inspectionNo;

    /**
     * 关联医保结算清单ID（可选）
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 扣款金额
     */
    private BigDecimal deductAmount;

    /**
     * 通知日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate noticeDate;

    /**
     * 处理期限
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealTime;

    /**
     * 申诉结果（字典 his_yb_appeal_result）
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealResultTime;

    /**
     * 责任科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;

    /**
     * 实际缴回金额
     */
    private BigDecimal paidAmount;

    /**
     * 缴回日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paybackTime;

    /**
     * 作废原因（必填）
     */
    private String cancelReason;
    private String cancelBy;

    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 超期未结（展示态：handle_deadline 早于今天且状态为待确认/申诉中）
     */
    private Boolean overdue;

    /**
     * 距处理期限剩余天数（负数=已超期；未结单据才有意义）
     */
    private Integer deadlineDays;
}
