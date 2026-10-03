package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检查项目查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysInspectionItemQueryPageDTO extends PageParam {

    /**
     * 关键字（匹配项目名称或编码），模糊匹配
     */
    private String keyword;

    /** 项目类型（1-放射检查 2-超声检查 3-心电图 4-内镜检查 5-其他） */
    private Integer itemType;

    /**
     * 返回条数上限（下拉搜索时使用）
     */
    private Integer limit;
}
