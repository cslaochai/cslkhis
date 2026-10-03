package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术完成入参（已安排 → 术后观察）。
 */
@Data
public class DaySurgeryFinishDTO implements Serializable {

    @NotNull(message = "登记单ID不能为空")
    private Long id;

    /** 手术结束时间 yyyy-MM-dd HH:mm:ss（不传取当前时间） */
    private String surgeryEndTime;
}
