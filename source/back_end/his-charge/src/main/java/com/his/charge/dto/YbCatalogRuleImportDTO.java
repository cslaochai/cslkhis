package com.his.charge.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保目录报销规则批量导入行（按 rule_no 幂等，已存在即更新）。
 *
 * <p>规则表是「项目 × 费别 × 就诊类型 × 险种」四维配置，一行错一个维度就多报/少报统筹，
 * 所以校验比目录表严：比例必须落在 0-100、险种必须与患者主档原文同源、编号必须合规。
 */
@Data
public class YbCatalogRuleImportDTO {

    /**
     * 规则编号（ICR+yyyyMMdd+5位，全局唯一，幂等键）。
     *
     * <p>不自动生成：编号是这条规则的政策批次标识，同一期政策必须共用同一批号，
     * 自动生成会让「哪批政策导的」在库里查不出来 —— 规则直接决定报多少钱，出处要可追。
     */
    @NotBlank(message = "规则编号不能为空")
    @Pattern(regexp = "^ICR\\d{13}$", message = "规则编号格式应为 ICR+yyyyMMdd+5位")
    private String ruleNo;

    /**
     * 项目编码（必须与院内字典 item_code 同源：药品 drug_code / 诊疗 treatment_item.item_code 等）
     */
    @NotBlank(message = "项目编码不能为空")
    @Size(max = 32, message = "项目编码长度不能超过32")
    private String itemCode;

    /**
     * 项目名称
     */
    @Size(max = 200, message = "项目名称长度不能超过200")
    private String itemName;

    /**
     * 医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）
     */
    @NotNull(message = "目录类别不能为空")
    @AllowedValues(values = {"0", "1", "2", "3"}, message = "目录类别不合法")
    private Integer catalogType;

    /**
     * 就诊类型（1-门诊 2-住院）
     */
    @NotNull(message = "就诊类型不能为空")
    @AllowedValues(values = {"1", "2"}, message = "就诊类型不合法")
    private Integer encounterType;

    /**
     * 医保类型（<b>患者主档 medical_insurance_type 原文</b>：城镇职工医保 / 城乡居民医保 / 公费医疗；
     * 留空=通用规则）
     *
     * <p>不写死枚举值：主档里出现新险种时（例如异地结算另设的参保身份），
     * 规则必须能跟上，不该被 DTO 拦在门外报错。
     */
    @Size(max = 32, message = "医保类型长度不能超过32")
    private String insuranceType;

    /**
     * 自付比例%（0-100）
     */
    @NotNull(message = "自付比例不能为空")
    @DecimalMin(value = "0", message = "自付比例不能小于0")
    @DecimalMax(value = "100", message = "自付比例不能大于100")
    private BigDecimal selfPayRatio;

    /**
     * 起付线（元，0=无起付线）
     */
    @DecimalMin(value = "0", message = "起付线不能小于0")
    private BigDecimal deductible;

    /**
     * 封顶线（元，0=不限）
     */
    @DecimalMin(value = "0", message = "封顶线不能小于0")
    private BigDecimal ceiling;

    /**
     * 统筹报销比例%（0-100）
     */
    @NotNull(message = "统筹比例不能为空")
    @DecimalMin(value = "0", message = "统筹比例不能小于0")
    @DecimalMax(value = "100", message = "统筹比例不能大于100")
    private BigDecimal poolRatio;

    /**
     * 生效日期（yyyy-MM-dd，空取当天）
     */
    @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "生效日期格式应为 yyyy-MM-dd")
    private String effectiveDate;

    /**
     * 失效日期（yyyy-MM-dd，空=长期有效）
     */
    @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "失效日期格式应为 yyyy-MM-dd")
    private String expireDate;

    /**
     * 优先级（数字越小越优先，空取 0）
     */
    private Integer priority;

    /**
     * 备注（<b>政策出处写这里</b>：如「湘医保发〔2023〕22号」）
     */
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}