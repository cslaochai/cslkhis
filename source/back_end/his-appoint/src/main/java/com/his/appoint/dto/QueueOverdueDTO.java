package com.his.appoint.dto;

import lombok.Data;

/**
 * 过号处理入参
 */
@Data
public class QueueOverdueDTO {
    /**
     * 队列ID
     */
    private Long id;
    /**
     * 过号原因
     */
    private String reason;
}
