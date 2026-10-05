package com.his.system.dto;

import lombok.Data;

/**
 * 字典类型新增/修改入参
 */
@Data
public class SysDictTypeUpsertDTO {

    /**
     * 字典类型ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 字典类型（唯一）
     */
    private String dictType;

    /**
     * 字典类型名称
     */
    private String dictName;

    /**
     * 字典来源：1-系统级 2-自定义
     */
    private Integer dictSource;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;
}
