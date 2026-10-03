package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对照工作台行（LEFT JOIN 产出：未对照行 mapping 相关字段为 null）。
 */
@Data
public class YbMappingListVO {

    /**
     * 对照行ID（未对照为 null）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long mappingId;

    /**
     * 院内项目类型（1-药品 2-诊疗项目 3-检验项目 4-耗材）
     */
    private Integer itemType;

    /**
     * 院内项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /**
     * 院内项目编码
     */
    private String itemCode;

    /**
     * 院内项目名称
     */
    private String itemName;

    /**
     * 规格（药品规格/检验标本类型）
     */
    private String spec;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 院内项目状态（0-停用 1-启用）
     */
    private Integer itemStatus;

    /**
     * 国家医保编码（未对照为 null）
     */
    private String ybCode;

    /**
     * 目录名称（未对照为 null）
     */
    private String ybName;

    /**
     * 对照方式（1-自动名称精确 2-人工 3-导入）
     */
    private Integer matchType;

    /**
     * 对照人
     */
    private String mappedBy;

    /**
     * 对照时间
     */
    private LocalDateTime mappedTime;

    /**
     * 甲乙类（目录 live 值，展示用）
     */
    private Integer insuranceLevel;

    /**
     * 支付比例%（目录 live 值，展示用）
     */
    private BigDecimal payRatio;
}
