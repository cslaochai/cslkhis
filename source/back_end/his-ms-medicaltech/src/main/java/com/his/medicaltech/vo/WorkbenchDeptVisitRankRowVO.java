package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台排行卡的一行（WorkbenchMetricMapper#deptVisitRank 一行）。
 */
@Data
public class WorkbenchDeptVisitRankRowVO implements Serializable {

    /**
     * 科室名称（快照为空时为「未知科室」）
     */
    private String deptName;

    /**
     * 今日就诊数
     */
    private Long cnt;
}