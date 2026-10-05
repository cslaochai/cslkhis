package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.ai.support.DeteriorationScoreRules;
import lombok.Data;

import java.util.List;

/**
 * 危重预警单患者明细（G-12）。
 * <p>评分与预警级是代码事实；{@code advice} 是模型观察建议（仅 alertLevel ≥1 才调模型），
 * 模型不可用时 {@code degraded=true}、{@code advice=null}，评分与预警级照常可见。</p>
 */
@Data
public class DeteriorationExplainVO {

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者姓名 */
    private String patientName;

    /** 床号 */
    private String bedNo;

    /** 病区名称 */
    private String wardName;

    /** 测量时间（yyyy-MM-dd HH:mm:ss） */
    private String measureTime;

    /** 分项明细 */
    private List<DeteriorationScoreRules.ScoreItem> items;

    /** MEWS+SpO2 总分（-1 = 近窗无体征数据） */
    private Integer totalScore;

    /** 预警级：0-未触发 1-关注 2-高危 */
    private Integer alertLevel;

    /** 预警级文案 */
    private String alertText;

    /** 触发的分项事实文本（提示词与前端共用口径） */
    private List<String> triggeredFacts;

    /** 模型观察建议（未达阈值或模型不可用时为 null） */
    private String advice;

    /** 降级标志：模型环节失败时 true（评分照常返回） */
    private Boolean degraded;

    /** 降级原因（说人话） */
    private String degradeReason;
}
