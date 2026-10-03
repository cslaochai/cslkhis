package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色信息出参
 */
@Data
public class RoleVO {

    /**
     * 角色ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色编码，唯一
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色类型：0-自定义角色 1-系统角色
     */
    private Integer roleType;

    /** 数据权限范围（1-全部数据 2-自定义数据 3-本部门数据 4-本部门及以下 5-仅本人数据） */
    private Integer dataScope;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;
}
