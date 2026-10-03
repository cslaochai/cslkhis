package com.his.pharmacy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 生成住院摆药单入参。
 *
 * <p>按「病区 + 摆药日期」捞该病区在院患者的药品医嘱；admissionId 可选，传了只摆这一个患者。
 */
@Data
public class WardDispenseGenerateDTO implements Serializable {

    /**
     * 病区ID（必填）
     */
    @NotNull(message = "病区不能为空")
    private Long wardId;

    /**
     * 摆药日期（默认今天）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispenseDate;

    /**
     * 入院ID（可选：只摆指定患者）
     */
    private Long admissionId;
}
