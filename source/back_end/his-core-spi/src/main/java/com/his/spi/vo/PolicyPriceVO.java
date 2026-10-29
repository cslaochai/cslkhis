package com.his.spi.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 政策价查询单（政策级 + 档位级属性一次拿齐）。
 *
 * <p>诊查费等「政策价」的价格权威在价表（sys_charge_policy / sys_charge_policy_price），
 * 业务模块（急诊记账、复诊拆行、号源默认价）一律通过
 * {@link ChargeFacadeService#policyPrice} 取价，不再读代码内常量。
 *
 * <p>返回 {@code null} 的语义 = 价表缺行或停用：调用方自己降级
 * （急诊只 log 不记账不拦人；门诊回退枚举兜底价并留痕）。
 */
@Data
public class PolicyPriceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 政策编码（OPD_CONSULT / EMG_CONSULT）
     */
    private String policyCode;

    /**
     * 政策项目名称（物价目录名）
     */
    private String policyName;

    /**
     * 院内费用行项目编码（biz_fee_record.item_code，如 ZCF/JZCF）
     */
    private String itemCode;

    /**
     * 院内费用行项目名称
     */
    private String itemName;

    /**
     * 计价单位
     */
    private String unit;

    /**
     * 支付分类（FeeCatalogTypeEnum：3-丙类）
     */
    private Integer catalogType;

    /**
     * 档位码（1-普通 2-副主任 3-主任 4-知名专家，定义见 com.his.common.support.TitleTierMapping）
     */
    private Integer tierCode;

    /**
     * 档位名称
     */
    private String tierLabel;

    /**
     * 国家医保编码（门诊档位子码 / 急诊主项码）
     */
    private String ybCode;

    /**
     * 全额（元）
     */
    private BigDecimal amount;

    /**
     * 加收额（元）：门诊副主任及以上拆「主项目+加收」两行用，无加收为 null
     */
    private BigDecimal surcharge;
}
