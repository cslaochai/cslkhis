package com.his.operation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 排台入参：手术室把申请单落到"哪个手术间 + 哪个时段 + 谁主刀"。
 */
@Data
public class OperationScheduleDTO implements Serializable {

    /**
     * 手术申请单ID（必填）
     */
    @NotNull(message = "手术申请单ID不能为空")
    private Long applyId;

    /**
     * 手术间（必填）
     */
    @NotBlank(message = "手术间不能为空")
    private String operationRoom;

    /**
     * 计划开始时间（必填）。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "计划开始/结束时间不能为空（没有时段的排台等于没排）")
    private LocalDateTime plannedStartTime;

    /**
     * 计划结束时间（必填，必须晚于开始时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "计划开始/结束时间不能为空（没有时段的排台等于没排）")
    private LocalDateTime plannedEndTime;

    /**
     * 主刀医师ID（必填，员工ID）
     */
    @NotNull(message = "主刀医师不能为空（排台的核心是定人）")
    private Long surgeonId;

    /**
     * 助手姓名（多人逗号分隔，可空）
     */
    private String assistantName;

    /**
     * 麻醉医师ID（可空 = 未指定）
     */
    private Long anesthetistId;

    /**
     * 排台备注
     */
    private String scheduleRemark;
}
