package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 按已安排床位办理入院入参
 */
@Data
public class BedWaitAdmitDTO {

    @NotNull(message = "排队记录不能为空")
    private Long waitId;

    /**
     * 入院医生ID
     */
    @NotNull(message = "入院医生不能为空")
    private Long admitDoctorId;

    /**
     * 入院途径：1-门诊 2-急诊 3-转院 4-其他（无证登记时必填）
     */
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "入院途径取值不合法")
    private Integer admitWay;

    /**
     * 入院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admitTime;

    /**
     * 备注
     */
    private String remark;
}
