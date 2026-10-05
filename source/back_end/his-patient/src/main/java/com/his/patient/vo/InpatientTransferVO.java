package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 转科记录出参（P4.2）。
 *
 * <p>三条约定（与会诊 VO 同源）：
 * <ol>
 *   <li>所有 ID 走 {@code ToStringSerializer}：雪花 ID 超 JS 精度，截断后会变成"记录不存在"的假象。</li>
 *   <li>码值一律带 {@code xxxText} 文案，且文案由后端给（前端不自己判状态、不自己拼中文）。</li>
 *   <li>{@code canAccept / canCancel} 由后端按状态算好 —— 按钮可用性属于业务规则，不属于前端。</li>
 * </ol>
 */
@Data
public class InpatientTransferVO implements Serializable {

    /**
     * 转科记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 转科单号
     */
    private String transferNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 入院号（快照）
     */
    private String admissionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    // 转出侧

    /**
     * 转出科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromDeptId;

    /**
     * 转出科室名称（快照）
     */
    private String fromDeptName;

    /**
     * 转出病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromWardId;

    /**
     * 转出病区名称（快照）
     */
    private String fromWardName;

    /**
     * 转出床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromBedId;

    /**
     * 转出床位号（快照）
     */
    private String fromBedNo;

    // 转入侧

    /**
     * 转入科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toDeptId;

    /**
     * 转入科室名称（快照）
     */
    private String toDeptName;

    /**
     * 转入病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toWardId;

    /**
     * 转入病区名称（快照）
     */
    private String toWardName;

    /**
     * 转入床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toBedId;

    /**
     * 转入床位号（快照）
     */
    private String toBedNo;

    // 业务字段

    /**
     * 转科类型：1-普通转科 2-急诊转科 3-转入ICU 4-ICU转出
     */
    private Integer transferType;

    /**
     * 转科类型文案
     */
    private String transferTypeText;

    /**
     * 转科原因
     */
    private String transferReason;

    /**
     * 发起转科时该次住院的已住院天数
     */
    private Integer hospitalDays;

    /**
     * 接收时随之停止的长期医嘱条数
     */
    private Integer stopOrdersCount;

    /**
     * 医嘱处置说明
     */
    private String orderRemark;

    /**
     * 发起医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /**
     * 转出方发起医生姓名（快照）
     */
    private String applyDoctorName;

    /**
     * 转入方接收医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiveDoctorId;

    /**
     * 转入方接收医生姓名（快照）
     */
    private String receiveDoctorName;

    /**
     * 回写的住院病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 回写的住院病历号（病历里查得到这次转科的证据）
     */
    private String recordNo;

    /**
     * 发起时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 接收时间（转科生效时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;

    /**
     * 转科状态：0-待接收 1-已完成 2-已取消
     */
    private Integer transferStatus;

    /**
     * 转科状态文案
     */
    private String transferStatusText;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 备注
     */
    private String remark;

    // 以下为服务层算出的展示字段

    /**
     * 发起至接收的耗时（分钟）：已接收 = receive_time - apply_time；待接收 = 已等待时长
     */
    private Long waitingMinutes;

    /**
     * 等待时长文案（待接收时提示"已等待 N 分钟"，接收后为实际耗时）
     */
    private String waitText;

    /**
     * 可否接收（待接收 + 该次住院仍在院）
     */
    private Boolean canAccept;

    /**
     * 可否取消（仅待接收）
     */
    private Boolean canCancel;
}
