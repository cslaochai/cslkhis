package com.his.workflow.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 我的待办分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowTaskPageQueryDTO extends PageParam implements Serializable {

    /** 流程名称/节点名称模糊匹配 */
    private String keyword;
}
