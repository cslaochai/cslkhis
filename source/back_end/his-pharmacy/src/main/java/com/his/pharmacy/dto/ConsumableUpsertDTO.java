package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 耗材字典新增/修改入参（id 空=新增）
 */
@Data
public class ConsumableUpsertDTO {
    /**
     * 主键ID（空=新增）
     */
    private Long id;
    /**
     * 耗材编码（唯一）
     */
    @NotBlank(message = "耗材编码不能为空")
    private String consumableCode;
    /**
     * 耗材名称
     */
    @NotBlank(message = "耗材名称不能为空")
    private String consumableName;
    /**
     * 类别（1-卫生材料 2-注射穿刺 3-医用敷料 4-防护用品 5-其他）
     */
    private Integer category;
    /**
     * 规格
     */
    private String specification;
    /**
     * 单位（包、支、盒、个等）
     */
    private String unit;
    /**
     * 生产厂家
     */
    private String manufacturer;
    /**
     * 零售价
     */
    private BigDecimal retailPrice;
    /**
     * 是否高值耗材（0-普通 1-高值）
     */
    private Integer isHighValue;
    /**
     * 产品级UDI-DI（GS1 (01) 段）
     */
    private String udiDi;
    /**
     * 医疗器械注册证/备案号
     */
    private String regCertNo;
    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
}
