package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 输液闭环动作 DTO（开始 / 巡视 / 结束三动作共用一形）。
 */
@Data
public class InfusionActionDTO {

    /** 执行行ID（医嘱执行记录的ID） */
    @NotNull(message = "执行行ID不能为空")
    private Long execId;

    /** 滴速（滴/分）：开始与巡视用 */
    private Integer dripRate;

    /** 余量（ml）：巡视用 */
    private Integer remainingVolume;

    /** 巡视时间（空 = 当前时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime roundTime;

    /** 不良反应：0-无 1-有（结束用） */
    private Integer adverseFlag;

    /** 不良反应描述（adverseFlag=1 必填） */
    private String adverseNote;

    /** 备注（巡视：穿刺部位/局部情况等） */
    private String remark;
}
