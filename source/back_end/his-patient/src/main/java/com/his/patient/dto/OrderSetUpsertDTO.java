package com.his.patient.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 医嘱组套模板新增/修改入参（命名遵循 AGENTS.md：新增和修改用 `xxxUpsertDTO`）。
 *
 * <p>与个人模板（{@link InpatientOrderTemplateUpsertDTO}）的差别只有 {@code scope}：
 * 明细类型、校验规则、先删后插的落库方式全部共用，避免两套口径漂移。
 *
 * <p>{@code doctorId} / {@code deptId} 都不放进 DTO —— 归属一律服务端按当前登录人/科室覆盖，
 * 否则改个字段就能把别人的组套挂到自己名下。
 */
@Data
public class OrderSetUpsertDTO implements Serializable {

    /**
     * 模板ID（为空=新增，有值=修改）
     */
    private Long id;

    /** 模板名称 */
    @NotBlank(message = "组套名称不能为空")
    @Size(max = 100, message = "组套名称不能超过 100 字")
    private String templateName;

    /**
     * 共享范围：1-个人 2-科室 3-全院（必填，服务端按它决定归属与可见范围）
     */
    @NotNull(message = "共享范围不能为空")
    private Integer scope;

    /**
     * 默认医嘱类型：1-长期 2-临时（缺省按临时）
     */
    private Integer orderType;

    /** 备注/适用场景说明 */
    @Size(max = 500, message = "备注不能超过 500 字")
    private String remark;

    /** 明细项集合 */
    @NotEmpty(message = "组套至少包含一条医嘱明细")
    @Valid
    private List<InpatientOrderItemDTO> items;
}
