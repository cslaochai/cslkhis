package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 检查申请单跨域摘要：医技侧开单、预约、建执行记录时要从申请单上带走的病案与项目维度。
 *
 * <p>只列医技域真正读取的列，不代表申请单的全部事实（签名状态、提交与预约时刻等归病历域自己）。
 */
@Data
@NoArgsConstructor
public class InspectionApplyBriefVO {

    /**
     * 申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 申请单号
     */
    private String applyNo;

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
     * 性别（码值权威见 SysGenderEnum）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 挂号ID：一次就诊的标识，记账侧要挂在它上面
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;

    /**
     * 申请科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 申请科室名称
     */
    private String deptName;

    /**
     * 申请医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 申请医生姓名
     */
    private String doctorName;

    /**
     * 检查项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionItemId;

    /**
     * 检查项目编码
     */
    private String inspectionItemCode;

    /**
     * 检查项目名称
     */
    private String inspectionItemName;

    /**
     * 检查科室ID（承接该项目的医技科室）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionDeptId;

    /**
     * 检查科室名称
     */
    private String inspectionDeptName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 检查目的
     */
    private String inspectionPurpose;

    /**
     * 临床诊断
     */
    private String clinicalDiagnosis;

    /**
     * 检查费用（元）
     */
    private BigDecimal price;

    /**
     * 是否急诊（1-是 0-否）
     */
    private Integer isEmergency;

    /**
     * 申请状态（1-已提交 2-已缴费 3-已预约 4-检查中 5-已出报告 6-已取消）
     */
    private Integer applyStatus;
}
