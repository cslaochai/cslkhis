package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 员工
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_employee")
public class SysEmployee extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 员工编号（唯一）
     */
    private String empCode;

    /**
     * 员工姓名
     */
    private String empName;

    /**
     * 员工类型（1-医生 2-护士 3-收费员 4-药剂师 5-管理员 6-其他）
     */
    private Integer empType;

    /**
     * 性别（性别字典口径：1-男 2-女 9-未知；原 0女1男员工口径已于 2026-09-23 迁移，见 sql/75）
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
     * 主科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 主科室名称
     */
    private String deptName;

    /**
     * 职称 —— 存字典职称字典的 dictValue，不是中文
     * （101 医士 … 201 主治（主管）医师 … 301 副主任医师 … 401 主任医师；501+ 非卫技系列）。
     * 2026-09-28 sql/174 已把存量中文全量迁成码值，列注释同步改为字典口径。
     * 判定职称层级的码值集合单点在 EmpTitleCode，别再写 like '主任%'。
     */
    private String title;

    /**
     * 职位 —— 存字典 hospital_position 的 dictValue，不是中文
     * （1 临床科室主任 … 15 护士 … 31 临床医师 … 45 病案与编码岗）。
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
     * 是否专家（0-否 1-是）
     */
    private Integer isExpert;

    /**
     * 专家号价格
     */
    private BigDecimal expertPrice;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
