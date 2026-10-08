package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 药品字典快照（his-system 的 sys_drug，跨模块裸 SQL 只读）。
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
