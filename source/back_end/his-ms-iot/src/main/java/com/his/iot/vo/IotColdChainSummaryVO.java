package com.his.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 冷链近 24 小时汇总：卡片上回答「这台柜过去一天稳不稳」，而不是只看当前一个点。
 * 字段与 Mapper SQL 别名逐字一致。
 */
@Data
@Schema(description = "冷链温湿度窗口汇总")
public class IotColdChainSummaryVO implements Serializable {

    @Schema(description = "设备编号")
    private String deviceNo;

    @Schema(description = "窗口最低温度（℃）")
    private Double minTemp;

    @Schema(description = "窗口最高温度（℃）")
    private Double maxTemp;

    @Schema(description = "归档窗口数（5 分钟一个）")
    private Integer windowCount;

    @Schema(description = "归档原始采样点数")
    private Long sampleCount;

    @Schema(description = "窗口内第一条记录时间")
    private LocalDateTime firstTs;

    @Schema(description = "窗口内最后一条记录时间")
    private LocalDateTime lastTs;
}
