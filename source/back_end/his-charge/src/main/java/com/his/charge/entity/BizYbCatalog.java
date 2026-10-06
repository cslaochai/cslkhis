package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 国家医保目录（本地模拟目录库：正式对接时由前置机下载导入，表结构不变）。
 *
 * <p>铁律：唯一键 uk_yb_code 不含 del_flag → 本表<b>不提供删除，只做启停</b>，
 * 保证已对照行的追溯链完整（143 脚本头有说明）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_yb_catalog")
public class BizYbCatalog extends BaseEntity {

    /**
     * 目录类型（1-西药/中成药 2-中药饮片 3-医疗服务项目 4-医用耗材）
     */
    private Integer catalogType;

    /**
     * 国家医保编码（X-西药 T-中药饮片 C-医疗服务 B-耗材）
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
     * 剂型（药品类）
     */
    private String dosageForm;

    /**
     * 甲乙类（1-甲类 2-乙类 3-丙类/自费；医疗服务项目可为空）
     */
    private Integer insuranceLevel;

    /**
     * 支付比例%（如100.00全额 70.00乙类自付30）
     */
    private java.math.BigDecimal payRatio;

    /**
     * 生效日期
     */
    private java.time.LocalDate effectiveDate;

    /**
     * 失效日期（NULL=长期有效）
     */
    private java.time.LocalDate expireDate;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
