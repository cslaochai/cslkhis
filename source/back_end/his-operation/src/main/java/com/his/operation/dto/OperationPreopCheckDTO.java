package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 术前核对入参（三方核查的可核对部分）。
 *
 * <p>{@code checkItems} 是**码值集合**（逗号分隔，如 {@code 1,2,3,4}），不是自由文本。
 * 必核项（1-患者身份与手术部位 / 2-术式与知情同意 / 3-麻醉方式与麻醉同意 / 4-过敏史与术前用药）
 * 缺任何一项都直接拒绝 —— 见 {@code OperationCheckItems.REQUIRED}。
 *
 * <p>{@code preopNote} 用来写**异常项**（如"术中需备血 4U"）。核对全部正常时可以为空，
 * 但既然必核项已经结构化，"有没有异常"这件事不该靠备注文本猜。
 */
@Data
public class OperationPreopCheckDTO implements Serializable {

    /** 手术申请单ID（必填） */
    @NotNull(message = "手术申请单ID不能为空")
    private Long applyId;

    /** 术前核对要点码（逗号分隔，如 1,2,3,4；必填，且必须含 1/2/3/4） */
    private String checkItems;

    /** 术前核对补充说明（异常项写这里） */
    private String preopNote;

    /** 备注 */
    private String remark;
}
