package com.his.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 员工新增/编辑DTO
 */
@Data
public class EmployeeUpsertDTO {

    /**
     * 员工ID，新增时为空，修改时必填
     */
    private Long id;

    /**
     * 员工编号（唯一），新增时由系统自动生成，可不传
     */
    private String empCode;

    /**
     * 员工姓名
     */
    private String empName;

    /**
     * 员工类型：1-医生 2-护士 3-收费员 4-药剂师 5-管理员 6-其他
     */
    private Integer empType;

    /**
     * 性别（性别字典口径：1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 入职日期
     */
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
     * 职称 —— 字典职称字典的 dictValue（如 401 主任医师 / 203 主管护师 / 501 科员），
     * 不接中文。页面下拉直接取自字典数据，新增码值不用改前后端。
     */
    private String title;

    /**
     * 职位 —— 字典 hospital_position 的 dictValue（如 1 临床科室主任 / 15 护士 / 31 临床医师），
     * 不接中文。
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
     * 入职日期（整型，兼容历史数据）
     */
    private Integer entryDate;

    /**
     * 岗位列表（角色 × 科室），一行即「以某角色在某科室执业」。
     *
     * <p>取代改造前的 {@code deptIds}（执业科室）+ {@code roleCodes}（角色）两个独立多选 ——
     * 分开存就能被拼出「药剂师·骨科」这种没分配过的身份。主科室也不再单独传，
     * 它是主岗位（is_primary=1）落下来的派生值。
     *
     * <p>null 与空集合语义不同：null = 本次不带岗位字段（如名册只切换启用状态），原岗位不动；
     * 空集合 = 显式清空。
     */
    @Valid
    private List<EmployeePostDTO> posts;
}
