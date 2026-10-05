package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 随访电话外呼结果回填入参
 */
@Data
public class FollowupCallResultDTO {

    /** 随访任务ID */
    @NotNull(message = "随访任务ID不能为空")
    private Long id;

    /** 是否接通：true-已接通（任务转随访中） false-未接通（可再次登记外呼） */
    @NotNull(message = "请选择是否接通")
    private Boolean connected;

    /** 外呼备注（未接通原因等，超长由服务端截断） */
    private String remark;
}
