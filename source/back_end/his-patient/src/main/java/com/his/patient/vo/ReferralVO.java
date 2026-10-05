package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 双向转诊 VO。
 */
@Data
public class ReferralVO {

    /**
     * 转诊ID
     */
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
     * 患者编号
     */
    private String patientNo;
    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 就诊次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long visitId;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
    private String admissionNo;

    /**
     * 转出科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromDeptId;
    private String fromDeptName;

    /**
     * 转入科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toDeptId;
    private String toDeptName;
    /**
     * 转入医院
     */
    private String toHospital;

    /**
     * 转诊方向（1-上转 2-下转）
     */
    private Integer direction;

    /**
     * 转诊方向文案（his_referral_direction）
     */
    private String directionText;

    /**
     * 转诊原因
     */
    private String reason;
    /**
     * 诊断摘要
     */
    private String diagnosis;
    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 状态（0-待确认 1-已确认 2-已完成 3-已取消）
     */
    private Integer referralStatus;

    /**
     * 转诊状态文案（his_referral_status）
     */
    private String referralStatusText;

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
}
