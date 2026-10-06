package com.his.miniapp.dto;

import com.his.common.base.PageParam;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Data;

/**
 * 患者端报告查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportQueryPageDTO extends PageParam {

    /** 就诊人边界锚点（必填：不指定就诊人就无法判定能读谁的报告） */
    @NotNull(message = "patientId不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 1-检查 2-检验，空=全部 */
    private Integer reportType;

}
