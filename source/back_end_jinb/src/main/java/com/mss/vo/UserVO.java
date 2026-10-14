package com.mss.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表出参（含员工/科室/角色回显）
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
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    private String deptName;
    /** 用户类型（1-系统用户 2-外部用户） */
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

    private List<String> roleCodes;
    private List<String> roleNames;
}
