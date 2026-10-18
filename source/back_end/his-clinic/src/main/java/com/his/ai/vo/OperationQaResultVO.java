package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 运营问数结果：表格式数据 + 实际执行的 SQL（透明可查）+ 降级说明。
 */
@Data
@Schema(description = "AI 运营问数结果")
public class OperationQaResultVO {

    /** 用户原始问题 */
    @Schema(description = "原始问题")
    private String question;

    /** 查询内容概括 */
    @Schema(description = "查询内容概括（模型生成）")
    private String title;

    /** 实际执行的查询语句（已过安全闸门） */
    @Schema(description = "实际执行的 SELECT 语句，已过安全闸门")
    private String sql;

    /** 结果列定义 */
    @Schema(description = "列定义")
    private List<OperationColumnVO> columns = new ArrayList<>();

    /** 结果数据行 */
    @Schema(description = "数据行，cells 与 columns 按下标对应")
    private List<OperationRowVO> rows = new ArrayList<>();

    /** 返回行数 */
    @Schema(description = "返回行数")
    private int rowCount;

    /** 结果是否因超过行数上限被截断 */
    @Schema(description = "结果是否超过 100 行被截断")
    private boolean truncated;

    /** 查询耗时（毫秒） */
    @Schema(description = "耗时（毫秒）")
    private long elapsedMs;

    /** 是否降级（降级时页面必须展示原因） */
    @Schema(description = "是否降级（true 时页面必须展示降级原因）")
    private boolean degraded;

    /** 降级原因，未降级时为空 */
    @Schema(description = "降级原因，未降级时为空")
    private String degradeReason;

    /** 基于结果表生成的简短结论（仅要求带总结时返回） */
    @Schema(description = "模型基于结果表生成的简短结论，withSummary=true 时返回")
    private String summary;
}
