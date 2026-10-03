package com.his.charge.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保目录批量导入行（yb_code 幂等：存在即更新，不存在则新增）。
 */
@Data
public class YbCatalogImportItemDTO {

    /**
     * 目录类型（1-西药/中成药 2-中药饮片 3-医疗服务项目 4-医用耗材）
     */
    private Integer catalogType;

    /**
     * 国家医保编码
     */
    private String ybCode;

    /**
     * 目录名称
     */
    private String ybName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 单位
     */
    private String unit;

    /**
     * 剂型
     */
    private String dosageForm;

    /**
     * 甲乙类（1-甲类 2-乙类 3-丙类/自费）
     */
    private Integer insuranceLevel;

    /**
     * 支付比例%
     */
    private BigDecimal payRatio;
}
