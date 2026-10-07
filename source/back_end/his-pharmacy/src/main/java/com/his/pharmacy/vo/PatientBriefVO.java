package com.his.pharmacy.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 患者姓名/号快照（his-patient 的 {@code biz_patient}，跨模块裸 SQL 只读两列）。
 *
 * <p>对应 {@code BizConsumableTraceMapper#selectPatientSnapshot}：高值耗材登记时前端
 * 没传患者姓名/号的兜底路径。台账上必须留当时的姓名与住院号 —— 事后患者改名改号，
 * 「这一件东西用在了谁身上」要能追回去。
 *
 * <p>只取 2 列：不引 his-patient 依赖（与该 Mapper 同族的 {@code selectByUdiDi} 一样走裸 SQL），
 * 也不带 balance/身份证等与「这个人是谁」无关的列。
 */
@Data
public class PatientBriefVO implements Serializable {

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;
}
