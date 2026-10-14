package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 院内项目对照分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MappingQueryDTO extends BizPageParam {

    /** 已对照 true / 未对照 false；空为不限 */
    private Boolean mapped;
}
