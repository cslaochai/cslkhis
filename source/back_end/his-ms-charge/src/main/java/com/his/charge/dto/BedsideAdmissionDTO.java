package com.his.charge.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 床旁结算按入院操作的入参（试算 / 结算 / 查已出账单）。
 */
@Data
public class BedsideAdmissionDTO {

    /**
     * 入院ID（必填）
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 结算方式（1-自费 2-医保）：与住院结算页同契约——由收窗口的人显式选，
     * 服务端不按患者有无参保号静默推断（后端拿参保号也判不出"这次要不要走医保"）。
     * 选了医保但平台查无参保/停保时，服务端按自费原价出账、不拦单。
     */
    @AllowedValues(values = {"1", "2"}, message = "结算方式取值不合法")
    private Integer settleMode;
}
