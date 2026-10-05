package com.his.appoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 段级号源编辑入参（/schedule/slotUpsert）：一次提交一个排班的若干段。
 *
 * <p>为什么是整段列表而不是单段单发：号源是「Σ段 = 主表总量」的总量约束，
 * 逐段单发会让中途状态出现 Σ段 ≠ 主表的窗口；一次提交在事务内整批生效。
 * 允许只提交要改的段（部分编辑），未提交的段原样保留。
 */
@Data
public class ScheduleSlotUpsertDTO {

    /**
     * 排班ID
     */
    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;

    /**
     * 要编辑的段列表（按段ID定位）
     */
    @NotEmpty(message = "时间段列表不能为空")
    @Valid
    private List<ScheduleSlotItemUpsertDTO> slots;
}
