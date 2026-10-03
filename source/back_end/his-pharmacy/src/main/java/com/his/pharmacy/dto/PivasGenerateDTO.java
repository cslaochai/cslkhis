package com.his.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 生成静配单入参。
 *
 * <p>按「病区 + 调配日期」捞该病区在院患者的静脉用药医嘱；admissionId 可选，传了只排这一个患者。
 */
@Data
public class PivasGenerateDTO implements Serializable {

    /**
     * 病区ID（必填）
     */
    @NotNull(message = "病区不能为空")
    private Long wardId;

    /**
     * 调配日期（默认今天）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate admixDate;

    /**
     * 入院ID（可选：只排指定患者）
     */
    private Long admissionId;
}
