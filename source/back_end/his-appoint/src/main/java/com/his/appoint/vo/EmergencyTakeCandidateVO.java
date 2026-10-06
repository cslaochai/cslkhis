package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 接班人候选（交班弹框的下拉数据）
 */
@Data
public class EmergencyTakeCandidateVO {

    /**
     * 员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long empId;

    /**
     * 员工姓名
     */
    private String empName;

    /**
     * 所属科室名称
     */
    private String deptName;

    /**
     * 候选来源文案：当前在岗（有排班且此刻在班）/ 本科室（在职员工）
     */
    private String sourceText;
}
