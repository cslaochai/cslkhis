package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 人工补发评价入参（仍必须挂在一条随访任务上）。
 *
 * <p>不允许「无来源发放」：回收率的分母一旦可以是凭空创建的发放单，
 * 「发得多回收得少」就能靠多发把比率做上去，指标当场失去意义。
 */
@Data
public class SurveyDispatchIssueDTO implements Serializable {

    @NotNull(message = "随访任务ID不能为空")
    private Long followupTaskId;

    /**
     * 回收渠道:1-电话代填 2-短信 3-微信 4-现场扫码（默认 1）
     */
    private Integer channel;

    /**
     * 回收截止天数（默认 14 天）
     */
    private Integer expireDays;
}
