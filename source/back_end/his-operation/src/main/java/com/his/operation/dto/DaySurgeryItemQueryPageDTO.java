package com.his.operation.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术准入目录分页查询入参。
 */
@Data
public class DaySurgeryItemQueryPageDTO implements Serializable {

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

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}
