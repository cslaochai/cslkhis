package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 输血反应上报入参。
 *
 * <p>反应可能在输注中、也可能在输注后数小时才发现，所以它<b>独立于流程状态</b>：
 * 只在"已完成"的单子上补登记（{@code has_reaction} 从 0 置 1），
 * <b>不回改历史状态</b> —— 把一条已完成的输血单改回"输注中"来记反应，
 * 就是在改历史，而且会破坏"完成=已回写病历"的一致性。
 *
 * <p>{@code reactionType} 走受控字典（{@code TransfusionLabels.reactionTypes()}），
 * 不接受自由文本：写成"发热"和"发热反应"两种，统计时永远凑不到一起。
 */
@Data
public class TransfusionReactionDTO implements Serializable {

    /** 输血申请单ID（必填） */
    @NotNull(message = "输血申请单ID不能为空")
    private Long applyId;

    /** 反应类型（必填，取自后端字典：发热反应/过敏反应/急性溶血反应/…/其他） */
    private String reactionType;

    /** 反应描述（必填：什么时候出现什么症状、生命体征变化） */
    @NotBlank(message = "反应描述不能为空（什么时候出现什么症状、生命体征怎么变的）")
    private String reactionDesc;

    /** 处理措施（必填：停药、给氧、用药、是否上报血库与医务科） */
    @NotBlank(message = "处理措施不能为空（停药/给氧/用药/是否上报血库与医务科）")
    private String reactionHandle;

    /** 备注 */
    private String remark;
}
