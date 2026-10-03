package com.his.system.dto;

import lombok.Data;

/**
 * 检验项目明细新增/修改入参
 */
@Data
public class SysLaboratoryItemDetailUpsertDTO {

    /**
     * 明细ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 检验大项目ID
     */
    private Long laboratoryItemId;

    /**
     * 明细项目编码
     */
    private String itemCode;

    /**
     * 明细项目名称
     */
    private String itemName;

    /**
     * 单位
     */
    private String unit;

    /**
     * 参考范围
     */
    private String referenceRange;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
