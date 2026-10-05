package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 患者端用药说明 · 单药。
 *
 * <p><b>每一个字段都必须能追溯到处方或药品字典上的原始值。</b>
 * 剂量、频次、疗程一律照抄医嘱，本能力不做任何"换算"和"推算" ——
 * 患者拿到的剂量一旦和医生口头交代的不一样，最先被质疑的是医院而不是系统。
 */
@Data
@Schema(description = "患者端用药说明-单药")
public class PatientMedicationItemVO {

    @Schema(description = "药品名称")
    private String drugName;

    @Schema(description = "规格")
    private String specification;

    @Schema(description = "剂型")
    private String dosageForm;

    @Schema(description = "发药数量（含单位）")
    private String quantityText;

    /**
     * 每次吃多少，例如「每次 1 袋」；医嘱没写时为 null，前端显示「按医生交代服用」
     */
    @Schema(description = "单次剂量")
    private String dosageText;

    /**
     * 频次白话，例如「每天 3 次」
     */
    @Schema(description = "用药频次")
    private String frequencyText;

    /**
     * 用药途径，例如「口服」
     */
    @Schema(description = "用药途径")
    private String routeText;

    /**
     * 疗程，例如「连服 7 天」
     */
    @Schema(description = "疗程")
    private String courseText;

    /**
     * 注意事项（代码按药品属性判定，逐条列，不做合并）
     */
    @Schema(description = "注意事项")
    private List<String> cautions;

    /**
     * 说明书常规用法（成人）。
     * <p>
     * 只作为参考展示，<b>且必须标注"以本次医生开的为准"</b> ——
     * 说明书剂量与医嘱不一致在儿科、肝肾功能不全患者身上是常态，
     * 不标注就等于让患者自行选择听谁的。
     */
    @Schema(description = "说明书常规用法（成人，仅供参考）")
    private String specText;
}
