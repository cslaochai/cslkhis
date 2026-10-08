package com.his.operation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 三方安全核查签单入参（一个时段一次提交，提交即生效、不可改）。
 */
@Data
public class SafetyCheckSignDTO implements Serializable {

    /**
     * 手术申请单ID（必填）
     */
    @NotNull(message = "手术申请单ID不能为空")
    private Long applyId;

    /**
     * 核查时段（必填）：1-麻醉诱导前 2-手术开始前 3-患者离开手术室前
     */
    @NotNull(message = "核查时段不能为空")
    private Integer phase;

    /**
     * 核查项码值（逗号分隔，如 1,2,3；必核项缺一不可）
     */
    @NotBlank(message = "核查项不能为空（逐项确认才是核对）")
    private String items;

    /**
     * 异常说明（发现风险/偏差必须写）
     */
    @Size(max = 1000, message = "异常说明过长")
    private String note;

    /**
     * 手术医师员工ID（必填）
     */
    @NotNull(message = "手术医师签名不能为空")
    private Long surgeonId;

    /**
     * 麻醉医师员工ID（必填）
     */
    @NotNull(message = "麻醉医师签名不能为空")
    private Long anesthetistId;

    /**
     * 手术室护士员工ID（必填）
     */
    @NotNull(message = "手术室护士签名不能为空")
    private Long nurseId;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注过长")
    private String remark;
}
