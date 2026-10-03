package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 医生接诊统计VO
 */
@Data
public class DoctorStatsVO {

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 已接诊完成数量
     */
    private Integer completedCount;

    /**
     * 当前候诊中数量
     */
    private Integer waitingCount;
}
