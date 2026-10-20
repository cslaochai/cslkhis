package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 此刻当班人员摘要：诊室没医生/护士应答时，把「该叫谁来顶这个班」交给叫号域。
 */
@Data
@NoArgsConstructor
public class DutyStaffBriefVO {

    /**
     * 当班人员工ID；该科室此刻没人排班时为 null
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long staffId;

    /**
     * 当班人姓名快照
     */
    private String staffName;
}
