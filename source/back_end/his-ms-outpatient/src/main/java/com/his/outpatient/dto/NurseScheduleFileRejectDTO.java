package com.his.outpatient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 护理排班备案退回入参：把某护理单元某一周的已生效排班整批退回草稿并留退回原因。
 */
@Data
public class NurseScheduleFileRejectDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 排班单元类型（1-病区 2-门诊科室；空按 1-病区）
     */
    @AllowedValues(values = {"1", "2"}, message = "排班单元类型取值不合法")
    private Integer unitType;

    /**
     * 排班单元ID（unitType=1 取病区ID，=2 取门诊科室ID）
     */
    @NotNull(message = "请选择排班单元")
    private Long wardId;

    /**
     * 周起始日（空按本周，服务端归一到周一）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    /**
     * 退回原因；长度由服务端截到列宽
     */
    @NotBlank(message = "请填写退回原因")
    private String opinion;
}
