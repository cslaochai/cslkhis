package com.his.ai.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 同一患者某项检验指标的历史趋势。
 */
@Data
public class LabTrendVO {

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 单位
     */
    private String unit;

    /**
     * 历史结果点（按时间升序）
     */
    private List<Point> points = new ArrayList<>();

    /**
     * 方向：升高 / 降低 / 持平 / 波动
     */
    private String direction;

    /**
     * 变化描述，如 5.6 → 6.8 → 7.9（持续升高）
     */
    private String changeText;

    /**
     * 变化幅度描述，如较首次升高 41%
     */
    private String magnitudeText;

    @Data
    public static class Point {

        /**
         * 检验记录号
         */
        private String recordNo;

        /**
         * 就诊/报告日期
         */
        private String date;

        /**
         * 结果值原文
         */
        private String resultValue;

        /**
         * 数值（无法解析时为 null，该点会被排除出趋势计算）
         */
        private Double numericValue;

        /**
         * 异常标志
         */
        private Integer abnormalFlag;
    }
}
