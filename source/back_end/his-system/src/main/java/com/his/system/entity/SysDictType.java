package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
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

    /**
     * 1-系统级 2-自定义
     */
    private Integer dictSource;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

}
