package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工岗位实体：一行 = 一个人在一个科室以某个角色执业（角色 × 科室）。
 *
 * <p>见 sql/107-岗位模型.sql：改造前本表只有人-科室，角色另在旧员工角色表
 * （该镜像表已于 sql/118 删除，鉴权 join 直读本表），
 * 两张表自由组合能拼出「医生 · 药房」这种不存在的身份，顶栏切角色/切科室各切各的即源于此。
 *
 * <p>本表没有 del_flag（实体不带该列），MyBatis-Plus 的 delete 即物理删，
 * 不会留下占着 {@code uk_emp_role_dept} 的软删行。
 */
@Data
@TableName("sys_employee_post")
public class SysEmployeePost {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 角色ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 是否主科室（0-否 1-是）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer isPrimary;

    /**
     * 岗位生效日期（NULL=保存即生效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate effectiveDate;

    /**
     * 岗位失效日期（NULL=长期有效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate expireDate;
}
