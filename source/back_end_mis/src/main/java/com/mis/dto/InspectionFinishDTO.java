package com.mis.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 飞检任务办结（填写结果小结）
 */
@Data
public class InspectionFinishDTO implements Serializable {

    @NotNull(message = "任务id不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 检查结果小结 */
    private String resultSummary;
}
