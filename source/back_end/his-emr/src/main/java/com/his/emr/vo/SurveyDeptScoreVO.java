package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 满意度：科室短板 TOP10（百分制均分升序 —— 评审要的是短板榜，不是光荣榜）。
 */
@Data
public class SurveyDeptScoreVO implements Serializable {

    /**
     * 科室ID（无科室时 SQL 给 0）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名（含「未指定科室」「科室#id」两种兜底文案）
     */
    private String deptName;

    /**
     * 有效卷数
     */
    private Long cnt;

    /**
     * 百分制均分
     */
    private BigDecimal avgScore100;
}