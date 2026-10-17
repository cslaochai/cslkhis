package com.his.operation.vo;

import lombok.Data;

/**
 * 手术间聚合统计（Mapper 裸 SQL group by 的返回行，列名与字段逐字对齐）。
 */
@Data
public class RoomUtilizationStatVO {

    /** 手术间名称（group by operation_room） */
    private String roomName;

    /** 已排台数 */
    private Integer scheduledCount;

    /** 急诊台数 */
    private Integer emergencyCount;

    /** 占用分钟数（SUM 计划时长） */
    private Long occupiedMinutes;
}
