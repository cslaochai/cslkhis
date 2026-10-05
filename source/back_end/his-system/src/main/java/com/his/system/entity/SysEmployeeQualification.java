package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工资格证书实体：一行 = 一个人的一本证（一人多证）。
 *
 * <p>见 sql/112-员工资格证书.sql。与 sql/42 的电子签名证书（电子签名 CA 证书）无关。
 *
 * <p>本表没有 del_flag（不继承 BaseEntity），MyBatis-Plus 的 delete 即物理删，
 * 不会留下占着 {@code uk_emp_cert_type_no} 的软删行。
 */
@Data
@TableName("sys_employee_qualification")
public class SysEmployeeQualification {

    /**
     * 主键（雪花ID）
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
     * 员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 证书类型（字典 his_emp_cert_type：1医师资格证 2医师执业证 3护士执业证 4药师资格证 9其他）
     */
    private String certType;

    /**
     * 证书编号
     */
    private String certNo;

    /**
     * 发证机关（字典 his_emp_cert_org：1国家卫健委；允许清空：updateStrategy=IGNORED，否则 updateById 跳过 null 列改不回来）
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String issueOrg;

    /**
     * 发证日期（允许清空）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDate issueDate;

    /**
     * 有效期至（NULL=长期有效；清空有效期=改成长期有效，必须参与 UPDATE，见 sql/112）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDate validUntil;

    /**
     * 备注
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String remark;
}
