package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 科室
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class SysDepartment extends BaseEntity {
    /** 备注 */
    private String remark;


    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;



    /**
     * 科室编码（唯一）
     */
    private String deptCode;
    /**
     * 科室名称
     */
    private String deptName;
    /**
     * 科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他），多个类型逗号分隔
     */
    private String deptType;

    /**
     * 父科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 科室图标
     */
    private String deptIcon;
    /**
     * 科室描述
     */
    private String deptDesc;
    /**
     * 联系电话
     */
    private String contactPhone;
    /**
     * 科室位置
     */
    private String location;

    /**
     * 科室负责人（sys_employee.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptLeaderId;

    /**
     * 是否开诊（0-否 1-是）
     */
    private Integer isOpen;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    @TableField(exist = false)
    private List<SysDepartment> children;
}
