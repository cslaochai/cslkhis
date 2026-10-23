package com.his.outpatient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
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

    /**
     * 号别集合（逗号分隔，1-普通号 2-专家号 3-急诊号；不传 = 全部号别，与 listPage/statusCount 同口径）
     */
    @AllowedValues(values = {"1", "2", "3", "4"}, separator = ",", message = "号别取值不合法")
    private String registTypes;
}
