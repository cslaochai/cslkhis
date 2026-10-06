package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 扣款通知单分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeductNoticeQueryPageDTO extends PageParam {

    /**
     * 状态（1-待确认 2-申诉中 3-申诉成功 4-维持扣款待缴 5-已缴回 6-已作废）
     */
    private Integer deductStatus;

    /**
     * 来源（1-飞检现场 2-智能审核/事后复核；空=全部）
     */
    private Integer sourceType;

    /**
     * 违规类型（字典 his_yb_violation_type；空=全部）
     */
    private Integer violationType;

    /**
     * 关联飞检批次ID（从飞检批次下钻扣款时用）
     */
    private Long inspectionId;

    /**
     * 只看超期未结（handle_deadline 早于今天且状态为待确认/申诉中）
     */
    private Boolean onlyOverdue;

    /**
     * 关键字（扣款单号/患者姓名/科室/违规描述模糊）
     */
    private String keyword;
}
