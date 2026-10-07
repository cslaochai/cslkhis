package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 病历文书生成（草稿）提示词变量。
 *
 * <p>对应 {@code prompts/emr-draft.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>字段与 {@code EmrDraftDTO} 的体征项同构：都是「空值可空」的采集项，
 * 空的一律填「-」（见 nullToDash），不让模板里出现空占位。
 */
@Data
public class EmrDraftPromptVariablesVO implements PromptVariables {

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
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 体温（℃）
     */
    private String temperature;

    /**
     * 脉搏（次/分）
     */
    private String pulse;

    /**
     * 呼吸（次/分）
     */
    private String respiration;

    /**
     * 血压（mmHg）
     */
    private String bloodPressure;

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
}