package com.his.appoint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 排班号源查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleSelectQueryDTO extends PageParam {
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
}
