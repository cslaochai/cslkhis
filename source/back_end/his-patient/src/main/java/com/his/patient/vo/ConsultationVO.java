package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 住院会诊出参。
 *
 * <p>三条约定：
 * <ol>
 *   <li>所有 ID 走 {@code ToStringSerializer}：雪花 ID 超 JS 精度，截断后会变成"记录不存在"的假象。</li>
 *   <li>码值一律带 {@code xxxText} 文案，且文案由后端给（前端不自己判状态、不自己拼中文）。</li>
 *   <li>{@code canAccept / canFinish / canCancel / canEdit} 由后端按状态算好 ——
 *       按钮可用性属于业务规则，不属于前端。</li>
 * </ol>
 */
@Data
public class ConsultationVO implements Serializable {

    /**
     * 会诊ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consultationId;

    /**
     * 会诊号
     */
    private String consultationNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 入院记录号
     */
    private String admissionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 床号（住院患者）
     */
    private String bedNo;

    /**
     * 申请科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromDeptId;

    /**
     * 申请科室名称
     */
    private String fromDeptName;

    /**
     * 会诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toDeptId;

    /**
     * 会诊科室名称
     */
    private String toDeptName;

    /**
     * 会诊范围：1-科内 2-科间 3-全院
     */
    private Integer consultType;

    /**
     * 会诊范围文案
     */
    private String consultTypeText;

    /**
     * 会诊类别：1-普通科间 2-营养 3-药学 4-其他专科（sql/168 §5）
     */
    private Integer consultCategory;

    /**
     * 会诊类别文案
     */
    private String consultCategoryText;

    /**
     * 是否急会诊：0-普通 1-急会诊
     */
    private Integer isUrgent;

    /**
     * 急会诊文案（含响应时限）
     */
    private String isUrgentText;

    /**
     * 会诊理由
     */
    private String reason;

    /**
     * 指定会诊医生ID（0 = 未指定）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 申请医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /**
     * 申请医生姓名（快照）
     */
    private String applyDoctorName;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 会诊状态：0-待应答 1-已完成 2-已取消 3-已应答
     */
    private Integer consultStatus;

    /**
     * 会诊状态文案
     */
    private String consultStatusText;

    /**
     * 会诊方接诊时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;

    /**
     * 接诊医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long acceptDoctorId;

    /**
     * 接诊医生姓名（快照）
     */
    private String acceptDoctorName;

    /**
     * 会诊完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /**
     * 会诊时间（出结论的时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consultTime;

    /**
     * 回写的住院病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 回写的住院病历号（病历里查得到这次会诊的证据）
     */
    private String recordNo;

    /**
     * 会诊结论
     */
    private String conclusion;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 备注
     */
    private String remark;

    // 以下为服务层/查询层算出的展示字段

    /**
     * 应答耗时（分钟）：已应答/已完成 = accept_time - apply_time；未应答 = 已等待时长
     */
    private Long responseMinutes;

    /**
     * 急会诊是否已超时未应答（**查询时算**，不落状态列）
     */
    private Boolean overdue;

    /**
     * 超时提示文案
     */
    private String overdueText;

    /**
     * 是否按时应答（急 ≤10 分钟、普通 ≤24 小时）；未应答按未按时计
     */
    private Boolean onTime;

    /**
     * 可否应答
     */
    private Boolean canAccept;

    /**
     * 可否完成
     */
    private Boolean canFinish;

    /**
     * 可否取消
     */
    private Boolean canCancel;

    /**
     * 可否修改申请
     */
    private Boolean canEdit;
}
