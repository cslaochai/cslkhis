package com.his.emr.dto;

import lombok.Data;

/**
 * 完成随访入参
 */
@Data
public class FollowupCompleteDTO {

    /**
     * 随访任务ID
     */
    private Long id;

    /**
     * 随访结果
     */
    private String result;

}
