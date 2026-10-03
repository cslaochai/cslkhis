package com.his.system.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 价格查询入参（跨价表统一查询）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PriceQueryPageDTO extends PageParam {

    /**
     * 项目类型：DRUG-药品 CONSUMABLE-耗材 INSPECTION-检查 LABORATORY-检验 TREATMENT-治疗
     */
    @NotBlank(message = "项目类型不能为空")
    private String itemType;

    /**
     * 关键字，按项目编码或名称模糊匹配
     */
    private String keyword;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
