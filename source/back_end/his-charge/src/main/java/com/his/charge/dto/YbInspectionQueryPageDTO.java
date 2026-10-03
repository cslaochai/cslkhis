package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 飞检/专项审核批次分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class YbInspectionQueryPageDTO extends PageParam {

    /**
     * 检查类型（1-国家飞检 2-省级飞检 3-智能审核转来 4-日常驻点审核）
     */
    private Integer inspectType;

    /**
     * 状态（字典 his_yb_inspect_status；空=全部）
     */
    private Integer status;

    /**
     * 关键字（批次号/检查组/统筹区/接待人模糊）
     */
    private String keyword;
}
