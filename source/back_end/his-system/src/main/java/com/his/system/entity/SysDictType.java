package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

    /**
     * 字典类型（唯一）
     */
    private String dictType;
    /**
     * 字典名称
     */
    private String dictName;
    private Integer dictSource;  // 1-系统级 2-自定义
    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
