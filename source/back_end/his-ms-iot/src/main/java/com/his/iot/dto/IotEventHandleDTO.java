package com.his.iot.dto;

import com.his.common.validation.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备事件处理（确认处理 / 忽略），处理人与处理时间由服务端落。
 */
@Data
@Schema(description = "设备事件处理入参")
public class IotEventHandleDTO implements Serializable {

    @Schema(description = "事件ID", example = "2104148440035360770")
    @NotNull(message = "事件ID不能为空")
    private Long id;

    @Schema(description = "处理结果（1-已处理 2-已忽略）", example = "1")
    @NotNull(message = "处理结果不能为空")
    @AllowedValues(values = {"1", "2"}, message = "处理结果取值不合法")
    private Integer handledFlag;

    @Schema(description = "处理说明", example = "已确认婴儿回床，腕带完好")
    private String remark;
}
