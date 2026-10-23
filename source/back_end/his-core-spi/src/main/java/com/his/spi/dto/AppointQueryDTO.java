package com.his.spi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import com.his.common.validation.AllowedValues;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 挂号查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppointQueryDTO extends PageParam {

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 就诊日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 挂号状态（1-已挂号 2-已签到 3-已接诊 4-已就诊 5-已退号 6-已过号）
     */
    private Integer registStatus;

    /**
     * 号别集合（逗号分隔，1-普通号 2-专家号 3-急诊号；不传 = 全部号别，由调用场景声明自己管哪些号）
     */
    @AllowedValues(values = {"1", "2", "3", "4"}, separator = ",", message = "号别取值不合法")
    private String registTypes;

    /**
     * 查询开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /**
     * 查询结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
