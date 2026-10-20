package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当班责任人摘要：需要把单据落到「此刻该值班的那个人」头上时用的最小字段。
 */
@Data
@NoArgsConstructor
public class DutyOfficerBriefVO {

    /**
     * 当班人员工ID；当班表没排到人时为 null
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 当班人姓名快照
     */
    private String employeeName;
}
