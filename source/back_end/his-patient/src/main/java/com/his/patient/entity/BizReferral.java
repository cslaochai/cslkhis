package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 转诊（双向转诊）实体 —— 对齐既有转诊表（11 号建表 + 85 号扩列，PK=referral_id）。
 *
 * <p>状态机（表注释既有口径）：0 待确认 → 1 已确认 → 2 已完成；0/1 → 3 已取消。
 * direction 区分双向：1-上转（转往上级医院）2-下转（转回基层/社区）。
 */
@Data
@TableName("biz_referral")
public class BizReferral {

    /**
     * 转诊ID
     */
    @TableId(value = "referral_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long referralId;

    /**
     * 转诊编号
     */
    private String referralNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 就诊次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long visitId;

    /**
     * 转出科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromDeptId;

    /**
     * 转入科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toDeptId;
    /**
     * 转入医院
     */
    private String toHospital;
    /**
     * 转诊原因
     */
    private String reason;

    /**
     * 转诊方向（1-上转 2-下转）
     */
    private Integer direction;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
    /**
     * 诊断摘要
     */
    private String diagnosis;
    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 确认人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditBy;
    /**
     * 确认人姓名
     */
    private String auditName;
    /**
     * 确认时间
     */
    private LocalDateTime auditTime;
    /**
     * 确认意见
     */
    private String auditRemark;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
    /**
     * 完成备注
     */
    private String finishRemark;

    /**
     * 转诊时间
     */
    private LocalDateTime referralTime;

    /**
     * 状态（0-待确认 1-已确认 2-已完成 3-已取消）
     */
    private Integer referralStatus;

    /**
     * 备注
     */
    private String remark;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
}
