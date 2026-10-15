package com.his.patient.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 线上问诊回复入参（接诊中 → 已完成，回复即结束）。
 */
@Data
public class OnlineReplyDTO implements Serializable {

    @NotNull(message = "问诊单ID不能为空")
    private Long id;

    /**
     * 医生回复
     */
    @NotBlank(message = "回复内容不能为空")
    private String reply;

    /**
     * 处置建议
     */
    private String advice;

    /**
     * 是否建议线下就诊（0-否 1-是）
     */
    @AllowedValues(values = {"0", "1"}, message = "是否建议线下就诊取值不合法")
    private Integer needVisit;
}
