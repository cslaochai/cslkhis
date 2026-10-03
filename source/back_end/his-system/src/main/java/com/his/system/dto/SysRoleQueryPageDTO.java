package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleQueryPageDTO extends PageParam {

    /**
     * 角色名称，模糊匹配
     */
    private String roleName;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

}
