package com.mis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工资格证书（仿 HIS biz_employee_qualification）
 */
@Data
@TableName("mss_employee_qualification")
public class MssEmployeeQualification implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;
    /** 证书类型（his_emp_cert_type 字典） */
    private String certType;
    private String certNo;
    /** 发证机关（his_emp_cert_org 字典） */
    private String issueOrg;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;
    /** 有效期至（空=长期有效） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;
    private String remark;

    private String createBy;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createById;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private String updateBy;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateById;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @TableLogic
    private Integer delFlag;
}
