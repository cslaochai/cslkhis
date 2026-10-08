package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 患者端影像报告解读结果。读者是患者本人，不是医生。
 */
@Data
@Schema(description = "患者端影像报告解读结果")
public class PatientImagingExplainVO {

    @Schema(description = "报告ID（字符串，避免前端 Number 丢精度）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reportId;

    @Schema(description = "报告单号")
    private String reportNo;

    @Schema(description = "检查项目（报告项目名原文）")
    private String itemName;

    @Schema(description = "检查方法（如「胸部CT平扫+三维重建」，可能为空）")
    private String examMethod;

    @Schema(description = "报告发布时间")
    private LocalDateTime reportTime;

    /**
     * 代码事实：来自报告的阴阳性标记（PositiveFlagEnum），模型不得改写
     */
    @Schema(description = "阴阳性（未判定/阴性/阳性/未见异常），报告未判定时为 null")
    private String positiveText;

    /**
     * 代码事实：来自报告的危急值标记，唯一允许「催促」的字段
     */
    @Schema(description = "危急置顶提示；报告未标注危急时为 null")
    private String criticalAlert;

    /**
     * 词典层：影像检查白话词典命中即给，模型挂了也照常
     */
    @Schema(description = "这项检查是查什么的（白话）；词典未收录且模型不可用时为 null")
    private String examIntro;

    /**
     * 词典层：检查前后的注意，与 examIntro 同源
     */
    @Schema(description = "检查前后的注意事项（白话）；词典未命中时为 null")
    private String examNotice;

    @Schema(description = "报告「描述」部分的白话串讲；模型不可用时为 null（原文照常在报告页可见）")
    private String findingsPlain;

    @Schema(description = "报告「结论」部分的白话串讲；模型不可用时为 null")
    private String conclusionsPlain;

    @Schema(description = "报告「建议」部分的白话串讲；模型不可用时为 null")
    private String advicePlain;

    /**
     * 固定免责与引导：每份解读必带，是这个能力能上线的条件
     */
    @Schema(description = "固定免责与引导提示")
    private String advice;

    @Schema(description = "表达层来源：rule-词典与规则 model-模型串话已生效")
    private String source;

    @Schema(description = "是否处于降级态（模型未参与，白话串讲缺失，事实与引导照常返回）")
    private Boolean degraded;

    @Schema(description = "降级原因（密钥未配置 / 熔断中 / 调用失败 / 词典未收录等）")
    private String degradeReason;
}
