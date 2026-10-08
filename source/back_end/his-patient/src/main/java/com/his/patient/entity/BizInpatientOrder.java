package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院医嘱（长期 / 临时合一）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_order")
public class BizInpatientOrder extends BaseEntity implements Serializable {

    /**
     * 医嘱号（YZ + yyyyMMdd + 4位序号）
     */
    private String orderNo;

    /**
     * 入院ID（入院记录的入院ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

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
     * 开立科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 开立科室名称
     */
    private String deptName;

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床号
     */
    private String bedNo;

    /**
     * 医嘱类型：1-长期 2-临时
     */
    private Integer orderType;

    /**
     * 组套号：同一组的医嘱必须同起同停
     */
    private String orderGroup;

    /**
     * 医嘱类别：1-药品 2-检查 3-检验 4-治疗 5-护理 6-手术 7-输血 8-监护 9-其他 10-临床营养
     */
    private Integer orderClass;

    /**
     * 项目编码（药品/检查/检验字典码）
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 单位
     */
    private String unit;

    /**
     * 单次剂量
     */
    private BigDecimal dosage;

    /**
     * 剂量单位
     */
    private String dosageUnit;

    /**
     * 给药途径（口服/静滴/肌注…）
     */
    private String route;

    /**
     * 频次（qd/bid/tid/q8h…）
     */
    private String frequency;

    /**
     * 本次执行数量（计费用）
     */
    private BigDecimal quantity;

    /**
     * 单价（元，开立时快照）
     */
    private BigDecimal price;

    /**
     * 本次执行金额（元）= quantity × price
     */
    private BigDecimal amount;

    /**
     * 医嘱开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 计划结束时间（长期医嘱为空表示"到停医嘱/出院为止"）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planEndTime;

    /**
     * 实际停止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stopTime;

    /**
     * 开立时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    /**
     * 开立医生ID（员工ID，不是用户的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 开立医生姓名
     */
    private String doctorName;

    /**
     * 开立医生签名ID（电子签名证据的ID，P5.5）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorSignId;

    /**
     * 开立签名时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime doctorSignedTime;

    /**
     * 校对护士ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long verifyNurseId;

    /**
     * 校对护士姓名
     */
    private String verifyNurseName;

    /**
     * 校对时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifyTime;

    /**
     * 校对护士签名ID（电子签名证据的ID，P5.5）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseSignId;

    /**
     * 校对签名时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nurseSignedTime;

    /**
     * 停止医嘱的医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stopDoctorId;

    /**
     * 停止医嘱的医生姓名
     */
    private String stopDoctorName;

    /**
     * 停止原因（作废原因也写这里，靠 orderStatus 区分）
     */
    private String stopReason;

    /**
     * 医嘱状态：1-待校对 2-已校对 3-执行中 4-已完成 5-已停止 6-已作废 7-已退回
     */
    private Integer orderStatus;

    /**
     * 是否加急：0-否 1-是
     */
    private Integer isUrgent;

    /**
     * 医嘱来源：1-医生 2-模板 3-组套
     */
    private Integer source;
}
