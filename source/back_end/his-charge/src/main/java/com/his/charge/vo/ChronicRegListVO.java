package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 慢特病备案行。
 */
@Data
public class ChronicRegListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 备案单号
     */
    private String regNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 医保卡号（展示用，已脱敏）
     */
    private String medicalInsuranceNoMasked;

    /**
     * 病种目录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
    @JsonSerialize(using = ToStringSerializer.class)
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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate certifyDate;

    /**
     * 诊断依据（病历摘要/检验结果/出院小结，必填）
     */
    private String certifyBasis;

    /**
     * 备案经办机构ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registerDeptId;

    /**
     * 备案经办机构名称
     */
    private String registerDeptName;

    /**
     * 备案经办人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registerEmpId;

    /**
     * 备案经办人（谁办的备案）
     */
    private String registerEmpName;

    /**
     * 备案日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registerDate;

    /**
     * 待遇生效日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validStart;

    /**
     * 待遇终止日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validEnd;

    /**
     * 状态（字典 his_yb_chronic_status：1-有效 2-已注销 3-已驳回）
     */
    private Integer regStatus;

    /**
     * 展示态：1-有效 2-已注销 3-已驳回 4-已过期（由 validEnd 与当天现算）
     */
    private Integer displayStatus;

    /**
     * 是否长期有效（validEnd 为空）
     */
    private Boolean longTerm;

    /**
     * 距待遇终止剩余天数（长期为空；负数=已过期）
     */
    private Integer remainDays;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rejectTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
