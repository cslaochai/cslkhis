package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * ICU 监护记录分页入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IcuMonitorQueryPageDTO extends PageParam {

    /**
     * 入科记录ID
     */
    private Long stayId;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
