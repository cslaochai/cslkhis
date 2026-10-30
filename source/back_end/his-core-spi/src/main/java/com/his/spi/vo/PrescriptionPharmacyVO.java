package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 药房域发药/麻精/代煎所需的处方头只读快照（事实宿主：临床域 biz_prescription）。
 *
 * <p>窄口径：只带药房实际读取的列，不暴露处方整行实体。
 */
@Data
public class PrescriptionPharmacyVO implements Serializable {

    /**
     * 处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 处方状态（取值见临床域 PrescriptionStatusEnum）
     */
    private Integer prescriptionStatus;

    /**
     * 临床诊断（麻精处方必须含诊断）
     */
    private String diagnosis;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 开方科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 开方科室名称
     */
    private String deptName;

    /**
     * 开方医师ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 开方医师姓名
     */
    private String doctorName;

    /**
     * 开方医师电子签名ID（null = 未签名，即无签结诊的「待补签」处方，发药侧按此拦截）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorSignId;

    /**
     * 审方药师
     */
    private String auditBy;

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
     * 处方类型（1-西药 2-中成药 3-中药饮片，取值见临床域）
     */
    private Integer prescriptionType;

    /**
     * 是否代煎（1-是 0-否）
     */
    private Integer decoctFlag;

    /**
     * 剂数（煎几付）
     */
    private Integer doseCount;
}
