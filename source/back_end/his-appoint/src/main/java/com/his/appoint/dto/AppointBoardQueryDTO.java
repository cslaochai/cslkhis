package com.his.appoint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 预约看板查询入参
 */
@Data
public class AppointBoardQueryDTO {

    /**
     * 查询开始日期（就诊日，含）
     */
    @NotNull(message = "查询开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 查询结束日期（就诊日，含）
     */
    @NotNull(message = "查询结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 科室ID（不传 = 全部科室）
     */
    private Long deptId;

    /**
     * 医生ID
     */
    private Long doctorId;
}
