package com.his.appoint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 段级号源编辑单行入参（/schedule/slotUpsert 的 slots 元素）。
 *
 * <p>段的时间窗（seq/startTime/endTime）不接受编辑：段边界是挂号快照
 * （slot_start/slot_end）的语义来源，改窗等于篡改已挂号的时段事实。
 */
@Data
public class ScheduleSlotItemUpsertDTO {

    /**
     * 段ID（biz_schedule_slot 的行，必须属于本次提交的排班）
     */
    @NotNull(message = "时间段ID不能为空")
    private Long id;

    /**
     * 段号源总数（不得小于该段已挂号数）
     */
    @NotNull(message = "号源数量不能为空")
    @Min(value = 0, message = "号源数量不能为负")
    private Integer totalSource;

    /**
     * 段内线上预约预留（0=未划池；不得小于该段预约已用，不得大于段号源总数）
     */
    @Min(value = 0, message = "预约号源数不能为负")
    private Integer appointmentSource;

    /**
     * 段状态（0-停用 1-正常）：停用段不可再挂，已挂的不受影响
     */
    @NotNull(message = "段状态不能为空")
    @Min(value = 0, message = "段状态不合法")
    @Max(value = 1, message = "段状态不合法")
    private Integer status;
}
