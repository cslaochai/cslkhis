package com.his.clinical.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 点评明细分页入参（batchId 为空 = 跨批次全量，供统计/导出；页面明细 tab 固定传批次）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RxReviewItemPageQueryDTO extends PageParam {

    /**
     * 批次ID（可空）
     */
    private Long batchId;

    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 开方医生姓名
     */
    private String doctorName;

    /**
     * 点评状态（0-待点评 1-已点评）
     */
    private Integer reviewStatus;

    /**
     * 点评结论（1-合理 2-不规范处方 3-用药不适宜处方 4-超常处方）
     */
    private Integer reviewResult;

    /**
     * 公示状态（0-未公示 1-已公示）
     */
    private Integer publicityStatus;

}
