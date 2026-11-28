package com.his.iot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 调度派车入参：robotNo 为空时由系统自动选空闲机器人。
 */
@Data
public class RobotDispatchAssignDTO implements Serializable {

    @NotNull(message = "任务单不能为空")
    private Long id;

    /** 指定机器人编号；空为自动选车 */
    private String robotNo;
}
