package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 放射报告书写入参（保存草稿 / 提交审核共用，sql/138）。
 */
@Data
public class RadioReportUpsertDTO {

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
     * 使用的报告模板ID（可空；只是留痕，方便统计哪份模板被用得多）
     */
    private Long templateId;

    /**
     * 检查方法（如胸部CT平扫+三维重建）
     */
    @Size(max = 200, message = "检查方法不能超过 200 字")
    private String examMethod;

    /**
     * 报告内容
     */
    @Size(max = 4000, message = "影像所见不能超过 4000 字")
    private String reportContent;

    /**
     * 影像诊断 / 印象
     */
    @Size(max = 2000, message = "影像诊断不能超过 2000 字")
    private String conclusion;

    /**
     * 建议
     */
    @Size(max = 1000, message = "建议不能超过 1000 字")
    private String suggestions;

    /**
     * 阴阳性（字典 his_positive_flag：0-未判定 1-阴性 2-阳性 3-未见异常）
     */
    private Integer positiveFlag;

    /**
     * 是否危急（0-否 1-是）
     */
    private Integer isCritical;
}
