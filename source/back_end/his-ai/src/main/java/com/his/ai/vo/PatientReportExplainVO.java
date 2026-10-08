package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 患者端报告解读结果。
 * <p>
 * <b>设计前提：这个 VO 的读者是患者本人，不是医生。</b>
 * 因此它没有 errorLevel、没有处置建议分级、没有「需尽快处理」这类判断 ——
 * 那些是医生站 lab_interpret 的职责。这里只回答三个问题：
 * 「我的报告里有多少项」「哪些项不在参考范围内」「这些项分别是查什么的」。
 * <p>
 * 模型只参与 {@code summary} 这一段的措辞；{@code items} 全部来自规则层与词典，
 * 模型输出不得修改。模型不可用时 {@code degraded=true}，其余字段照常返回 ——
 * 患者看到的是一份完整的、只是措辞更朴素的解读，而不是一句「服务不可用」。
 */
@Data
@Schema(description = "患者端报告解读结果")
public class PatientReportExplainVO {

    @Schema(description = "报告ID（字符串，避免前端 Number 丢精度）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;

    @Schema(description = "报告单号")
    private String reportNo;

    @Schema(description = "检验项目（大项，如「血常规」）")
    private String itemName;

    @Schema(description = "报告发布时间")
    private LocalDateTime reportTime;

    @Schema(description = "一句话总览，如「共 8 项，其中 2 项不在参考范围内」")
    private String summary;

    /**
     * 危急值置顶提示。
     * <p>
     * 这是整个 VO 里唯一允许「催促」的字段，因为它是代码判定的事实
     * （{@code LabCriticalValueRules}），不是模型的判断。
     */
    @Schema(description = "危急值提示；无危急值时为 null")
    private String criticalAlert;

    @Schema(description = "逐项解读")
    private List<PatientLabItemPlainVO> items;

    @Schema(description = "结果总项数")
    private Integer itemCount;

    @Schema(description = "不在参考范围内的项数（不含未判定）")
    private Integer abnormalCount;

    @Schema(description = "未能自动判定的项数（参考区间不可用，既不是异常也不是正常）")
    private Integer unjudgedCount;

    @Schema(description = "固定免责与引导提示")
    private String advice;

    /**
     * 表达层来源：rule-规则文案；model-模型润色已生效。
     * 前端据此决定是否展示「AI 生成」标识 —— 不能把规则文案说成 AI 说的，那是虚标。
     */
    @Schema(description = "表达层来源：rule-规则文案 model-模型润色")
    private String source;

    @Schema(description = "是否处于降级态（模型未参与，事实与白话照常返回）")
    private Boolean degraded;

    @Schema(description = "降级原因（密钥未配置 / 熔断中 / 调用失败）")
    private String degradeReason;
}
