package com.his.medicaltech.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 心电报告书写入参（保存草稿 / 提交审核共用，sql/173）。
 */
@Data
public class EcgReportUpsertDTO {

    /**
     * 已有报告ID（改稿时传；新增为空）
     */
    private Long reportId;

    /**
     * 检查记录ID（必填，报告的锚点）
     */
    @NotNull(message = "缺少检查记录")
    private Long recordId;

    /**
     * 使用的报告模板ID（可空；留痕）
     */
    private Long templateId;

    /**
     * 心电图所见
     */
    @Size(max = 4000, message = "心电图所见不能超过 4000 字")
    private String reportContent;

    /**
     * 心电图诊断
     */
    @Size(max = 2000, message = "心电图诊断不能超过 2000 字")
    private String conclusion;

    /**
     * 建议
     */
    @Size(max = 1000, message = "建议不能超过 1000 字")
    private String suggestions;

    /**
     * 阴阳性（字典 his_positive_flag：0-未判定 1-阴性 2-阳性 3-未见异常）
     */
    @NotNull(message = "阴阳性不能为空")
    @AllowedValues(values = {"0", "1", "2", "3"}, message = "阴阳性取值不合法")
    private Integer positiveFlag;

    /**
     * 是否危急（0-否 1-是）
     */
    @NotNull(message = "是否危急不能为空")
    @AllowedValues(values = {"0", "1"}, message = "是否危急取值不合法")
    private Integer isCritical;

    /**
     * 签名 PIN（软签二次确认）：签名人本人输入，明文不入库
     */
    private String pin;
}
