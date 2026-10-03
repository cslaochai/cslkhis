package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医保目录报销规则（L2 结算分摊依据）。
 */
@Data
@TableName("biz_insurance_catalog_rule")
public class BizInsuranceCatalogRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId
    private Long id;

    /**
     * 规则编号（ICR+yyyyMMdd+5位）
     */
    private String ruleNo;

    /**
     * 项目编码（药品/检查/检验/治疗项目的统一编码）
     */
    private String itemCode;

    /**
     * 项目名称（冗余，便于后台管理界面展示）
     */
    private String itemName;

    /**
     * 医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）
     */
    private Integer catalogType;

    /**
     * 就诊类型（1-门诊 2-住院）
     */
    private Integer encounterType;

    /**
     * 医保类型（职工/居民/公费等；NULL=通用规则，所有医保类型共用）
     */
    private String insuranceType;

    /**
     * 自付比例（%）：乙类先自付 X% 再进统筹；甲类=0，丙类=100
     */
    private BigDecimal selfPayRatio;

    /**
     * 起付线（元）：本规则下累计未达此金额前全自费；0=无起付线
     */
    private BigDecimal deductible;

    /**
     * 封顶线（元）：本规则下累计超过此金额的部分全自费；默认极大值表示不限
     */
    private BigDecimal ceiling;

    /**
     * 统筹报销比例（%）：扣除自付后，剩余部分按此比例由统筹支付；0=全自费
     */
    private BigDecimal poolRatio;

    /**
     * 限制标志位掩码：bit0=限适应症 bit1=限二级以上医院 bit2=限急诊 bit3=限慢病备案 bit4=限转诊证明
     */
    private Integer limitFlags;

    /**
     * 生效日期（含）
     */
    private LocalDate effectiveDate;

    /**
     * 失效日期（含；NULL=长期有效）
     */
    private LocalDate expireDate;

    /**
     * 优先级（数字越小越优先）
     */
    private Integer priority;

    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
