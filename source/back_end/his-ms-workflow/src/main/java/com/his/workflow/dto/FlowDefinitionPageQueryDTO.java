package com.his.workflow.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 流程定义分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowDefinitionPageQueryDTO extends PageParam implements Serializable {

    /** 流程编码/名称模糊匹配 */
    private String keyword;

    /** 状态（0-草稿 1-已发布 2-已停用），空 = 全部 */
    private Integer status;
}
