package com.his.emergency.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 急诊交班台账行（出参）
 */
@Data
public class EmergencyHandoverVO {

    /**
     * 交班单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 交班单号
     */
    private String handoverNo;

    /**
     * 交班科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 交班科室名称（快照）
     */
    private String deptName;

    /**
     * 交出人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromEmpId;

    /**
     * 交出人姓名（快照）
     */
    private String fromEmpName;

    /**
     * 接班人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long takeEmpId;

    /**
     * 接班人姓名
     */
    private String takeEmpName;

    /**
     * 班次名（交出人当日在岗排班的班次，查不到为空）
     */
    private String shiftName;

    /**
     * 本班区间起
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodBegin;

    /**
     * 本班区间止（= 交班时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;

    /**
     * 本次移交未闭环人数（定格）
     */
    private Integer pendingCount;

    /**
     * 其中交班前无人指派的条数（定格，= 本次清零掉的池子行数）
     */
    private Integer poolCount;

    /**
     * 其中候诊已超时的条数（定格）
     */
    private Integer overdueCount;

    /**
     * 其中留观中的条数（定格）
     */
    private Integer observationCount;

    /**
     * 其中留观已超时限的条数（定格）
     */
    private Integer obsOverLimitCount;

    /**
     * 整单交代备注
     */
    private String remark;

    /**
     * 建单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
