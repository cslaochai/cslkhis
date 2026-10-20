package com.his.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * 病区×班次交接班摘要（G-13）。
 */
@Data
public class WardHandoverVO {

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 班次文案
     */
    private String shiftText;

    /**
     * 时间窗文本（如 2026-10-04 16:00 ~ 24:00）
     */
    private String windowText;

    /**
     * 当前在院人数
     */
    private Integer inHospitalCount;

    /**
     * 窗内新入院（名称=床号 患者，诊断）
     */
    private List<String> newAdmissions;

    /**
     * 窗内出院人数
     */
    private Integer dischargeCount;

    /**
     * 体征越阈事件（代码按阈值筛，只报事实）
     */
    private List<String> abnormalEvents;

    /**
     * 窗内高/极高风险评估（风险等级 3/4）
     */
    private List<String> riskAssessments;

    /**
     * SBAR 式交班摘要草稿（模型或规则模板，护士终审）
     */
    private String summary;

    /**
     * 摘要来源：1-模型 2-规则模板
     */
    private Integer source;

    /**
     * 降级标志：模型环节失败时 true（事实列表照常返回）
     */
    private Boolean degraded;

    /**
     * 降级原因（说人话）
     */
    private String degradeReason;
}
