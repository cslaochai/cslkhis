package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 病历草拟入参。
 */
@Data
@Schema(description = "病历草拟入参")
public class EmrDraftDTO {

    @Schema(description = "病历ID，可选。传了就作为字段缺省值的来源，并作为审计的 bizId")
    private Long recordId;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    @Schema(description = "性别（1-男 2-女），可选")
    private Integer gender;

    /**
     * 年龄
     */
    @Schema(description = "年龄，可选")
    private Integer age;

    @Schema(description = "主诉（必填，空则直接拒绝草拟，不调用模型）")
    private String chiefComplaint;

    @Schema(description = "现病史（医生已写的部分，可空）")
    private String presentIllness;

    @Schema(description = "既往史")
    private String pastHistory;

    @Schema(description = "过敏史")
    private String allergyHistory;

    @Schema(description = "体温")
    private String temperature;

    @Schema(description = "脉搏")
    private String pulse;

    @Schema(description = "呼吸")
    private String respiration;

    @Schema(description = "收缩压")
    private String systolicPressure;

    @Schema(description = "舒张压")
    private String diastolicPressure;

    @Schema(description = "体格检查-一般情况")
    private String generalCondition;

    @Schema(description = "体格检查-皮肤黏膜")
    private String skinMucosa;

    @Schema(description = "体格检查-头颈部")
    private String headNeck;

    @Schema(description = "体格检查-胸肺部")
    private String chestLung;

    @Schema(description = "体格检查-心脏")
    private String heart;

    @Schema(description = "体格检查-腹部")
    private String abdomen;

    @Schema(description = "体格检查-脊柱四肢")
    private String spineLimbs;

    @Schema(description = "体格检查-神经系统")
    private String nervousSystem;

    @Schema(description = "专科检查")
    private String specialistExam;

    @Schema(description = "辅助检查")
    private String auxiliaryExam;
}
