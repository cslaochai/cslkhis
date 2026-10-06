package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收费员交班入参（G8 班结）。
 *
 * <p>⚠ <b>不带 cashierId</b>：收费员身份由服务端从登录态取
 * （{@code UserUtils.getCurrentUser().getEmployeeId()}）。
 * 前端传"我是谁"等于让班结单可以随便挂到别人名下 —— 交班是签名性质的动作。
 */
@Data
public class CashierHandoverDTO {

    /**
     * 班次（1-白班 2-夜班 3-其他）。仅作展示标签，不参与统计口径。
     */
    @NotNull(message = "班次不能为空")
    private Integer shiftType;

    /**
     * 实交现金（收费员清点后录入）。交班必须点钞，故必填。
     */
    @NotNull(message = "实交现金不能为空")
    private BigDecimal handinCash;

    /**
     * 差异说明。系统现金与实交现金不一致时必填（后端校验）；
     * 一致时可不填 —— 但不允许"有差异还留空"，那样差异就没人解释了。
     */
    private String diffReason;

    /**
     * 备注
     */
    private String remark;
}
