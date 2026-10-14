package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 违规追回分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecoverQueryDTO extends BizPageParam {

    /** 来源类型（1-疑点稽核 2-飞检） */
    private Integer sourceType;
    /** 状态（待追回/已追回/已核销） */
    private String status;
}
