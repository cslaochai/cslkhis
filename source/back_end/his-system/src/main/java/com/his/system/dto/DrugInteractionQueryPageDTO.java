package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药物相互作用知识分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DrugInteractionQueryPageDTO extends PageParam {

    /**
     * 成分关键字（同时模糊匹配 componentA / componentB）
     */
    private String component;

    /**
     * 后果或建议正文关键字（模糊匹配）
     */
    private String keyword;

    /**
     * 严重度过滤（1-禁忌 2-慎用），NULL=全部
     */
    private Integer severity;

    /**
     * 状态过滤（1-启用 0-停用），NULL=全部
     */
    private Integer status;
}
