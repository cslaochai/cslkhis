package com.his.report.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * DRG 模拟 DTO。
 */
public class DrgSimDTO {

    /**
     * 单条模拟（首页主诊断为空时补传 icdCode/icdName）
     */
    @Data
    public static class Simulate {
        @NotNull(message = "病案首页不能为空")
        private Long summaryId;
        /**
         * 首页主诊断编码为空时必填
         */
        private String icdCode;
        private String icdName;
    }

    /**
     * 批量模拟
     */
    @Data
    public static class SimulateBatch {
        /**
         * 不传则模拟最近 50 条
         */
        private List<Long> summaryIds;
    }

    /**
     * 结果分页
     */
    @Data
    public static class ResultQuery {
        /**
         * 1已入组 2未入组
         */
        private Integer simStatus;
        /**
         * 关键字
         */
        private String keyword;
        /**
         * 页码
         */
        private Integer pageNum = 1;
        /**
         * 每页条数
         */
        private Integer pageSize = 10;
    }
}
