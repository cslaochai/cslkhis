package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 住院证展示 VO
 *
 * <p>{@code orderStatusText} / {@code genderText} 由后端统一给文案，前端不再自己拼——
 * 一旦两边各拼一次，"未知码值"就可能在某一侧被渲染成合法值（本项目已踩过这个坑：
 * 检验「未判定」被当成「正常」）。
 */
@Data
public class AdmissionOrderVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 住院证号
     */
    private String orderNo;

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
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    private String genderText;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 身份证号
     */
    private String idCard;

    // 来源门诊线索

    /**
     * 来源挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 来源挂号号
     */
    private String registNo;

    /**
     * 来源就诊次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long visitId;

    // 开证方

    /**
     * 开证科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceDeptId;

    /**
     * 开证科室名称
     */
    private String sourceDeptName;

    /**
     * 开证医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceDoctorId;

    /**
     * 开证医生姓名
     */
    private String sourceDoctorName;

    // 拟收治

    /**
     * 拟收治科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDeptId;

    /**
     * 拟收治科室名称
     */
    private String applyDeptName;
    /**
     * 拟诊ICD编码
     */
    private String diagnosisCode;
    /**
     * 拟诊名称
     */
    private String diagnosisName;
    /**
     * 病情与收治说明
     */
    private String diagnosisNote;

    // 医保

    /**
     * 医保类型
     */
    private String insuranceType;
    /**
     * 医保卡号
     */
    private String medicalInsuranceNo;

    // 状态

    /**
     * 状态（1-待收治 2-已收治 3-已作废 4-已过期）
     */
    private Integer orderStatus;

    private String orderStatusText;

    /**
     * 是否已过有效期（查询时实时算，不落库成状态——同"危急值超时"的口径）
     */
    private Boolean expired;

    /**
     * 预计入院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectAdmitTime;

    /**
     * 开证时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    /**
     * 有效期至
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validUntil;

    // 收治回填

    /**
     * 收治后回填的入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 入院号（收治后才有，JOIN 入院记录得到）
     */
    private String admissionNo;

    /**
     * 实际收治时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admitTime;

    /**
     * 实际收治科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admitDeptId;

    /**
     * 实际收治科室名（JOIN 得到）
     */
    private String admitDeptName;

    /**
     * 是否调过科（实际收治科室 ≠ 拟收治科室）
     */
    private Boolean deptAdjusted;

    /**
     * 作废原因
     */
    private String cancelReason;
    /**
     * 备注
     */
    private String remark;
}
