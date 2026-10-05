package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

/**
 * 医师约谈分页入参
 */
@Data
public class RxReviewTalkQueryPageDTO {

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

    /**
     * 页码
     */
    @Min(value = 1, message = "页码非法")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页条数非法")
    private Integer pageSize = 10;
}
