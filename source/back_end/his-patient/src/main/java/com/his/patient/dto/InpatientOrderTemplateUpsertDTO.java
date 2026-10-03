package com.his.patient.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 住院医嘱模板新增/修改入参（命名遵循 AGENTS.md：新增和修改用 `xxxUpsertDTO`）。
 *
 * <p>明细直接复用开立医嘱的 {@link InpatientOrderItemDTO}：模板就是"没填病人的医嘱半成品"，
 * 造第二个明细类型必然出现两套字段漂移。排序按提交顺序由服务端写排序号，前端不传。
 *
 * <p>{@code doctorId} 不放进来 —— 归属一律服务端按当前登录人覆盖，否则改个字段就能读写别人的模板。
 */
@Data
public class InpatientOrderTemplateUpsertDTO implements Serializable {

    /**
     * 模板ID（为空=新增，有值=修改）
     */
    private Long id;

    /** 模板名称 */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称不能超过 100 字")
    private String templateName;

    /**
     * 默认医嘱类型：1-长期 2-临时（缺省按临时）
     */
    private Integer orderType;

    /** 备注/适用场景说明 */
    @Size(max = 500, message = "备注不能超过 500 字")
    private String remark;

    /** 明细项集合 */
    @NotEmpty(message = "模板至少包含一条医嘱明细")
    @Valid
    private List<InpatientOrderItemDTO> items;
}
