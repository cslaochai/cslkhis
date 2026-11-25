package com.his.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 一台设备的最新一行遥测。用于设备总览/护士站卡片的「当前状态」。
 */
@Data
@Schema(description = "设备最新遥测")
public class IotLatestVO implements Serializable {

    @Schema(description = "设备类型", example = "infusion")
    private String deviceType;

    @Schema(description = "设备类型中文名", example = "输液泵")
    private String deviceTypeLabel;

    @Schema(description = "设备编号", example = "PUMP-01")
    private String deviceNo;

    @Schema(description = "上报时间")
    private LocalDateTime ts;

    @Schema(description = "指标清单")
    private List<IotMetricVO> metrics;
}
