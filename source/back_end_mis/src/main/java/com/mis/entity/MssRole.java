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
 * 角色（仿 HIS sys_role）
 */
@Data
@TableName("mss_role")
public class MssRole implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String roleCode;
    private String roleName;
    /** 角色类型（1-系统角色 2-自定义角色） */
    private Integer roleType;
    /** 数据权限范围（1-全部数据 2-自定义数据 3-本部门数据 5-仅本人数据） */
    private Integer dataScope;
    private Integer sortOrder;
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
}
