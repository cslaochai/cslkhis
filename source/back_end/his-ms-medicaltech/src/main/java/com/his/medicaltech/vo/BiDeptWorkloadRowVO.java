package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 科室出院人次 TOP5 一行（BiMapper#deptWorkloadTop）。
 */
@Data
public class BiDeptWorkloadRowVO implements Serializable {

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 近 30 日出院人数
     */
    private Long count;
}
