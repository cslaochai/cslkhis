package com.his.emr.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 点评明细分页入参（batchId 为空 = 跨批次全量，供统计/导出；页面明细 tab 固定传批次）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RxReviewItemPageDTO extends PageParam {

    /**
     * 批次ID（可空）
     */
    private Long batchId;

    /**
     * 处方号（快照）
     */
    private String prescriptionNo;

    /**
     * 开方医生姓名（快照）
     */
    private String doctorName;

    /**
     * 点评状态（0-待点评 1-已点评）
     */
    @Min(value = 0, message = "点评状态非法")
    @Max(value = 1, message = "点评状态非法")
    private Integer reviewStatus;

    /**
     * 点评结论（1-合理 2-不规范处方 3-用药不适宜处方 4-超常处方）
     */
    @Min(value = 1, message = "点评结论非法")
    @Max(value = 4, message = "点评结论非法")
    private Integer reviewResult;

    /**
     * 公示状态（0-未公示 1-已公示）
     */
    @Min(value = 0, message = "公示状态非法")
    @Max(value = 1, message = "公示状态非法")
    private Integer publicityStatus;

}
