package com.his.spi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 处方用药快照：跨域生成患者用药说明所需的主档与明细，一次取全。
 */
@Data
@NoArgsConstructor
public class PrescriptionMedicationVO {

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
     * 患者ID（供调用方做数据范围核查）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 开单科室名称
     */
    private String deptName;

    /**
     * 开单医生姓名
     */
    private String doctorName;

    /**
     * 处方类型（1-西药处方 2-中成药处方 3-中药饮片处方）
     */
    private Integer prescriptionType;

    /**
     * 处方状态（1-草稿 2-已提交待审方 3-已审方 4-已发药 5-已作废 6-已退药 7-审方退回）
     */
    private Integer prescriptionStatus;

    /**
     * 中药剂数
     */
    private Integer doseCount;

    /**
     * 是否长处方（1-是 0-否）
     */
    private Integer isLongPrescription;

    /**
     * 长处方天数
     */
    private Integer longPrescriptionDays;

    /**
     * 煎煮方式（1-代煎 2-自煎）
     */
    private Integer decoctFlag;

    /**
     * 处方明细
     */
    private List<PrescriptionMedicationItemVO> items;
}
