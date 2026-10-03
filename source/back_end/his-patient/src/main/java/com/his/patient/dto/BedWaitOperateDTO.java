package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 等床队列动作入参（退回队列 / 取消排队共用）
 *
 * <p><b>原因不能在这里加 @NotBlank</b>：取消排队必须有原因 ——
 * 患者去了别的医院、还是转门诊随访了，这两件事对床位周转率的解释完全不同；
 * 而退回队列的原因由服务端兜一个默认值（床位被占着却没人来住是全院最贵的浪费之一，
 * "谁把它放掉的"比"为什么"更需要留痕）。同一个字段在两个动作上必填口径不同，
 * 判级留在 service 里按动作分别处理。
 */
@Data
public class BedWaitOperateDTO {

    @NotNull(message = "排队记录不能为空")
    private Long waitId;

    /** 原因 */
    private String reason;
}
