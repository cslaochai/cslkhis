package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 员工信息VO（包含科室信息）
 */
@Data
public class EmployeeVO {

    /**
     * 员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 员工编号（唯一）
     */
    private String empCode;

    /**
     * 员工姓名
     */
    private String empName;

    /**
     * 员工类型：1-医生 2-护士 3-收费员 4-药剂师 5-管理员 6-其他
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer empType;

    /**
     * 性别（性别字典口径：1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /**
     * 入职日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 职称 —— 字典职称字典的 dictValue（401 主任医师 / 301 副主任医师 …）。
     * 前端按该码去字典取文案，本字段本身是码不是中文（sql/174 起）。
     */
    private String title;

    /**
     * 职位 —— 字典 hospital_position 的 dictValue（1 临床科室主任 / 15 护士 …），同上是码不是中文。
     */
    private String position;

    /**
     * 专业特长
     */
    private String specialty;

    /**
     * 学历
     */
    private String education;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 是否专家：0-否 1-是
     */
    private Integer isExpert;

    /**
     * 专家号价格，单位：元
     */
    private BigDecimal expertPrice;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    /**
     * 主科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 主科室名称
     */
    private String deptName;

    /**
     * 科室ID列表（所有执业科室）
     */
    private List<String> deptIds;

    /**
     * 科室名称列表
     */
    private List<String> deptNames;

    /**
     * 角色编码列表（由岗位派生，只读回显）
     */
    private List<String> roleCodes;

    /**
     * 岗位列表（角色 × 科室）—— 授权的唯一来源，编辑表单据此配置
     */
    private List<EmployeePostVO> posts;
}
