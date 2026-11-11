package com.his.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者端-取消检查预约入参。
 */
@Data
public class MiniExamCancelDTO {

    /**
     * 就诊人ID（服务端校验绑定关系）
     */
    @NotNull(message = "就诊人不能为空")
    private Long patientId;

    /**
     * 预约单ID
     */
    @NotNull(message = "预约单不能为空")
    private Long apptId;

    /**
     * 取消原因（可空，服务端落「患者自助取消」）
     */
    private String reason;
}
