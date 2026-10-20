package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 号源时段摘要：按时段叫号的科室把一条号源拆成若干时间区间后，跨模块要读的只有区间本身。
 */
@Data
@NoArgsConstructor
public class ClinicSourceSlotBriefVO {

    /**
     * 时段ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属号源ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 时段开始时间 HH:mm
     */
    private String startTime;

    /**
     * 时段结束时间 HH:mm
     */
    private String endTime;

    /**
     * 时段状态：1-正常，其余视为已停/作废
     */
    private Integer status;
}
