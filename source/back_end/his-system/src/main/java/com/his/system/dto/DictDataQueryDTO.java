package com.his.system.dto;

import lombok.Data;

/**
 * 字典数据查询入参
 */
@Data
public class DictDataQueryDTO {

    /**
     * 字典类型编码，多个用英文逗号分隔
     */
    private String dictTypes;
}
