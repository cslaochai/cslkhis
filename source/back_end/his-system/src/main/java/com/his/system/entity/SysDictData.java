package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    /**
     * 字典类型
     */
    private String dictType;
    /**
     * 字典标签
     */
    private String dictLabel;
    /**
     * 字典值
     */
    private String dictValue;
    /**
     * 排序号
     */
    private Integer dictSort;

    private Integer dictSource;  // 1-系统级 2-自定义

    /**
     * 样式属性
     */
    private String dictClass;
    /**
     * 表格回显样式
     */
    private String listClass;

    /**
     * 是否默认（0-否 1-是）
     */
    private Integer isDefault;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
