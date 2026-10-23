package com.his.platform.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * 员工
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_employee")
public class SysEmployee extends BaseEntity {

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
     * 职称 —— 存字典职称字典的 dictValue
     */
    private String title;

    /**
     * 职位 —— 存字典 hospital_position
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
     * 签名笔迹（base64 PNG，结诊处方与检查检验报告审核的视觉落款引用）
     */
    private String signImage;

    /**
     * 是否专家（0-否 1-是）
     */
    private Integer isExpert;

    /**
     * 本机构注册执业（0-否 1-是 NULL-未登记）。
     * 底线：非本机构注册执业人员不得单独值班（《医疗质量安全核心制度要点》2018 8号）。
     * 排班校验：急诊夜班值班人 is_registered=0 拦截；NULL 放行（档案未维护不误伤排班）。
     */
    private Integer isRegistered;

    /**
     * 是否固定医师（0-轮转 1-固定 NULL-未登记）。
     * 仅用于固定急诊医师占比统计（《急诊科建设与管理指南(试行)》2009 50号：固定急诊医师≥在岗75%）。
     */
    private Integer isFixed;

    /**
     * 专家号价格
     */
    private BigDecimal expertPrice;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
