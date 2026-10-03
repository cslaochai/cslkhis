package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/** 科室出院例数行（按科室生成快照时枚举科室用） */
@Data
public class DeptCountRowVO {

    /** 开单科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 开单科室名称（快照） */
    private String deptName;

    private Integer patientCount;
}
