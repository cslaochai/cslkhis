package com.his.drgdip.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * DIP 模拟 DTO。
 */
public class DipSimDTO {

    /**
     * 单条模拟（首页主诊断/手术操作为空时补传 icdCode/icdName、operCode/operName）
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
        /**
         * 首页手术操作编码为空时补传（核心病种匹配依赖手术操作）
         */
        private String operCode;
        private String operName;
        /**
         * 病种库版本（2.0/3.0），不传默认 2.0
         */
        private String version;
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
        /**
         * 病种库版本（2.0/3.0），不传默认 2.0
         */
        private String version;
    }

    /**
     * 结果分页
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ResultQuery extends PageParam {
        /**
         * 1已入组 2未入组
         */
        private Integer simStatus;
        /**
         * 关键字
         */
        private String keyword;
        /**
         * 病种库版本（2.0/3.0），不传则查全部版本
         */
        private String version;
    }
}
