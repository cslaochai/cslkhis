package com.his.appoint.dto;

import lombok.Data;

/**
 * 叫下一位入参
 */
@Data
public class QueueCallNextDTO {
    /**
     * 科室ID
     */
    private Long deptId;
    /**
     * 医生ID
     */
    private Long doctorId;
    /**
     * 指定呼叫的队列ID（用于插队呼叫）
     */
    private Long queueId;
}
