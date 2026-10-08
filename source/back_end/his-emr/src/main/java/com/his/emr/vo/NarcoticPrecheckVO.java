package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 处方麻精预检结论（G10）——发药窗口点「发药」之前问一次"这单能不能发、要哪些手续"。
 */
@Data
public class NarcoticPrecheckVO {

    /**
     * 处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 处方是否已填写临床诊断（麻精处方必填）
     */
    private Boolean hasDiagnosis;

    /**
     * 本处方是否含管制品种
     */
    private Boolean hasControlledDrug;

    /**
     * 本处方是否必须双人复核（含麻醉药品或第一类精神药品）
     */
    private Boolean requiresDualCheck;

    /**
     * 是否含须回收空安瓿的品种（麻醉/一类精神的注射剂）
     */
    private Boolean requiresAmpouleTracking;

    /**
     * 管制明细清单（合规的也在里面，供窗口展示）
     */
    private List<ControlledDrugVO> controlledDrugs;

    /**
     * 违规清单；含 BLOCK 级即不可发
     */
    private List<NarcoticViolationVO> violations;

    /**
     * 当前条件下能否发药（无 BLOCK 级违规即为 true）
     */
    private Boolean canDispense;

    /**
     * 是否"仅差一个超量理由"（第二类精神药品超 7 日）——
     * 窗口据此提示"可填写医师超量理由后放行"，而不是直接把单子打死。
     */
    private Boolean overLimitReasonRequired;
}
