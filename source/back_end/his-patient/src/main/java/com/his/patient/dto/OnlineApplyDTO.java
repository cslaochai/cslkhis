package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 线上问诊发起入参（互联网医院场景下多为患者端提交，院内可由医生代为登记）。
 */
@Data
public class OnlineApplyDTO implements Serializable {

    /** 患者ID */
    @NotNull(message = "患者不能为空")
    private Long patientId;

    /** 接诊科室ID */
    private Long deptId;

    /** 接诊医生ID（员工ID） */
    private Long doctorId;

    /** 问诊方式（1-图文问诊 2-电话问诊 3-视频问诊） */
    @NotNull(message = "问诊方式不能为空")
    private Integer consultType;

    /** 主诉/问题描述 */
    @NotBlank(message = "主诉/问题描述不能为空")
    private String chiefComplaint;

    /** 问诊费用 */
    private BigDecimal fee;

    /** 备注 */
    private String remark;
}
