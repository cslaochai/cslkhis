package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 医嘱基础字典
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysOrderDictData extends BaseEntity implements Serializable {

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

    /**
     * 表格回显样式
     */
    private String listClass;

    /**
     * 是否默认（0-否 1-是）
     */
    private Integer isDefault;

    /**
     * 1-启用 0-停用（停用后下拉不再出现，但历史医嘱里的值仍能渲染出文案）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 1-系统内置 2-自定义（医嘱字典页新增的都算自定义）
     */
    private Integer dictSource;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;
}
