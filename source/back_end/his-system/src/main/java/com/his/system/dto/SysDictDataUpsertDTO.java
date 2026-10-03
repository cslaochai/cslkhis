package com.his.system.dto;

import lombok.Data;

/**
 * 字典数据新增/修改入参
 */
@Data
public class SysDictDataUpsertDTO {

    /**
     * 字典数据ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 字典类型编码
     */
    private String dictType;

    /**
     * 字典标签（展示名称）
     */
    private String dictLabel;

    /**
     * 字典值（实际存储值）
     */
    private String dictValue;

    /**
     * 字典排序号，越小越靠前
     */
    private Integer dictSort;

    /**
     * 字典来源：1-系统级 2-自定义
     */
    private Integer dictSource;

    /**
     * 样式类
     */
    private String dictClass;

    /**
     * 列表样式类
     */
    private String listClass;

    /**
     * 是否默认值：0-否 1-是
     */
    private Integer isDefault;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;
}
