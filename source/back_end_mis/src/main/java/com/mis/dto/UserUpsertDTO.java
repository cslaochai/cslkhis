package com.mis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户新增/修改入参（HIS 口径：userUpsert）
 * - id 为空走新增：院内用户自动建员工（工号自动生成）、初始密码默认 123456
 * - id 非空走修改：密码不传则不变
 * - posts 非空：员工岗位整单替换（角色 × 科室，角色可按 roleCode 传）
 */
@Data
public class UserUpsertDTO implements Serializable {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String userName;

    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private Long empId;

    /** 用户类型（1-院内用户 2-院外用户 3-患者 4-其他） */
    private Integer userType;

    /** 状态（0-停用 1-启用 2-锁定） */
    private Integer status;

    private String remark;

    // ---------- 员工信息（院内用户时填写，MSS 员工表有列才落库） ----------
    private Integer gender;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String idCard;
    private String phone;
    private String email;
    private String title;
    private String position;

    /** 岗位（角色 × 科室）整单替换 */
    private List<PostItemDTO> posts;
}
