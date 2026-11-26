package com.his.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 冷链温湿度记录（5 分钟一轮）。字段与 SQL 别名逐字一致，Mapper 直接映射，不再中转 Map。
 */
@Data
@Schema(description = "冷链温湿度记录")
public class IotColdChainRecordVO implements Serializable {

    @Schema(description = "记录时间（5 分钟窗口起点）")
    private LocalDateTime ts;

    @Schema(description = "窗口平均温度（℃）")
    private Double temp;

    @Schema(description = "窗口最低温度（℃）")
    private Double tempMin;

    @Schema(description = "窗口最高温度（℃）")
    private Double tempMax;

    @Schema(description = "窗口平均湿度（%）")
    private Double humidity;

    @Schema(description = "窗口内上报点数，0 表示该时段设备未上报")
    private Integer pointCount;

    @Schema(description = "窗口内是否开过门（0-全程关闭 1-开过）")
    private Integer door;

    @Schema(description = "窗口内是否断过电（0-断过电 1-全程正常）")
    private Integer power;
}
