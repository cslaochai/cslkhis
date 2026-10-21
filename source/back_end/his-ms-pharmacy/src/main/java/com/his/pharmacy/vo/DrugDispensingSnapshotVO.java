package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发药单快照（his-emr 的 biz_drug_dispensing，跨模块裸 SQL 只读）。
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
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 药品编码
     */
    private String drugCode;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 规格
     */
    private String specification;

    /**
     * 单位
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
