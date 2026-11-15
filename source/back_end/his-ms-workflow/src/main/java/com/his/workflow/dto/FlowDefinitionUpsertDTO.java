package com.his.workflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 流程定义新增/修改（整图提交：元信息 + 节点 + 流转线一次落库）。
 */
@Data
public class FlowDefinitionUpsertDTO implements Serializable {

    /** 为空 = 新增，有值 = 修改 */
    private Long id;

    @NotBlank(message = "流程编码不能为空")
    private String flowCode;

    @NotBlank(message = "流程名称不能为空")
    private String flowName;

    /** 流程边界说明，意图路由时给模型看 */
    private String flowDesc;

    @NotEmpty(message = "节点不能为空")
    @Valid
    private List<FlowNodeUpsertDTO> nodes;

    @NotEmpty(message = "流转线不能为空")
    @Valid
    private List<FlowSkipUpsertDTO> skips;
}
