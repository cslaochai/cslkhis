package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ICU 出科登记入参（去向必填，转归说明在死亡/自动离院时必填）
 */
@Data
public class IcuStayOutDTO {

    @NotNull(message = "入科记录不能为空")
    private Long id;

    /**
     * 出科时间
     */
    @NotNull(message = "出科时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outTime;

    /**
     * 转出去向（1-普通病房 2-专科病房 3-手术室 4-转院 5-死亡 6-自动离院）
     */
    @NotNull(message = "转出去向不能为空")
    private Integer outDest;

    /**
     * 出科情况/转归说明
     */
    private String outReason;

    /**
     * 出科 GCS（3~15）
     */
    private Integer outGcs;
}
