package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 点评批次分页入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RxReviewBatchQueryPageDTO extends PageParam {

    /**
     * 批次名称/批次号模糊
     */
    private String keyword;

    /**
     * 批次状态（1进行中 2已完成）
     */
    private Integer status;

    /**
     * 点评类型（1常规 2专项）
     */
    private Integer reviewType;

}
