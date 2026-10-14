package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 异地就医备案分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FilingQueryDTO extends BizPageParam {

    private String status;
}
