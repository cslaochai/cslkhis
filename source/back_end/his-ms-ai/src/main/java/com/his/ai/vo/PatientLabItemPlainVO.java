package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 患者端报告解读 · 单项结果。
 */
@Data
@Schema(description = "患者端报告解读-单项结果")
public class PatientLabItemPlainVO {

    /** 检验项目名称（报告单上的原名） */
    @Schema(description = "检验项目名称（报告单上的原名）")
    private String itemName;

    /** 白话名；词典未收录时为 null */
    @Schema(description = "白话名，如「血色素」「坏胆固醇」；词典未收录时为 null")
    private String plainName;

    /** 结果值文本 */
    @Schema(description = "结果值")
    private String resultValue;

    /** 单位 */
    @Schema(description = "单位")
    private String unit;

    /** 参考范围 */
    @Schema(description = "参考区间")
    private String referenceRange;

    /**
     * 结果状态：1-正常 2-偏高 3-偏低 4-未判定 5-异常（方向不明确）
     * <p>
     * 「未判定」是独立的一档，不能并入正常：系统没能解析出参考区间，
     * 说它正常是凭空给了一个结论。
     */
    @Schema(description = "结果状态：1-正常 2-偏高 3-偏低 4-未判定 5-异常（方向不明确）")
    private Integer status;

    /** 状态文本 */
    @Schema(description = "状态文案")
    private String statusText;

    /** 这项检查查什么（来自白话词典） */
    @Schema(description = "这项查什么（来自词典）")
    private String whatIsIt;

    /** 结果含义的白话说明（偏高/偏低说明） */
    @Schema(description = "这项结果意味着什么（来自词典的偏高/偏低说明；词典未收录时给中性兜底）")
    private String plainText;

    /** 本项是否为危急值 */
    @Schema(description = "是否危急值")
    private Boolean critical;

    /** 危急值描述（仅危急值项有值） */
    @Schema(description = "危急值描述（仅 critical=true 时有值）")
    private String criticalDesc;

    /** 结果值升降箭头（↑/↓），正常与未判定为空 */
    @Schema(description = "结果值的升降箭头（↑/↓），正常与未判定为空")
    private String arrow;
}
