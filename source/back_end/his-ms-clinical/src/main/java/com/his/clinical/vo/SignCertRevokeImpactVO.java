package com.his.clinical.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签名证书吊销影响面：吊销前弹窗展示「这张证书一旦废止，还有多少东西签不了」。
 *
 * <p>未签对象口径与医生「我的待补签」一致（病历已提交未签、处方未签且未取消/退回、
 * 检查检验未签且未取消），只统计该证书持有人（empId）名下，用于把不可逆后果摆在操作人面前。
 */
@Data
public class SignCertRevokeImpactVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long certId;

    private String certNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long empId;

    private String empName;

    private String deptName;

    /** 证书状态（1-有效 2-已吊销） */
    private Integer certStatus;

    private String certStatusText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTo;

    /** 已过期（有效期过了但状态仍为有效） */
    private Boolean expired;

    /** 该证书累计签名次数 */
    private Integer signCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUsedTime;

    /** 未签病历（已提交、签名留白） */
    private Long pendingRecordCount;

    /** 未签处方（未落开方签名、且未取消/退回） */
    private Long pendingPrescriptionCount;

    /** 未签检查申请单 */
    private Long pendingInspectionCount;

    /** 未签检验申请单 */
    private Long pendingLaboratoryCount;

    /** 合计未签对象数 */
    private Long pendingTotal;
}
