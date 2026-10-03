package com.his.emr.dto;

import lombok.Data;

/**
 * 开始随访入参
 */
@Data
public class FollowupStartDTO {

    /**
     * 随访任务ID
     */
    private Long id;

    /**
     * 执行人ID
     */
    private Long executorId;

    /**
     * 执行人姓名
     */
    private String executorName;

}
