package com.his.iot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import com.his.common.validation.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备事件分页查询（护士站告警列表）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设备事件分页查询")
public class IotEventPageQueryDTO extends PageParam implements Serializable {

    @Schema(description = "设备类型，空=全部", example = "coldchain")
    @AllowedValues(values = {"coldchain", "infusion", "asset", "env", "gas", "bed", "monitor",
            "dialysis", "cabinet", "uv", "robot"}, message = "设备类型取值不合法")
    private String deviceType;

    @Schema(description = "归属科室ID，空=全部；护士站只查本病区告警")
    private Long deptId;

    @Schema(description = "设备编号，空=全部。设备详情看单台的作业记录（任务单、等梯、签收）时用", example = "RBT-01")
    private String deviceNo;

    @Schema(description = "处理状态，空=全部", example = "0")
    @AllowedValues(values = {"0", "1", "2"}, message = "处理状态取值不合法")
    private Integer handledFlag;

    @Schema(description = "最低告警级别，空=全部。看板只要 level>=2（故障/严重），把每次开门这类 1 级作业记录排除掉",
            example = "2")
    @AllowedValues(values = {"1", "2", "3"}, message = "告警级别取值不合法")
    private Integer minLevel;

    @Schema(description = "开始时间（含）", example = "2026-11-25 08:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间（含）", example = "2026-11-25 18:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
