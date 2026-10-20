package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 病历质控提示词变量。
 */
@Data
public class EmrQcPromptVariablesVO implements PromptVariables {

    /**
     * 性别
     */
    private String gender;

    /**
     * 年龄（含「岁」后缀，未填写时为「（未填写）」）
     */
    private String age;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 现病史
     */
    private String presentIllness;

    /**
     * 既往史
     */
    private String pastHistory;

    /**
     * 个人史
     */
    private String personalHistory;

    /**
     * 家族史
     */
    private String familyHistory;

    /**
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 一般情况
     */
    private String generalCondition;

    /**
     * 皮肤黏膜
     */
    private String skinMucosa;

    /**
     * 头颈部
     */
    private String headNeck;

    /**
     * 胸部肺野
     */
    private String chestLung;

    /**
     * 心脏
     */
    private String heart;

    /**
     * 腹部
     */
    private String abdomen;

    /**
     * 脊柱四肢
     */
    private String spineLimbs;

    /**
     * 神经系统
     */
    private String nervousSystem;

    /**
     * 专科检查
     */
    private String specialistExam;

    /**
     * 辅助检查
     */
    private String auxiliaryExam;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 治疗方案
     */
    private String treatmentPlan;
}