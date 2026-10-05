package com.his.emr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 医务处终审 DTO（定级必填）
 */
@Data
public class RecordQcFlowFinalDTO implements Serializable {

    /**
     * 流转单ID
     */
    @NotNull(message = "流转单ID不能为空")
    private Long flowId;

    /**
     * 终审定级（1-甲级 2-乙级 3-丙级）
     */
    @NotNull(message = "终审必须定级（1甲级/2乙级/3丙级）")
    @Min(value = 1, message = "定级码值非法")
    @Max(value = 3, message = "定级码值非法")
    private Integer grade;

    /**
     * 终审评分（0-100，可空）
     */
    @Min(value = 0, message = "评分范围 0-100")
    @Max(value = 100, message = "评分范围 0-100")
    private Integer finalScore;

    /**
     * 终审意见（可空）
     */
    private String finalOpinion;
}
