package com.mis.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户出参（列表 + 详情：详情含员工字段与岗位行）
 */
@Data
public class UserVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String userName;
    private String realName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long empId;
    private String empName;
    private String empNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    private String deptName;
    /** 用户类型（1-院内用户 2-院外用户 3-患者 4-其他） */
    private Integer userType;
    /** 状态（0-停用 1-启用 2-锁定） */
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer loginCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private String remark;

    // ---------- 员工信息（详情回显用） ----------
    private Integer gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String idCard;
    private String phone;
    private String email;
    private String title;
    private String position;

    /** 岗位行（角色 × 科室） */
    private List<PostVO> posts;

    private List<String> roleCodes;
    private List<String> roleNames;
}
