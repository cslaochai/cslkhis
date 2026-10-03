package com.his.system.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.his.system.dto.EmployeePostDTO;

/**
 * 用户新增/编辑DTO
 */
@Data
public class SysUserUpsertDTO {

    /**
     * 用户ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String userName;

    /**
     * 真实姓名
     */
    private String realName;

    /** 用户类型（1-系统用户 2-外部用户） */
    private Integer userType;

    /**
     * 启用状态（his_enable_status：0-禁用 1-启用）
     */
    private Integer status;

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
     * 岗位列表（角色 × 科室）：执业科室与角色的唯一配置入口。
     *
     * <p>不再单独接收 deptIds / roleCodes / deptId —— 那三份是同一件事的四个说法，
     * 各自能配出一份互不对应的数据，拼出「医生 · 药房」这种不存在的身份。
     * 主科室由后端取主岗位所在科室回填，见 {@code SysUserServiceImpl#syncPrimaryDept}。
     */
    @Valid
    private List<EmployeePostDTO> posts;

    /**
     * 职称
     */
    private String title;

    /**
     * 职位
     */
    private String position;

    /**
     * 专业特长
     */
    private String specialty;

    /**
     * 学历（字典 his_education：1博士 2硕士 3本科 4大专 5中专）
     */
    private String education;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 是否专家：0-否 1-是
     */
    private Integer isExpert;

    /**
     * 专家号价格，单位：元
     */
    private BigDecimal expertPrice;
}
