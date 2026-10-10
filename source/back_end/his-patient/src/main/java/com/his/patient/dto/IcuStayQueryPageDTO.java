package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * ICU 入出科台账分页入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IcuStayQueryPageDTO extends PageParam {

    /**
     * 入科单号
     */
    private String stayNo;

    /**
     * 患者姓名
     */
    private String patientName;

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

    /**
     * ICU 病区ID
     */
    private Long wardId;

    /**
     * 监护等级（1-特级 2-I级 3-II级）
     */
    private Integer careLevel;

    /**
     * 状态（1-在科 2-已出科）
     */
    private Integer status;
}
