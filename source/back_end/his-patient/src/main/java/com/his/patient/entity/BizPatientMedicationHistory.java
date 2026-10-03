package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 既往用药史实体（既往用药史）
 *
 * <p>这张表此前只有 CDR 的裸 SQL 在读（{@code CdrMapper.PROFILE_SQL} 的 'medication' 分支），
 * 没有实体、没有 Mapper、没有 Controller —— 也就是说**能看不能维护**，写入口压根不存在。
 * 六组健康档案里唯独这一组是「只读摆设」，本类补上的就是这条链路。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_patient_medication_history")
public class BizPatientMedicationHistory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 药物名称
     */
    private String drugName;

    /**
     * 药物类型（处方药/非处方药/中药/保健品）
     */
    private String drugType;

    /**
     * 剂量（如 0.5g、30mg）
     */
    private String dosage;

    /**
     * 频次（如 qd、bid、tid）
     */
    private String frequency;

    /**
     * 给药途径（口服/注射/外用/吸入等）
     */
    private String route;

    /**
     * 开始用药日期
     */
    private LocalDate startDate;

    /**
     * 停药日期
     */
    private LocalDate endDate;

    /**
     * 用药指征 / 适应症
     */
    private String indications;

    /**
     * 处方医生
     */
    private String prescriber;

    /**
     * 用药状态（进行中/已停用/已换药/已减量）
     *
     * <p>口径以建表注释为准。⚠ 表列 DEFAULT 是 '已完成'，那个值**不在**注释枚举里 ——
     * 不要依赖 DB 默认值，写入口必须显式给值，否则库里会长出第五种状态。
     */
    private String status;

    /**
     * 停药原因（疗效不佳/不良反应/患者要求/已治愈）
     */
    private String reasonStop;
}
