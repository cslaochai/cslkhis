package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 病案借阅/复印 VO
 */
@Data
public class ArchiveBorrowVO {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 单号 BR+yyyyMMdd+4位 */
    private String borrowNo;

    /** 类型（1-借阅 2-复印） */
    private Integer borrowType;

    /** 归档记录病历归档的ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long archiveId;

    /** 病历号（快照） */
    private String recordNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 病历所属科室（快照） */
    private String deptName;

    /** 申请人员工ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicantId;

    /** 申请人姓名（快照） */
    private String applicantName;

    /** 借阅/复印用途（病历讨论/医保核查/司法取证/科研等） */
    private String purpose;

    /** 应归还日期（借阅必填，复印为空） */
    private LocalDate expectReturnDate;

    /** 状态（1-待审核 2-已借出 3-已归还 4-已拒绝 5-已复印） */
    private Integer status;

    /** 审核人员工ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditById;

    /** 审核人姓名（快照） */
    private String auditByName;

    /** 审核意见（拒绝必填） */
    private String auditRemark;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 借出时间 */
    private LocalDateTime lendTime;

    /** 归还时间 */
    private LocalDateTime returnTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
