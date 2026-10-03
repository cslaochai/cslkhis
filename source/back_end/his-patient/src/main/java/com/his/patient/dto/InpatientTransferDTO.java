package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 换床入参
 * <p>第 1 期只支持<b>同一科室内部</b>换床（含同科室不同病区）。跨科室属「转科」，
 * 会改变入院科室与主诊医师，需要转科流程（后续期次），此处直接拒绝，避免把转科记录伪装成换床。
 */
@Data
public class InpatientTransferDTO {

    /** 入院ID（必填） */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /** 目标床位ID（必填，必须空闲且与原床位同科室） */
    @NotNull(message = "目标床位不能为空")
    private Long newBedId;

    /** 换床原因 */
    private String reason;
}
