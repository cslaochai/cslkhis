package com.his.emr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交点评结论入参。
 *
 * <p>结论 1-合理：问题码/意见清空；结论 2/3/4（不合理）：问题码与意见必填，
 * 且问题码分组必须与结论一致（2→11~15 不规范、3→21~27 不适宜、4→31~34 超常）。
 * 已公示的明细禁改（公示只增）。
 */
@Data
public class RxReviewItemUpsertDTO {

    /** 主键 */
    @NotNull(message = "点评明细不能为空")
    private Long id;

    /** 点评结论（1-合理 2-不规范处方 3-用药不适宜处方 4-超常处方） */
    @NotNull(message = "点评结论不能为空")
    @Min(value = 1, message = "点评结论非法")
    @Max(value = 4, message = "点评结论非法")
    private Integer reviewResult;

    /** 问题码（11-15不规范 21-27不适宜 31-34超常） */
    private List<String> problemTypes;

    /** 点评意见（不合理时必填） */
    private String reviewOpinion;
}
