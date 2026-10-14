package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户分页查询入参（兼容 HIS：body 传 userName/status/pageNum/pageSize）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryPageDTO extends BizPageParam implements Serializable {

    /** HIS 口径：用户名模糊查询 */
    private String userName;

    /** 状态（0-停用 1-启用 2-锁定） */
    private Integer status;
}
