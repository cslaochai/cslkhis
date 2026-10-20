package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 科室快照（就诊锚点带出的科室 ID + 科室名）。
 */
@Data
public class DeptSnapshotVO implements Serializable {

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称 */
    private String deptName;
}