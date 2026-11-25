package com.his.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史遥测的一个时间点，一行多个指标。前端按指标拆成多条曲线即可。
 */
@Data
@Schema(description = "历史遥测点")
public class IotHistoryPointVO implements Serializable {

    @Schema(description = "上报时间")
    private LocalDateTime ts;

    @Schema(description = "该时间点的指标清单")
    private List<IotMetricVO> metrics;
}
