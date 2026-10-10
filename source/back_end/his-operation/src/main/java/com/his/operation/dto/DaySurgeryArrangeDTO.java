package com.his.operation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术安排入参（评估通过 → 已安排）。
 */
@Data
public class DaySurgeryArrangeDTO implements Serializable {

    @NotNull(message = "登记单ID不能为空")
    private Long id;

    /**
     * 手术开始时间 yyyy-MM-dd HH:mm:ss
     */
    @NotBlank(message = "手术时间不能为空")
    private String surgeryTime;

    /**
     * 手术间
     */
    @NotBlank(message = "手术间不能为空")
    private String operatingRoom;

    /**
     * 台次
     */
    private Integer seqNo;

    /**
     * 实际麻醉方式
     */
    @NotNull(message = "麻醉方式不能为空")
    private Integer anesthesiaType;

    /**
     * 主刀医生姓名
     */
    @NotBlank(message = "主刀医生不能为空")
    private String surgeon;
}
