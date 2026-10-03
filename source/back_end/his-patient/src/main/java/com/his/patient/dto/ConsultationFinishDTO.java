package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会诊完成入参：给出结论并回写住院病历。
 *
 * <p>结论不可为空（= 医嘱"执行必须有记录"）：「会诊已完成」而没有结论，
 * 在病历上等于什么都没发生，四核对里"病历有医嘱没记"这一类缺陷反而会多出来。
 */
@Data
public class ConsultationFinishDTO implements Serializable {

    /**
     * 会诊ID（必填）
     */
    @NotNull(message = "会诊ID不能为空")
    private Long consultationId;

    /**
     * 会诊结论（必填）
     */
    @NotBlank(message = "会诊结论不能为空（「已完成」而没有结论，病历上等于什么都没发生）")
    private String conclusion;

    /**
     * 会诊时间（为空 = 当前时间）
     */
    private LocalDateTime consultTime;

    /**
     * 备注
     */
    private String remark;
}
