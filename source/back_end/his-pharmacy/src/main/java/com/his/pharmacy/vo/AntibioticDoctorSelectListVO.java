package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/** 医师下拉（抗菌药物处方权授权选人用） */
@Data
public class AntibioticDoctorSelectListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 医师姓名（快照） */
    private String doctorName;

    /** 科室ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称（快照） */
    private String deptName;

    /** 职称 */
    private String title;
}
