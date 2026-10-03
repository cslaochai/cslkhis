package com.his.appoint.dto;

import lombok.Data;

/**
 * 队列操作入参（签到/完成/过号等）
 */
@Data
public class QueueUpsertDTO {
    /**
     * 队列ID
     */
    private Long id;
}
