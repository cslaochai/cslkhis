package com.his.iot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 冷链温湿度 GSP 记录查询。
 * <p>
 * GSP 附录 5 要求冷藏设备「自动记录、不可篡改、可按日导出」，所以导出粒度固定 5 分钟一轮
 * （每轮含平均值、最低、最高、记录点数），不按前端传入的间隔放宽——检查看的是记录密度是否达标。
 */
@Data
@Schema(description = "冷链温湿度记录查询")
public class IotColdChainRecordQueryDTO implements Serializable {

    @Schema(description = "冷柜编号", example = "FRIDGE-01")
    @NotBlank(message = "冷柜编号不能为空")
    private String deviceNo;

    @Schema(description = "开始时间", example = "2026-11-25 00:00:00")
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-11-25 23:59:59")
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
