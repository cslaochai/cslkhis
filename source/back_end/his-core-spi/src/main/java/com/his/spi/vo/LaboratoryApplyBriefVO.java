package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 检验申请单跨域摘要：医技侧接单建检验记录、影像登记时要从申请单上带走的病案与项目维度。
 *
 * <p>只列医技域真正读取的列，申请单自身的流转与签名状态归病历域。
 */
@Data
@NoArgsConstructor
public class LaboratoryApplyBriefVO {

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
     * 检验项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long laboratoryItemId;

    /**
     * 检验项目编码
     */
    private String laboratoryItemCode;

    /**
     * 检验项目名称
     */
    private String laboratoryItemName;

    /**
     * 检验科室ID（承接该项目的医技科室）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long laboratoryDeptId;

    /**
     * 检验科室名称
     */
    private String laboratoryDeptName;

    /**
     * 标本类型
     */
    private String specimenType;

    /**
     * 检验费用（元）
     */
    private BigDecimal price;
}
