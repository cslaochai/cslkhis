package com.his.schedule.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 派岗候选行：某天确实上了班、可挪到门诊公共工位的人。
 */
@Data
public class OpdAssignCandidateVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 员工ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /** 员工工号 */
    private String empCode;

    /** 员工姓名 */
    private String employeeName;

    /** 岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他 7-护工） */
    private Integer staffType;

    /** 当天排班所属单元名称（病区或门诊科室，用来认人） */
    private String orgName;
}
