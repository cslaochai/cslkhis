package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台排行卡的一行（{@code WorkbenchMetricMapper#deptVisitRank} 一行）。
 *
 * <p>字段名是前端契约：{@code DeptRankWidget.vue} 按 {@code item.deptName} 与
 * {@code item.cnt}（同时用于条长归一化与右侧数字）渲染。
 *
 * <p>科室名取快照列而非 join 科室表：科室改名/撤销后历史挂号仍记旧名，
 * 这样排行榜与历史数据对得上（代价是同一科室若曾改名会拆成两行）。
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