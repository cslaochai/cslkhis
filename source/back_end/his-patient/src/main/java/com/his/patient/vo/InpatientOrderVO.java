package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院医嘱出参。
 */
@Data
public class InpatientOrderVO implements Serializable {

    /**
     * 医嘱ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 医嘱号
     */
    private String orderNo;

    /**
     * 入院ID
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
     * 医嘱类型文案
     */
    private String orderTypeText;

    /**
     * 组套号
     */
    private String orderGroup;

    /**
     * 医嘱类别
     */
    private Integer orderClass;

    /**
     * 医嘱类别文案
     */
    private String orderClassText;

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
     * 本次执行数量
     */
    private BigDecimal quantity;

    /**
     * 单价（元）
     */
    private BigDecimal price;

    /**
     * 本次执行金额（元）
     */
    private BigDecimal amount;

    /**
     * 医嘱开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 计划结束时间
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
     * 开立医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 开立医生姓名
     */
    private String doctorName;

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
     * 停止医嘱的医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stopDoctorId;

    /**
     * 停止医嘱的医生姓名
     */
    private String stopDoctorName;

    /**
     * 停止/作废原因
     */
    private String stopReason;

    /**
     * 医嘱状态：1-待校对 2-已校对 3-执行中 4-已完成 5-已停止 6-已作废 7-已退回
     */
    private Integer orderStatus;

    /**
     * 医嘱状态文案
     */
    private String orderStatusText;

    /**
     * 是否加急：0-否 1-是
     */
    private Integer isUrgent;

    /**
     * 是否加急文案
     */
    private String isUrgentText;

    /**
     * 医嘱来源：1-医生 2-模板 3-组套
     */
    private Integer source;

    /**
     * 医嘱来源文案
     */
    private String sourceText;

    /**
     * 备注
     */
    private String remark;

    /**
     * 今日已执行次数（计划行里 exec_status IN (2,3) 的条数）
     */
    private Integer todayExecCount;

    /**
     * 仍未执行的计划条数
     */
    private Integer pendingExecCount;

    /**
     * 是否可校对（待校对）
     */
    private Boolean canVerify;

    /**
     * 是否可作废（仅待校对）
     */
    private Boolean canCancel;

    /**
     * 是否可修改（仅待校对；后端 {@code updateOne} 还要求只改一条项目、不动组套共享字段）
     */
    private Boolean canEdit;

    /**
     * 是否可停止（已校对 / 执行中）
     */
    private Boolean canStop;

    /**
     * 开立医生签名ID（P5.5；为空表示未签开立名）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorSignId;

    /**
     * 开立签名时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime doctorSignedTime;

    /**
     * 校对护士签名ID（为空表示未签校对名）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseSignId;

    /**
     * 校对签名时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nurseSignedTime;

    /**
     * 是否已签开立名
     */
    private Boolean doctorSigned;

    /**
     * 是否已签校对名
     */
    private Boolean nurseSigned;

    /**
     * 签名情况文案（双签完成 / 仅开立签名 / 未签名）
     */
    private String signStatusText;
}
