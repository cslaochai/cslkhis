package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 角色分页查询入参（HIS：POST /system/role/listPage body）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleListPageDTO extends BizPageParam implements Serializable {

    /** 角色名称模糊查询 */
    private String roleName;
}
