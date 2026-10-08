package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医疗技术临床应用授权台账实体：一行 = 一个人在一个类别上的一条授权。
 *
 * <p>见 {@code sql/155}。{@code techLevel} 是**级别上限**（授权 3 级即可做 1/2/3 级），
 * 不是每个级别一行 —— 否则「他最高能做到几级」要靠聚合算，闸门每次查询都得 SUM。
 *
 * <p>本表没有 del_flag（与员工资格证书同做法），delete 即物理删，
 * 不会留下占着 {@code uk_emp_cat_from} 的软删行。生效授权只能收回不能改。
 */
@Data
@TableName("sys_employee_tech_auth")
public class SysEmployeeTechAuth {

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
     * 员工姓名（快照：台账要能脱离员工表现场回看「当时是谁」）
     */
    private String employeeName;

    /**
     * 所属科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long deptId;

    /**
     * 所属科室名称
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String deptName;

    /**
     * 职称（快照，授权基准来自职称）
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String title;

    /**
     * 授权类别（字典 his_tech_auth_category / TechAuthCategoryEnum：1-手术 2-麻醉 3-内镜与介入）
     */
    private Integer authCategory;

    /**
     * 可独立操作的手术级别上限（1~4，与字典 his_operation_level 同码）
     */
    private Integer techLevel;

    /**
     * 限定术式编码白名单（逗号分隔；NULL=该级别全部术式，清空=解除限定，必须参与 UPDATE）
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String itemScope;

    /**
     * 授权方式（字典 his_tech_auth_type：1-独立授权 2-上级指导下 3-限制授权须上级在场）
     */
    private Integer authType;

    /**
     * 授权依据（技术准入评价/培训考核/累计手术量，评审要看依据）
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String authBasis;

    /**
     * 授权生效日期
     */
    private LocalDate validFrom;

    /**
     * 有效期至（NULL=长期有效；到期/临期按此列现算，不存冗余状态列）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDate validUntil;

    /**
     * 状态（字典 his_tech_auth_status：1-待审批 2-已授权 3-已驳回 4-已收回）
     */
    private Integer authStatus;

    /**
     * 申请（登记）
     */
    private String applyBy;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyTime;

    /**
     * 审批人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long approverId;

    /**
     * 审批人姓名
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String approverName;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime approveTime;

    /**
     * 审批意见
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String approveOpinion;

    /**
     * 收回人
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String revokeBy;

    /**
     * 收回时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime revokeTime;

    /**
     * 收回原因
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String revokeReason;

    /**
     * 备注
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String remark;
}
