package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术随访登记入参（出院 / 转住院后 24h 内必访一次，可多次）。
 */
@Data
public class DaySurgeryFollowDTO implements Serializable {

    /**
     * 主键ID
     */
    @NotNull(message = "登记单ID不能为空")
    private Long id;

    /**
     * 随访方式（1-电话 2-门诊 3-上门 4-线上）
     */
    @NotNull(message = "随访方式不能为空")
    private Integer followType;

    /**
     * 随访结果（1-无异常 2-有异常已处置 3-有异常再就诊 4-失联）
     */
    @NotNull(message = "随访结果不能为空")
    private Integer result;

    /**
     * 随访内容
     */
    private String content;
}
