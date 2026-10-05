package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作台卡片注册表分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkbenchWidgetQueryPageDTO extends PageParam {

    /**
     * 卡片编码/名称，模糊匹配
     */
    private String keyword;

    /**
     * 归属区域：todo/notice/entry/kpi/domain
     */
    private String area;

    /**
     * 状态（1-已上线可挂载 0-注册表先占位）
     */
    private Integer status;
}
