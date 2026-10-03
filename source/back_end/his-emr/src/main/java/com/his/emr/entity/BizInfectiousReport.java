package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 传染病报告卡：填卡→审核→退报重报→直报（外发段预留，报文落 payload）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_infectious_report")
public class BizInfectiousReport extends BaseEntity {

    /** 报卡编号（INF+yyyyMMdd+4位） */
    private String reportNo;

    /** 患者ID */
    private Long patientId;

    /** 患者编号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 性别（快照，性别字典） */
    private Integer gender;

    /** 年龄（快照） */
    private Integer age;

    /** 门诊就诊ID（与 inpId 二选一） */
    private Long registId;

    /** 住院记录ID（与 registId 二选一） */
    private Long inpId;

    /** 发现/就诊科室ID（快照） */
    private Long visitDeptId;

    /** 发现/就诊科室（快照） */
    private String visitDeptName;

    /** 病种ID（法定传染病目录） */
    private Long diseaseId;

    /** 病种编码（快照） */
    private String diseaseCode;

    /** 病种名称（快照） */
    private String diseaseName;

    /** 传染病类别（快照，1甲/2乙/3丙） */
    private Integer infectiousClass;

    /** ICD-10（快照） */
    private String icd10;

    /** 报卡时限（填卡时间+目录时限） */
    private LocalDateTime reportDeadline;

    /** 临床摘要 */
    private String clinicalDesc;

    /** 状态（1待审核/2已审核待直报/3已直报/4已退报） */
    private Integer reportStatus;

    /** 报卡次数（退报重报递增） */
    private Integer reportCount;

    /** 填卡医生ID */
    private Long reportBy;

    /** 填卡医生姓名（快照） */
    private String reportByName;

    /** 填卡时间（时限起算点） */
    private LocalDateTime reportTime;

    /** 审核人姓名 */
    private String auditByName;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核意见 */
    private String auditOpinion;

    /** 退报原因（最近一次） */
    private String returnReason;

    /** 最近一次超时催报时间（幂等锚点） */
    private LocalDateTime notifyTime;

    /** 直报时间 */
    private LocalDateTime directTime;

    /** 直报报文（JSON 预览；真实对接后为已发送报文） */
    private String directPayload;
}
