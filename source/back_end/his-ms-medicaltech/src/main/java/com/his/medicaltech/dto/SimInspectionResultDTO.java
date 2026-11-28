package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 仿真平台检查结果回传入参（外部回调）。
 *
 * <p>仿真平台延时完成检查后回调 HIS，把检查所见 / 结论 / 建议推回；HIS 落库为
 * 「已出结果 + 待审核报告」，不替诊断医师签名定稿。
 */
@Data
public class SimInspectionResultDTO {

    /** 检查记录 ID（biz_inspection_record.id）。 */
    @NotNull(message = "检查记录ID不能为空")
    private Long recordId;

    /** 检查所见（结果描述）。 */
    @NotBlank(message = "检查所见不能为空")
    private String resultDescription;

    /** 检查结论。 */
    private String resultConclusion;

    /** 处置建议 / 备注。 */
    private String suggestions;

    /** 阴阳性标志（0-未判定 1-阴性 2-阳性 3-未见异常）；空值按 3-未见异常落库。 */
    private Integer positiveFlag;

    /** 来源通道标识，留痕用（如 simlab）。 */
    private String source;
}
