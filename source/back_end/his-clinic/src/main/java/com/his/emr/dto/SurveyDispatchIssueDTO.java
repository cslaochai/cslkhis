package com.his.emr.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 人工补发评价入参（仍必须挂在一条随访任务上）。
 */
@Data
public class SurveyDispatchIssueDTO implements Serializable {

    @NotNull(message = "随访任务ID不能为空")
    private Long followupTaskId;

    /**
     * 回收渠道:1-电话代填 2-短信 3-微信 4-现场扫码（默认 1）
     */
    @AllowedValues(values = {"1", "2", "3", "4"},
            message = "回收渠道不合法")
    private Integer channel;

    /**
     * 回收截止天数（默认 14 天）
     */
    private Integer expireDays;
}
