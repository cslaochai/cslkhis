package com.his.pharmacy.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * I 类切口预防用药点评入参。
 */
@Data
public class IncisionReviewUpsertDTO {

    /**
     * 非空=改；为空=按 operationApplyId 新建
     */
    private Long id;

    /**
     * 手术申请单ID
     */
    @NotNull(message = "手术申请单ID不能为空")
    private Long operationApplyId;

    /**
     * 预防用药药品ID（未用药则不传）
     */
    private Long drugId;

    /**
     * 是否有预防用药指征（0-无 1-有）
     */
    @AllowedValues(values = {"0", "1"}, message = "指征标志取值不合法")
    private Integer indicationFlag = 0;

    /**
     * 给药时机（1~6）
     */
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6"}, message = "给药时机取值不合法")
    private Integer timingType;

    /**
     * 预防用药总时长（小时）
     */
    @Min(value = 0, message = "疗程非法")
    @Max(value = 720, message = "疗程上限 720 小时")
    private Integer courseHours;

    /**
     * 是否联合用药（0-否 1-是）
     */
    @AllowedValues(values = {"0", "1"}, message = "联合用药标志取值不合法")
    private Integer comboFlag = 0;

    /**
     * 联合用药理由
     */
    private String comboReason;

    /**
     * 特殊使用级是否有抗菌药物管理工作组会诊同意（0-无 1-有）
     */
    @AllowedValues(values = {"0", "1"}, message = "会诊标志取值不合法")
    private Integer consultFlag = 0;

    /**
     * 点评结论（1-合理 2-不合理）
     */
    @NotNull(message = "点评结论不能为空")
    @AllowedValues(values = {"1", "2"}, message = "点评结论取值不合法")
    private Integer reviewResult;

    /**
     * 问题码（数组，元素 41~48；结论=2 必填）
     */
    @AllowedValues(values = {"41", "42", "43", "44", "45", "46", "47", "48"}, message = "问题码非法（允许 41~48）")
    private List<String> problemTypes;

    /**
     * 点评意见（结论=2 必填）
     */
    @Size(max = 500, message = "点评意见超长")
    private String reviewOpinion;

    /**
     * 备注
     */
    private String remark;
}
