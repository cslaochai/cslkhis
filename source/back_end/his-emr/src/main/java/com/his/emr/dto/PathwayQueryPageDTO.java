package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 临床路径模板分页查询入参（listPage 为 POST）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PathwayQueryPageDTO extends PageParam implements Serializable {

    /**
     * 编码/名称/诊断模糊
     */
    private String keyword;

    /**
     * 状态（1-草稿 2-使用中 3-已停用）
     */
    private Integer status;

    /**
     * 适用科室ID
     */
    private Long deptId;
}