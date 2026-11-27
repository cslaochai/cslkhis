package com.his.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 监护波形批量查询条件。大屏一轮轮询把本科室挂床的监护仪编号一次传入，
 * 后端按设备号从内存缓冲取最近波形窗，不传设备号则返回全部在缓冲中的设备。
 */
@Data
@Schema(description = "监护波形查询")
public class IotWaveformQueryDTO implements Serializable {

    @Schema(description = "设备编号清单，空=缓冲内全部设备")
    private List<String> deviceNos;
}
