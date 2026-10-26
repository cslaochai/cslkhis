package com.his.platform.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 病区收治科室入参：一行 = 「某科室可以在本病区收治患者」。
 */
@Data
public class WardDeptQuotaDTO {

    /**
     * 科室ID
     */
    @NotNull(message = "收治科室不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 该科室在本病区的专科优先床数。不传 = 不限额（机动床，全院统筹）。
     */
    @Min(value = 0, message = "专科优先床数不能为负数")
    private Integer bedQuota;
}
