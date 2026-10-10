package com.his.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 技术授权审批入参
 */
@Data
public class TechAuthApproveDTO {

    @NotNull(message = "授权记录不能为空")
    private Long id;

    /**
     * 审批结论：true-通过（置已授权） false-驳回（置已驳回）
     */
    @NotNull(message = "审批结论不能为空")
    private Boolean approved;

    /**
     * 审批意见
     */
    private String approveOpinion;
}
