package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserQueryPageDTO extends PageParam {

    /**
     * 用户名（登录账号），模糊匹配
     */
    private String userName;

    /**
     * 科室ID，关联科室表
     */
    private Long deptId;

    /**
     * 启用状态（his_enable_status：0-禁用 1-启用）
     */
    private Integer status;
}
