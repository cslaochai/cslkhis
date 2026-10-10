package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 撤销合并入参（P5.1 EMPI）
 */
@Data
public class PatientMergeRevertDTO {

    /**
     * 合并审计记录ID（患者合并审计的ID）
     */
    @NotNull(message = "合并记录ID不能为空")
    private Long logId;

    /**
     * 撤销理由（必填 —— 撤销合并会让一份档案重新"活过来"，必须留痕）
     */
    @NotBlank(message = "撤销理由必填（撤销会让一份档案重新在册，必须留痕）")
    private String revertReason;
}
