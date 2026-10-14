package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工分页查询入参（HIS 口径：GET /system/employee/listPage 走 query 绑定）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeQueryPageDTO extends BizPageParam {

    private String empName;
    /** 状态（0-停用 1-启用） */
    private Integer status;
}
