package com.his.platform.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医疗组查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MedGroupQueryDTO extends PageParam {

    /**
     * 医疗组名称，模糊匹配
     */
    private String groupName;

    /**
     * 医疗组编码，模糊匹配
     */
    private String groupCode;

    /**
     * 所属科室ID
     */
    private Long deptId;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
