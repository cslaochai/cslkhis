package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验项目查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysLaboratoryItemQueryPageDTO extends PageParam {

    /**
     * 关键字（匹配项目名称或编码），模糊匹配
     */
    private String keyword;

    /**
     * 项目类型（1-血液检验 2-尿液检验 3-生化检验 4-免疫检验 5-微生物检验 6-其他）
     */
    private Integer itemType;

    /**
     * 返回条数上限（下拉搜索时使用）
     */
    private Integer limit;
}
