package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门诊慢特病人员备案（谁办的备案 = register_emp_name，服务端默认回填当前登录人）。
 *
 * <p>validEndKey 是唯一键 uk_chronic_active 的辅助列 = COALESCE(valid_end,'9999-12-31')，
 * 由服务端与 validEnd 同步维护：过期的行不再占位，允许续备。
 * 过期是展示态（reg_status=1 且 validEnd 早于今天 → 前端提示续备），不落状态列。
 * 注销/驳回是终态不可逆，所以本表不提供删除接口。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_yb_chronic_reg")
public class BizYbChronicReg extends BaseEntity {

    /**
     * 长期有效在唯一键辅助列上的兜底值
     */
    public static final LocalDate LONG_TERM_KEY = LocalDate.of(9999, 12, 31);

    /**
     * 备案单号（MT+yyyyMMdd+4位）
     */
    private String regNo;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 患者姓名快照
     */
    private String patientName;

    /**
     * 患者编号快照
     */
    private String patientNo;

    /**
     * 医保卡号快照
     */
    private String medicalInsuranceNo;

    /**
     * 病种目录ID
     */
    private Long catalogId;

    /**
     * 病种编码快照
     */
    private String diseaseCode;

    /**
     * 病种名称快照
     */
    private String diseaseName;

    /**
     * 病种类别快照（1-慢性 2-特殊）
     */
    private Integer diseaseType;

    /**
     * 诊断科室ID
     */
    private Long certifyDeptId;

    /**
     * 诊断科室名称
     */
    private String certifyDeptName;

    /**
     * 诊断医师姓名
     */
    private String certifyDoctorName;

    /**
     * 诊断日期
     */
    private LocalDate certifyDate;

    /**
     * 诊断依据（病历摘要/检验结果/出院小结，必填）
     */
    private String certifyBasis;

    /**
     * 备案经办机构ID
     */
    private Long registerDeptId;

    /**
     * 备案经办机构名称
     */
    private String registerDeptName;

    /**
     * 备案经办人ID（外部机构人员可为空）
     */
    private Long registerEmpId;

    /**
     * 备案经办人姓名（必填：谁办的备案）
     */
    private String registerEmpName;

    /**
     * 备案日期
     */
    private LocalDate registerDate;

    /**
     * 待遇生效日
     */
    private LocalDate validStart;

    /**
     * 待遇终止日（NULL=长期）
     */
    private LocalDate validEnd;

    /**
     * 唯一键辅助列 = COALESCE(validEnd, 9999-12-31)
     */
    private LocalDate validEndKey;

    /**
     * 状态（字典 his_yb_chronic_status：1-有效 2-已注销 3-已驳回）
     */
    private Integer regStatus;

    /**
     * 注销原因
     */
    private String cancelReason;

    /**
     * 注销经办人
     */
    private String cancelBy;

    /**
     * 注销时间
     */
    private LocalDateTime cancelTime;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 驳回经办人
     */
    private String rejectBy;

    /**
     * 驳回时间
     */
    private LocalDateTime rejectTime;
}
