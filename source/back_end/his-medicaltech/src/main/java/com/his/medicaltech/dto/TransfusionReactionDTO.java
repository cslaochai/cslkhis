package com.his.medicaltech.dto;

import com.his.common.validation.InEnum;
import com.his.medicaltech.enums.TransfusionReactionTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 输血反应上报入参。
 */
@Data
public class TransfusionReactionDTO implements Serializable {

    /**
     * 输血申请单ID（必填）
     */
    @NotNull(message = "输血申请单ID不能为空")
    private Long applyId;

    /**
     * 反应类型（必填，取自后端字典：发热反应/过敏反应/急性溶血反应/…/其他）
     */
    @NotBlank(message = "反应类型不能为空（须从受控词表选择，不接受自由文本）")
    @InEnum(value = TransfusionReactionTypeEnum.class, type = InEnum.Type.TEXT,
            message = "输血反应类型不合法（须从受控词表选择：发热反应/过敏反应/急性溶血反应/迟发性溶血反应/细菌污染反应/循环超负荷/输血相关急性肺损伤/输血相关移植物抗宿主病/其他）")
    private String reactionType;

    /**
     * 反应描述（必填：什么时候出现什么症状、生命体征变化）
     */
    @NotBlank(message = "反应描述不能为空（什么时候出现什么症状、生命体征怎么变的）")
    private String reactionDesc;

    /**
     * 处理措施（必填：停药、给氧、用药、是否上报血库与医务科）
     */
    @NotBlank(message = "处理措施不能为空（停药/给氧/用药/是否上报血库与医务科）")
    private String reactionHandle;

    /**
     * 备注
     */
    private String remark;
}
