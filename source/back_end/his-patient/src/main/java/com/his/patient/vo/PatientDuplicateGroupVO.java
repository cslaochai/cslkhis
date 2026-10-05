package com.his.patient.vo;

import lombok.Data;

import java.util.List;

/**
 * 疑似重复档案组（P5.1 EMPI）
 *
 * <p>一组 = 被同一依据串起来的若干份档案。**这不是"应该合并"的结论**，
 * 而是"值得人看一眼"的提示 —— 库里 44 个患者中真正的重复是 0，
 * 所有同名的都是不同的人，所以组的价值在提示、不在定论。
 */
@Data
public class PatientDuplicateGroupVO {

    /**
     * 组的匹配级别：1-身份证相同 2-姓名+性别+生日 3-姓名+手机号 4-仅同名
     */
    private Integer matchLevel;
    private String matchLevelText;
    /**
     * 命中依据快照，如 "id_card=430726199102122257"
     */
    private String matchEvidence;
    /**
     * 成组键（同一组内一致，用于前端区分）
     */
    private String groupKey;
    /**
     * 是否强依据（仅身份证相同为 true）
     */
    private Boolean strong;
    /**
     * 本组合并时要求的理由最小长度
     */
    private Integer minReasonLength;
    /**
     * 一句话提示：告诉操作者这个级别**不足**以认定同一人
     */
    private String tip;
    /**
     * 组内档案（按业务数据量倒序，数据多的更适合当主档）
     */
    private List<PatientIndexVO> members;
}
