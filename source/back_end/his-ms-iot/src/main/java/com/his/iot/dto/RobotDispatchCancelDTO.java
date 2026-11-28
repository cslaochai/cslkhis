package com.his.iot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 取消配送任务单。
 */
@Data
public class RobotDispatchCancelDTO implements Serializable {

    @NotNull(message = "任务单不能为空")
    private Long id;

    private String cancelReason;
}
