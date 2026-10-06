package com.his.emr.dto;

import com.his.common.base.PageParam;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.time.LocalDate;

/**
 * 医师约谈分页入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RxReviewTalkQueryPageDTO extends PageParam {

    /**
     * 医师姓名/约谈编号模糊
     */
    private String keyword;

    /**
     * 约谈类型（1~5）
     */
    @Min(value = 1, message = "约谈类型非法")
    @Max(value = 5, message = "约谈类型非法")
    private Integer talkType;

    /**
     * 整改状态（1待整改 2已整改）
     */
    @Min(value = 1, message = "整改状态非法")
    @Max(value = 2, message = "整改状态非法")
    private Integer rectifyStatus;

    /**
     * 约谈时间起
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateStart;

    /**
     * 约谈时间止
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateEnd;

}
