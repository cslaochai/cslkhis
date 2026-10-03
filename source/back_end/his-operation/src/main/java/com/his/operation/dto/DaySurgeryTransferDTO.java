package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术转住院入参（术后观察 → 已转住院，终态）。
 *
 * <p>转住院必须回填 admission_id：没有住院号就无法证明"这次住院从哪天算起"，
 * 病案与医保口径全断在这里。
 */
@Data
public class DaySurgeryTransferDTO implements Serializable {

    @NotNull(message = "登记单ID不能为空")
    private Long id;

    /** 转住院的住院ID */
    @NotNull(message = "转住院的住院ID不能为空")
    private Long transferAdmissionId;

    /** 转住院原因 */
    @NotNull(message = "转住院原因不能为空")
    private String transferRemark;
}
