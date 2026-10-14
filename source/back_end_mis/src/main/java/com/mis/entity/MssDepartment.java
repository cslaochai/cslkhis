package com.mis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 经办科室（仿 HIS sys_department，岗位 = 角色 × 科室 中的“科室”）
 */
@Data
@TableName("mss_department")
public class MssDepartment implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String deptCode;
    private String deptName;
    /** 科室类型（1-前台经办 2-审核稽核 3-基金财务 4-综合管理） */
    private Integer deptType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;
    private Integer sortOrder;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptLeaderId;
    private String phone;
    private String email;
    /** 状态（0-停用 1-启用） */
    private Integer status;

    private String createBy;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createById;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private String updateBy;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateById;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @TableLogic
    private Integer delFlag;
    private String remark;
    /** 子节点（树形返回用，非表字段） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private java.util.List<MssDepartment> children;
}
