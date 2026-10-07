package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医嘱基础字典
 */
@Data
@TableName("sys_dict_data")
public class SysOrderDictData implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;
}
