package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 患者端-待预约检查申请（小程序「检查预约」列表行）。
 *
 * <p>与员工侧 {@code ExamApptVO.ApplyVO} 的区别：只出患者要看的字段，
 * 不带申请单内部流转状态、不带走私的患者编号。</p>
 */
@Data
public class PatientExamApplyVO {

    /**
     * 检查申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 申请单号
     */
    private String applyNo;

    /**
     * 检查项目名称
     */
    private String itemName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 预计时长（分钟）
     */
    private Integer examMinutes;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 就诊日期（yyyy-MM-dd）
     */
    private String visitDate;

    /**
     * 开单科室
     */
    private String applyDeptName;

    /**
     * 开单医生
     */
    private String doctorName;

    /**
     * 是否急诊（1-是 0-否）
     */
    private Integer isEmergency;

    /**
     * 申请单状态文案
     */
    private String applyStatusText;

    /**
     * 已配置到几台设备（0 = 暂无设备可约，需联系医技科室）
     */
    private Integer deviceCount;
}
