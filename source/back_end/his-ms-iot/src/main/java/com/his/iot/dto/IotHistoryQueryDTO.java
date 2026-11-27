package com.his.iot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 单台设备的历史遥测查询。
 * <p>
 * 时间入参必须声明 {@code @JsonFormat}：Jackson 默认只认带 T 的 ISO 串，前端若传
 * {@code 2026-11-25 10:00:00} 会直接 400 且日志里没有业务线索。
 */
@Data
@Schema(description = "设备历史遥测查询")
public class IotHistoryQueryDTO implements Serializable {

    @Schema(description = "设备类型", example = "infusion")
    @NotBlank(message = "设备类型不能为空")
    @AllowedValues(values = {"coldchain", "infusion", "asset", "env", "gas", "bed",
            "dialysis", "cabinet", "uv", "robot", "monitor"}, message = "设备类型取值不合法")
    private String deviceType;

    @Schema(description = "设备编号", example = "PUMP-01")
    @NotBlank(message = "设备编号不能为空")
    private String deviceNo;

    @Schema(description = "开始时间", example = "2026-11-25 09:00:00")
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-11-25 10:00:00")
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "最多返回行数，默认 2000", example = "2000")
    @Min(value = 1, message = "返回行数至少 1")
    @Max(value = 5000, message = "返回行数最多 5000")
    private Integer limit = 2000;

    @Schema(description = "聚合间隔（分钟），空=原始点。看 24 小时曲线必须传（5 秒一条一天 1.7 万点，原始点会被 limit 截断）",
            example = "5")
    @Min(value = 1, message = "聚合间隔至少 1 分钟")
    @Max(value = 60, message = "聚合间隔最多 60 分钟")
    private Integer intervalMinutes;
}
