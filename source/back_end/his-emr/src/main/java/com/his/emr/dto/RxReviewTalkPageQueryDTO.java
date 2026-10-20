package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 医师约谈分页入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RxReviewTalkPageQueryDTO extends PageParam {

    /**
     * 医师姓名/约谈编号模糊
     */
    private String keyword;

    /**
     * 约谈类型（1~5：1-首次约谈 2-警告约谈 3-限制处方权 4-取消处方权 5-恢复处方权）
     */
    private Integer talkType;

    /**
     * 整改状态（1待整改 2已整改）
     */
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
