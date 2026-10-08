package com.his.medicaltech.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 输血完成入参：登记输注结束、实际输注量与疗效评估，并回写病历。
 */
@Data
public class TransfusionFinishDTO implements Serializable {

    /**
     * 输血申请单ID（必填）
     */
    @NotNull(message = "输血申请单ID不能为空")
    private Long applyId;

    /**
     * 输注结束时间（必填，必须晚于开始时间）。
     *
     * <p>{@code @JsonFormat} 见 {@link TransfusionStartDTO#getInfusionStartTime()} 的说明。
     */
    @NotNull(message = "输注结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime infusionEndTime;

    /**
     * 实际输注量（必填，>0）
     */
    private BigDecimal actualAmount;

    /**
     * 输注过程/输注后观察（必填：生命体征与有无不良反应）
     */
    @NotBlank(message = "输注过程观察不能为空（开始后 15 分钟是反应高发期，必须记录）")
    private String observation;

    /**
     * 输注后疗效评估（症状改善 + 复查指标）
     */
    private String efficacyEval;

    /**
     * 输血后血红蛋白 Hb（g/L）
     */
    private BigDecimal postHb;

    /**
     * 输血后红细胞压积 HCT（%）
     */
    private BigDecimal postHct;

    /**
     * 输血后血小板 PLT（×10^9/L）
     */
    private Integer postPlt;

    /**
     * 备注
     */
    private String remark;
}
