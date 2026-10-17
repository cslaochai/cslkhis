package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 耗材字典分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumableQueryPageDTO extends PageParam {
    /**
     * 关键字（名称/编码模糊）
     */
    private String keyword;
    /**
     * 类别（1-卫生材料 2-注射穿刺 3-医用敷料 4-防护用品 5-其他）
     */
    private Integer category;
    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
