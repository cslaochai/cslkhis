package com.his.appoint.dto;

import lombok.Data;

/**
 * 队列统计查询入参
 */
@Data
public class QueueStatsQueryDTO {
    /**
     * 排队状态（2-候诊中 3-就诊中 4-已就诊 5-已退号 6-已过号 7-已失效）
     */
    private Integer queueStatus;
}
