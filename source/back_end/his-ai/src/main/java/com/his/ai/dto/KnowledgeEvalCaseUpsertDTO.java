package com.his.ai.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 评测用例新增/修改（id 为空新增，非空修改）。
 */
@Data
public class KnowledgeEvalCaseUpsertDTO {

    /** 修改时必传（雪花ID字符串） */
    private Long id;

    @NotBlank(message = "评测问题不能为空")
    private String question;

    /** 期望命中文档ID，分号分隔 */
    @NotBlank(message = "期望命中文档不能为空")
    private String expectedDocIds;

    /** 期望命中文档标题，分号分隔（展示用，可随选择自动带出） */
    private String expectedDocTitles;

    /** 备注 */
    private String remark;
}
