package com.his.platform.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 病区查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WardQueryDTO extends PageParam {

    /**
     * 病区名称，模糊匹配
     */
    private String wardName;

    /**
     * 病区编码，模糊匹配
     */
    private String wardCode;

    /**
     * 科室ID：按「该科室可收治的病区」过滤（走 sys_ward_dept 多对多，含跨科收治的病区），
     * 不是只看本位科室。
     */
    private Long deptId;

    /**
     * 状态（0-停用 1-正常）
     */
    private Integer status;
}
