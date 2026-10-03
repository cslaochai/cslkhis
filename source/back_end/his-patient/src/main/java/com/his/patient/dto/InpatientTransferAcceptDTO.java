package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 接收转科入参（P4.2：**转科在这一刻才真正生效**）。
 *
 * <p>接收人 = 当前登录用户，不接受前端传入 —— 接收是一个责任动作，
 * 允许替别人接收，等于让病历上的签名变成可代填的字段。
 */
@Data
public class InpatientTransferAcceptDTO implements Serializable {

    /**
     * 转科记录ID（必填）
     */
    @NotNull(message = "转科记录ID不能为空")
    private Long transferId;

    /**
     * 接收备注（可空；医嘱处置说明由服务层自动写入 order_remark）
     */
    private String remark;
}
