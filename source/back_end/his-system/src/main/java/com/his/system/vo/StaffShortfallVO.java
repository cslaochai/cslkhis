package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 人力缺口：某单元某班次某岗位的实际在岗低于配置标准的最少人数。
 */
@Data
public class StaffShortfallVO {

    /** 排班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /** 排班单元类型（1-科室 2-病区 3-全院） */
    private Integer orgType;

    /** 排班单元ID（全院级为 0） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /** 排班单元名称（标准行快照） */
    private String orgName;

    /** 标准班次ID（0-该单元全部班次） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /** 班次名称（0-全部班次） */
    private String shiftName;

    /** 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他） */
    private Integer staffType;

    /** 最低在岗人数 */
    private Integer minStaff;

    /** 实际在岗人次 */
    private Long actualCount;

    /** 缺口人数 */
    private Integer shortfall;
}
