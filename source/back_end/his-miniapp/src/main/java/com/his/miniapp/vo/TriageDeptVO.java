package com.his.miniapp.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 智能导诊推荐科室。
 */
@Data
public class TriageDeptVO implements Serializable {

    /** 科室ID（字符串化防 BIGINT 精度丢失） */
    private String deptId;

    /** 科室名称 */
    private String deptName;

    /** 命中的症状名称（告诉患者「凭什么推荐这个科」） */
    private String symptomName;

    /** 急症信号（0-否 1-是）：1 时前端必须置顶并加红提示 */
    private Integer urgent;

    /** 就诊提示 */
    private String advice;

    /** 推荐权重 */
    private Integer weight;

    /**
     * 该科室当前可约号源数（-1 表示没查到，前端不显示）。
     *
     * <p>导诊的终点是挂上号：推荐了一个近七天根本没号的科室，患者点进去只会看到空白，
     * 闭环就断在这里。数字由排班域现算，不是快照。
     */
    private Integer bookableCount;
}
