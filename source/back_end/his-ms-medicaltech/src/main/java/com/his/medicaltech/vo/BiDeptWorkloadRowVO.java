package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 科室出院人次 TOP5 一行（BiMapper#deptWorkloadTop）。
 */
@Data
public class BiDeptWorkloadRowVO implements Serializable {

    /**
     * 科室 ID（未分配科室为 null），供驾驶舱下钻到科室明细报表
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 近 30 日出院人数
     */
    private Long count;
}
