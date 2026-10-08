package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 药物相互作用知识新增/修改入参（id 为空=新增）
 */
@Data
public class DrugInteractionUpsertDTO {

    /**
     * 主键，新增时为空
     */
    private Long id;

    /**
     * 成分关键字A（书写顺序不承载语义，服务端会归一化出 pairKey）
     */
    @NotBlank(message = "成分A不能为空")
    private String componentA;

    /**
     * 成分关键字B
     */
    @NotBlank(message = "成分B不能为空")
    private String componentB;

    /**
     * 严重度（1-禁忌 2-慎用）
     */
    @NotNull(message = "请选择严重度")
    private Integer severity;

    /**
     * 相互作用后果（审方提示正文）
     */
    @NotBlank(message = "相互作用后果不能为空")
    private String interactionDesc;

    /**
     * 处理建议（换药/减量/监测什么指标）
     */
    private String suggestion;

    /**
     * 状态（1-启用 0-停用），为空按启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
