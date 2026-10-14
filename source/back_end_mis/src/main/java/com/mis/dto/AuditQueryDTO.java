package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 费用审核分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditQueryDTO extends BizPageParam {

    private String payClass;
    private String status;
}
