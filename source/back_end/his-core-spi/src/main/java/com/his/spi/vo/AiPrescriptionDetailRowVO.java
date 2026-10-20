package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 处方明细行：跨域做合理用药判定与用法用量朗读时要读的药品快照列。
 */
@Data
@NoArgsConstructor
public class AiPrescriptionDetailRowVO {

    /**
     * 处方明细ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 药品ID（用于反查药品主数据，可空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 药品名称（单据快照）
     */
    private String drugName;

    /**
     * 通用名（单据快照，主数据缺失时的回退口径）
     */
    private String genericName;

    /**
     * 规格（单据快照）
     */
    private String specification;

    /**
     * 单次剂量原文
     */
    private String singleDosage;

    /**
     * 用药频次原文
     */
    private String frequency;

    /**
     * 给药途径原文
     */
    private String route;

    /**
     * 疗程天数
     */
    private Integer duration;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 是否已做皮试（1-是 0-否）
     */
    private Integer isSkinTest;
}
