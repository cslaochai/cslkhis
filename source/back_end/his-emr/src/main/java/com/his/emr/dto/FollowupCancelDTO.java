package com.his.emr.dto;

import lombok.Data;

/**
 * 取消随访入参
 */
@Data
public class FollowupCancelDTO {

    /**
     * 随访任务ID
     */
    private Long id;

    /**
     * 取消原因
     */
    private String reason;

}
