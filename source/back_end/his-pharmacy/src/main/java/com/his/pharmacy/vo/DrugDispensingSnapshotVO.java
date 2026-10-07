package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发药单快照（his-emr 的 {@code biz_drug_dispensing}，跨模块裸 SQL 只读）。
 *
 * <p>对应 {@code BizDrugTraceMapper#selectDispensingSnapshot}：核销追溯码时按发药记录 ID 取一次，
 * 用来回答「这个码核销到哪一次发药上」。
 *
 * <p><b>核销时必须逐字段比对而不是只看 drugId</b>：串码（把 A 药的码核到 B 药的发药记录上）
 * 是医保稽核里最常见也最致命的一类差错，只比药品 ID 还能漏掉「同药不同批次」，
 * 所以患者名、药名、数量一并留在快照里供服务层做提示文案与人工复核。
 */
@Data
public class DrugDispensingSnapshotVO implements Serializable {

    /**
     * 发药记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 发药单号
     */
    private String dispensingNo;

    /**
     * 处方ID（关联 his-emr 处方）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号（快照）
     */
    private String patientNo;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 药品编码（快照）
     */
    private String drugCode;

    /**
     * 药品名称（快照）
     */
    private String drugName;

    /**
     * 规格（快照）
     */
    private String specification;

    /**
     * 单位（快照）
     */
    private String unit;

    /**
     * 发药数量
     */
    private BigDecimal quantity;

    /**
     * 发药状态（1-待发药 2-已发药 3-已退药；仅 2 允许核销追溯码）
     */
    private Integer dispensingStatus;

    /**
     * 发药时间
     */
    private LocalDateTime dispensingTime;
}
