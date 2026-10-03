package com.his.emr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 点评批次分页入参 */
@Data
public class RxReviewBatchQueryPageDTO {

    /** 批次名称/批次号模糊 */
    private String keyword;

    /** 批次状态（1进行中 2已完成） */
    @Min(value = 1, message = "批次状态非法")
    @Max(value = 2, message = "批次状态非法")
    private Integer status;

    /** 点评类型（1常规 2专项） */
    @Min(value = 1, message = "点评类型非法")
    @Max(value = 2, message = "点评类型非法")
    private Integer reviewType;

    /** 页码 */
    @Min(value = 1, message = "页码非法")
    private Integer pageNum = 1;

    /** 每页条数 */
    @Min(value = 1, message = "每页条数非法")
    private Integer pageSize = 10;
}
