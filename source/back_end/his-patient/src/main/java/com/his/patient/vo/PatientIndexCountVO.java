package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * EMPI 概览原始计数（对应 {@code PatientIndexMapper.selectIndexStats} 的 6 个标量子查询）。
 *
 * <p>这 6 个数字全部是 {@code COUNT(*)}，没有业务字段，直接 Long 承接；
 * 比率（唯一性 / 各字段完整率）不在这里算 —— 那是"分子分母怎么定"的口径判断，
 * 属于业务语义，留在 service 收口（见 {@code PatientIndexServiceImpl#stats}）。
 *
 * <p>类名叫 CountVO 而不是直接复用 {@code PatientIndexStatVO}：后者还带 4 个
 * service 现算的比率字段，与 SQL 返回的列不是一对一，混在一起会让"这字段哪来的"变模糊。
 */
@Data
public class PatientIndexCountVO implements Serializable {

    /**
     * 患者档案总数（含已并入主档的影子档案）
     */
    private Long patientTotal;

    /**
     * 已并入主档的档案数
     */
    private Long mergedCount;

    /**
     * 强重复组数（身份证号非空且重复的分组数；手机号重复不计入）
     */
    private Long strongDupGroups;

    /**
     * 身份证号缺失的档案数
     */
    private Long idCardMissing;

    /**
     * 手机号缺失的档案数
     */
    private Long phoneMissing;

    /**
     * 过敏史缺失的档案数
     */
    private Long allergyMissing;
}
