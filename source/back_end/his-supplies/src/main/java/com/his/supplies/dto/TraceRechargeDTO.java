package com.his.supplies.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 高值耗材计费失败补记入参（锚点/单价用台账上的快照，不重新采集）
 */
@Data
public class TraceRechargeDTO {
    /** 台账ID */
    @NotNull(message = "缺少台账ID")
    private Long traceId;
}
