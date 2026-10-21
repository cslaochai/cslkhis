package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 白话词典覆盖率自检。
 */
@Data
@Schema(description = "白话词典覆盖率自检")
public class LabPlainCoverageVO {

    /** 出现过的检验项目名总数（去重） */
    @Schema(description = "库内出现过的检验项目名总数（去重）")
    private Integer totalItemName;

    /** 词典已收录的项目数 */
    @Schema(description = "词典已收录数")
    private Integer coveredCount;

    /** 词典未收录的项目数 */
    @Schema(description = "未收录数")
    private Integer missingCount;

    /** 收录覆盖率（百分数，0~100） */
    @Schema(description = "覆盖率（0~100，整数）")
    private Integer coverageRate;

    /** 词典现有分组清单 */
    @Schema(description = "词典现有分组清单（给筛选下拉用）")
    private List<String> groupNames;

    /** 未收录项目清单，按出现次数倒序 */
    @Schema(description = "未收录清单，按出现次数倒序——先补高频的")
    private List<MissingItemVO> missingList;

    @Data
    @Schema(description = "未收录的检验项目")
    public static class MissingItemVO {

        @Schema(description = "检验项目名")
        private String itemName;

        @Schema(description = "在检验结果里出现的次数，越多越该先补")
        private Integer refCount;

        @Schema(description = "词典里是否存在同名但已停用的条目（存在则改启用即可，不用新增）")
        private Boolean disabledOnly;
    }
}
