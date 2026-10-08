package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 药品字典快照（his-system 的 {@code sys_drug}，跨模块裸 SQL 只读）。
 *
 * <p>对应 {@code BizDrugTraceMapper#selectDrugByTraceKey}（按追溯标识反查，GS1 的 GTIN-14 命中
 * {@code trace_di}、20 位码的本体码命中 {@code trace_code_prefix}，两个键一起试）与
 * {@code #selectDrugSnapshot}（按 ID 取）—— 两条 SQL 选列完全相同，故共用一个类。
 *
 * <p><b>为什么是快照 VO 而不是 his-system 的 {@code SysDrug} 实体</b>：
 * his-pharmacy 不依赖 his-system 的实体（见 {@code BizDrugTraceMapper} 类注释的架构约定），
 * 追溯台账只冻「扫码那一刻药品长什么样」，字典后续改名改价都不回溯台账 —— 这是合规要求：
 * 追溯链上留的必须是发放当时的实物身份。
 */
@Data
public class DrugDictSnapshotVO implements Serializable {

    /**
     * 药品ID（sys_drug 主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 药品编码
     */
    private String drugCode;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 通用名
     */
    private String genericName;

    /**
     * 规格
     */
    private String specification;

    /**
     * 剂型
     */
    private String dosageForm;

    /**
     * 单位
     */
    private String unit;

    /**
     * 生产企业
     */
    private String manufacturer;

    /**
     * 批准文号（快照，医保上报必填）
     */
    private String approvalNumber;

    /**
     * 是否要求扫码采集（1-必须采集：麻精/集采/医保谈判品种；0-不要求；null-字典未标）
     */
    private Integer isTraceRequired;

    /**
     * 药品启用状态（0-停用 1-启用）。停用药一律不许采集，扫码页要当场拦下来
     */
    private Integer status;
}
