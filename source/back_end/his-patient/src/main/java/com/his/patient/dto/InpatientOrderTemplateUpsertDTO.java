package com.his.patient.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 住院医嘱模板新增/修改入参（命名遵循 AGENTS.md：新增和修改用 `xxxUpsertDTO`）。
 */
@Data
public class InpatientOrderTemplateUpsertDTO implements Serializable {

    /**
     * 模板ID（为空=新增，有值=修改）
     */
    private Long id;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称不能超过 100 字")
    private String templateName;

    /**
     * 医嘱类型：1-长期 2-临时（必填，长期/临时决定计费与执行计划，不允许后端兜底）
     */
    @NotNull(message = "请选择医嘱类型（长期 / 临时）")
    @AllowedValues(values = {"1", "2"}, message = "医嘱类型取值不合法")
    private Integer orderType;

    /**
     * 备注/适用场景说明
     */
    @Size(max = 500, message = "备注不能超过 500 字")
    private String remark;

    /**
     * 明细项集合
     */
    @NotEmpty(message = "模板至少包含一条医嘱明细")
    @Valid
    private List<InpatientOrderItemDTO> items;
}
