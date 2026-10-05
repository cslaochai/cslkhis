package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保目录新增/修改（id 空=新增，非空=修改；yb_code 全局唯一）。
 *
 * <p>长度靠服务端截断兜底（铁律：不在 DTO 上加 @Size 抢在截断前 400），
 * 这里只对 yb_code/yb_name 给必填校验。
 */
@Data
public class YbCatalogUpsertDTO {

    /**
     * 主键（空=新增）
     */
    private Long id;

    /**
     * 目录类型（1-西药/中成药 2-中药饮片 3-医疗服务项目 4-医用耗材）
     */
    @NotNull(message = "目录类型不能为空")
    private Integer catalogType;

    /**
     * 国家医保编码
     */
    @NotBlank(message = "国家医保编码不能为空")
    private String ybCode;

    /**
     * 目录名称（国家标准名）
     */
    @NotBlank(message = "目录名称不能为空")
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
     * 甲乙类（1-甲类 2-乙类 3-丙类/自费）
     */
    private Integer insuranceLevel;

    /**
     * 支付比例%
     */
    private BigDecimal payRatio;

    /**
     * 生效日期（yyyy-MM-dd）
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "生效日期格式应为 yyyy-MM-dd")
    private String effectiveDate;

    /**
     * 失效日期（yyyy-MM-dd，空=长期有效）
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "失效日期格式应为 yyyy-MM-dd")
    private String expireDate;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
