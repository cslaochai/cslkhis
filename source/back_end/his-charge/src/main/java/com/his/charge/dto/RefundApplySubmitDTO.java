package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提交退费申请入参
 */
@Data
public class RefundApplySubmitDTO {

    /**
     * 原结算账单ID（L2）
     */
    @NotNull(message = "缺少原结算账单，不能发起退费申请")
    private Long billId;

    /**
     * 账单号（快照，可空：服务端按 billId 兜出）
     */
    private String billNo;

    /**
     * 患者ID
     */
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
     * 退费类型（字典 {@code his_refund_apply_type}：1-退药 2-退检查 3-退检验 4-退治疗 5-全部退费）
     */
    @NotNull(message = "请选择退费类型")
    private Integer refundType;

    /**
     * 退费原因
     */
    @NotBlank(message = "请填写退费原因")
    private String refundReason;

    /**
     * 退费金额，单位：元（必须等于该类型下若干<b>整条</b>账单行金额之和，服务端校验）
     */
    @NotNull(message = "请填写退费金额")
    private BigDecimal refundAmount;

    /**
     * 申请人
     */
    private String applyBy;

}
