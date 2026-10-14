package com.mis.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 飞检发现问题录入
 */
@Data
public class InspectionItemUpsertDTO implements Serializable {

    @NotNull(message = "任务id不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionId;

    /** 问题类型 */
    private String problemType;

    /** 问题描述 */
    private String problemDesc;

    /** 证据说明 */
    private String evidence;

    /** 涉及金额 */
    private BigDecimal involvedAmount;

    /** 处置方式（整改/追回/移交稽核） */
    private String dispose;
}
