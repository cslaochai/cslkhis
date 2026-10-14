package com.mis.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 三大目录分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CatalogQueryDTO extends BizPageParam {

    private String category;
    private String payClass;
}
