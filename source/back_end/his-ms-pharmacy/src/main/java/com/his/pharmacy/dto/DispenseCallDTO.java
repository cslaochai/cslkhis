package com.his.pharmacy.dto;

import lombok.Data;

/**
 * 取药叫号/过号入参
 */
@Data
public class DispenseCallDTO {

    /**
     * 发药窗口ID（叫号时必填）
     */
    private Long windowId;

    /**
     * 取药队列ID（重叫、过号时必填）
     */
    private Long queueId;

    /**
     * 过号原因
     */
    private String reason;
}
