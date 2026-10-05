package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 互联网线上问诊 VO（按钮可用性服务端派生）。
 */
@Data
public class OnlineConsultVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 问诊单号
     */
    private String consultNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号（快照）
     */
    private String patientNo;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 接诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 接诊科室名称（快照）
     */
    private String deptName;

    /**
     * 接诊医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 接诊医生姓名
     */
    private String doctorName;

    /**
     * 问诊方式（1-图文问诊 2-电话问诊 3-视频问诊）
     */
    private Integer consultType;

    /**
     * 主诉/问题描述
     */
    private String chiefComplaint;

    /**
     * 医生回复
     */
    private String reply;

    /**
     * 处置建议
     */
    private String advice;

    /**
     * 是否建议线下就诊（0-否 1-是）
     */
    private Integer needVisit;

    /**
     * 状态（1-待接诊 2-接诊中 3-已完成 4-已退诊）
     */
    private Integer status;

    /**
     * 问诊费用
     */
    private BigDecimal fee;

    /**
     * 发起时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 接诊人
     */
    private String acceptBy;

    /**
     * 接诊时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;

    /**
     * 完成人
     */
    private String finishBy;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /**
     * 退诊原因
     */
    private String rejectReason;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 仅待接诊可接诊
     */
    private Boolean canAccept;

    /**
     * 仅接诊中可回复（回复即结束）
     */
    private Boolean canReply;

    /**
     * 待接诊/接诊中可退诊
     */
    private Boolean canReject;

    /**
     * 仅待接诊可删
     */
    private Boolean canDelete;
}
