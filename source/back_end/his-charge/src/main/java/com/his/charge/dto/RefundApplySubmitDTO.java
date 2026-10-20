package com.his.charge.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交退费申请入参
 */
@Data
public class RefundApplySubmitDTO {

    /**
     * 原结算账单ID
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
    @AllowedValues(values = {"1", "2", "3", "4", "5"}, message = "退费类型不合法")
    private Integer refundType;

    /**
     * 退费原因
     */
    @NotBlank(message = "请填写退费原因")
    private String refundReason;

    /**
     * 退费金额，单位：元。带了 {@link #items} 时服务端按行快照汇总覆盖此值（前端传来的只作展示）；
     * 没带 items 的旧路径必须等于若干<b>整条</b>账单行金额之和，服务端校验。
     */
    @NotNull(message = "请填写退费金额")
    private BigDecimal refundAmount;

    /**
     * 退费明细行（勾选 + 每行退费数量）；为空 = 旧的「类型 + 金额」整行凑单路径。
     *
     * <p>前端按账单 + 退费类型拉候选行（{@code /charge/refund/refundableLines}）后勾选，
     * {@code refundQuantity} 留空 = 该行整行退。
     */
    @Valid
    private List<RefundApplyItemDTO> items;

    /**
     * 申请人
     */
    private String applyBy;

}
