package com.his.operation.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 日间手术准入目录分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DaySurgeryItemQueryPageDTO extends PageParam implements Serializable {

    /**
     * 编码/名称模糊
     */
    private String keyword;

    /**
     * 适用科室ID
     */
    private Long deptId;

    /**
     * 是否仅启用
     */
    private Boolean enabledOnly;
}