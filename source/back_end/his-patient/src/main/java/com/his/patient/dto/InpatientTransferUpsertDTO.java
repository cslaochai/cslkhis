package com.his.patient.dto;

import com.his.common.validation.InEnum;
import com.his.patient.enums.TransferTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 发起转科入参（P4.2）。
 */
@Data
public class InpatientTransferUpsertDTO implements Serializable {

    /**
     * 入院ID（必填）
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 转入科室ID（必填，必须与原科室不同 —— 同科室请走换床）
     */
    @NotNull(message = "转入科室不能为空")
    private Long toDeptId;

    /**
     * 转入病区ID（必填，必须属于转入科室）
     */
    @NotNull(message = "转入病区不能为空")
    private Long toWardId;

    /**
     * 转入床位ID（必填，必须空闲且属于转入病区/科室）
     */
    @NotNull(message = "转入床位不能为空")
    private Long toBedId;

    /**
     * 转科类型：1-普通转科 2-急诊转科 3-转入ICU 4-ICU转出（为空按普通转科）
     */
    @InEnum(value = TransferTypeEnum.class, message = "转科类型取值不合法（应为 1~4）")
    private Integer transferType;

    /**
     * 转科原因（必填：转科是一个医疗决定，必须有人负责并写清理由）
     */
    @NotBlank(message = "转科原因不能为空（转科是一个医疗决定，必须写清理由）")
    private String transferReason;

    /**
     * 备注
     */
    private String remark;
}
