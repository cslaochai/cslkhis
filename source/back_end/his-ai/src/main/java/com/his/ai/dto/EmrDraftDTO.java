package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 病历草拟入参。
 * <p>
 * <b>为什么不是"只传 recordId"</b>：这条路上有一个死锁 ——
 * 「现病史」是保存病历的<b>必填项</b>，而草拟要产出的正是现病史。
 * 医生边问边填、填完主诉与查体想一键成文时，病历根本还没保存，也就没有 recordId 可用。
 * 因此本入参按「<b>以请求里带的内容为准，未填的字段回落到库内病历</b>」合并：
 * <ul>
 *   <li>字段填了 → 用你当前填写的内容（最新）；</li>
 *   <li>字段没填且传了 {@code recordId} → 用库内已保存病历的值；</li>
 *   <li>两者都没有 → 空，模型看到「（未填写）」。</li>
 * </ul>
 * 这里传业务内容不构成"绕过校验"的口子：本能力<b>不修改任何病历数据</b>，
 * 产出的草稿必须由医生显式点「填入」才进表单。
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
