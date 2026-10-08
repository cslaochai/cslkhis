package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 班次下拉出参：选班次时要认名字，排班表单还要用它带出上下班时间。
 *
 * <p>跨度分钟数、是否夜班、休息时长门槛、迟到宽限这些是班次自身的规定，
 * 由后端在排班校验时现读，下拉里带出去没有消费方。
 */
@Data
@Schema(name = "ShiftSelectListVO", description = "班次下拉出参")
public class ShiftSelectListVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 开始时间（HH:mm）
     */
    private String startTime;

    /**
     * 结束时间（HH:mm）
     */
    private String endTime;
}
