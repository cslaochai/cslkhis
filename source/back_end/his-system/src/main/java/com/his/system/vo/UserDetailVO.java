package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.his.system.vo.EmployeePostVO;

/**
 * 用户信息VO（包含员工信息）
 */
@Data
public class UserDetailVO {

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String userName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 主科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 主科室名称
     */
    private String deptName;

    /** 用户类型（1-系统用户 2-外部用户） */
    private Integer userType;

    /**
     * 启用状态（his_enable_status：0-禁用 1-启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createTime;

    /**
     * 头像地址（优先取用户头像，为空时回落到员工头像）
     */
    private String avatar;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 登录次数
     */
    private Integer loginCount;

    /**
     * 密码最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime passwordUpdateTime;

    /** 关联员工ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long empId;

    /**
     * 工号
     */
    private String empNo;

    /**
     * 员工类型（字典 emp_type）
     */
    private Integer empType;

    /**
     * 入职日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    /**
     * 性别（性别字典口径：1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 职称
     */
    private String title;

    /**
     * 学历
     */
    private String education;

    /**
     * 专业特长
     */
    private String specialty;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 职位
     */
    private String position;

    /**
     * 角色编码列表（由岗位镜像派生，只读回显用）
     */
    private List<String> roleCodes;

    /**
     * 科室ID列表（多科室，由岗位去重得到，只读回显用）
     */
    private List<String> deptIds;

    /**
     * 岗位列表（角色 × 科室）：编辑表单与顶栏档案的权威数据源
     */
    private List<EmployeePostVO> posts;
}
