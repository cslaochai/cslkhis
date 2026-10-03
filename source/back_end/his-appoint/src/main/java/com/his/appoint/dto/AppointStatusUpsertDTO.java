package com.his.appoint.dto;

import lombok.Data;

/**
 * 更新挂号状态入参
 */
@Data
public class AppointStatusUpsertDTO {
    /**
     * 挂号记录ID
     */
    private Long registId;
    /**
     * 挂号状态（1-已挂号 2-已签到 3-已接诊 4-已就诊 5-已退号 6-已过号）
     */
    private Integer status;
}
