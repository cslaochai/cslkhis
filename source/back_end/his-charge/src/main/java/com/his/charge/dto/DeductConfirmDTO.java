package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 确认扣款并追责（三要素：责任科室 + 损失承担方式 + 分摊金额；
 * 科室+个人共担时两者之和 ≤ 扣款金额，差额视为院方承担，不强制凑满）。
 */
@Data
public class DeductConfirmDTO {

    @NotNull(message = "扣款通知ID不能为空")
    private Long id;

    /**
     * 责任科室ID
     */
    @NotNull(message = "责任科室不能为空")
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
    @NotNull(message = "损失承担方式不能为空")
    private Integer lossBearType;

    /**
     * 科室承担金额
     */
    private BigDecimal bearDeptAmount;

    /**
     * 个人承担金额
     */
    private BigDecimal bearEmpAmount;
}
