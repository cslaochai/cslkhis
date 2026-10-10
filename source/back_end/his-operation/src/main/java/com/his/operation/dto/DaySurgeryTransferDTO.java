package com.his.operation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术转住院入参（术后观察 → 已转住院，终态）。
 */
@Data
public class DaySurgeryTransferDTO implements Serializable {

    @NotNull(message = "登记单ID不能为空")
    private Long id;

    /**
     * 转住院的住院ID
     */
    @NotNull(message = "转住院的住院ID不能为空")
    private Long transferAdmissionId;

    /**
     * 转住院原因
     */
    @NotBlank(message = "转住院原因不能为空")
    private String transferRemark;
}
