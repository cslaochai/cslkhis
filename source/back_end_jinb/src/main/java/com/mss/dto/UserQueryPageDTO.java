package com.mss.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryPageDTO extends BizPageParam implements Serializable {

    /** 状态（0-停用 1-启用 2-锁定） */
    private Integer status;
}
