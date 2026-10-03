package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医保目录行（列表/对照候选共用）。
 */
@Data
public class BizYbCatalogVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 目录类型（1-西药/中成药 2-中药饮片 3-医疗服务项目 4-医用耗材）
     */
    private Integer catalogType;

    /**
     * 国家医保编码
     */
    private String ybCode;

    /**
     * 目录名称（国家标准名）
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
     * 甲乙类（1-甲类 2-乙类 3-丙类）
     */
    private Integer insuranceLevel;

    /**
     * 支付比例%
     */
    private BigDecimal payRatio;

    /**
     * 生效日期
     */
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    private LocalDate expireDate;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
