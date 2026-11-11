package com.his.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 患者端-检查预约占号入参。
 */
@Data
public class MiniExamBookDTO {

    /**
     * 就诊人ID（服务端校验绑定关系）
     */
    @NotNull(message = "就诊人不能为空")
    private Long patientId;

    /**
     * 检查申请单ID
     */
    @NotNull(message = "检查申请单不能为空")
    private Long applyId;

    /**
     * 设备ID（由可选时段接口返回）
     */
    @NotNull(message = "检查设备不能为空")
    private Long deviceId;

    /**
     * 检查日期
     */
    @NotNull(message = "检查日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate examDate;

    /**
     * 开始时间（HH:mm）
     */
    @NotBlank(message = "开始时段不能为空")
    private String startTime;
}
