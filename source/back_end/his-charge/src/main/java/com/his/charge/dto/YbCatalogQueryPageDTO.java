package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医保目录分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class YbCatalogQueryPageDTO extends PageParam {

    /**
     * 目录类型（1-西药/中成药 2-中药饮片 3-医疗服务项目 4-医用耗材；空=全部）
     */
    private Integer catalogType;

    /**
     * 关键字（编码/名称模糊）
     */
    private String keyword;

    /**
     * 状态（0-停用 1-启用；空=全部）
     */
    private Integer status;
}
