package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 慢特病病种目录查询（分页与下拉候选共用）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChronicCatalogQueryPageDTO extends PageParam {

    /**
     * 类别（1-慢性病 2-特殊病；空=全部）
     */
    private Integer diseaseType;

    /**
     * 启用状态（0-停用 1-启用；空=全部）
     */
    private Integer status;

    /**
     * 关键字（编码/名称/ICD 模糊）
     */
    private String keyword;
}
