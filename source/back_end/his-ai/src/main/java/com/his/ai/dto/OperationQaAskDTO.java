package com.his.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 运营问数入参。
 */
@Data
public class OperationQaAskDTO {

    @NotBlank(message = "请输入要问的问题")
    @Size(max = 200, message = "问题不能超过200字")
    private String question;

    /**
     * 是否让模型基于查询结果再生成一段简短结论（失败静默，表格照常返回）
     */
    private Boolean withSummary;
}
