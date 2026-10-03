package com.his.emr.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 临床路径模板分页查询入参（listPage 为 POST）。
 */
@Data
public class PathwayQueryPageDTO implements Serializable {

    /** 编码/名称/诊断模糊 */
    private String keyword;

    /** 状态（1-草稿 2-使用中 3-已停用） */
    private Integer status;

    /** 适用科室ID */
    private Long deptId;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}
