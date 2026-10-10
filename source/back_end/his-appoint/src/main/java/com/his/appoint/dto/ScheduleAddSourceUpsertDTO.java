package com.his.appoint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 排班加号入参（专家临时加号：号源总数与剩余同步增加，added_source 留痕）
 */
@Data
public class ScheduleAddSourceUpsertDTO {

    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;

    @NotNull(message = "加号数不能为空")
    @Min(value = 1, message = "加号数至少为1")
    @Max(value = 50, message = "单次加号不能超过50")
    private Integer addNum;

    /**
     * 加号原因（必填，写入排班 remark 留痕）
     */
    @NotBlank(message = "加号原因不能为空")
    private String reason;
}
