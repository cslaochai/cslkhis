package com.his.spi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 待发药记录创建入参：处方明细缴清后，病历域把开药快照交给药房域落待发药记录。
 * 发药单号由药房域自己生成，调用方不传。
 */
@Data
public class PendingDispensingCreateDTO {

    /**
     * 处方主表ID
     */
    @NotNull(message = "缺少处方ID")
    private Long prescriptionId;

    /**
     * 处方号
     */
    @NotBlank(message = "缺少处方号")
    @Size(max = 32, message = "处方号不能超过32个字符")
    private String prescriptionNo;

    /**
     * 处方明细ID（待发药记录幂等锚点：同一条明细已有记录不叠加）
     */
    @NotNull(message = "缺少处方明细ID")
    private Long prescriptionDetailId;

    /**
     * 患者ID
     */
    @NotNull(message = "缺少患者ID")
    private Long patientId;

    /**
     * 患者号
     */
    @Size(max = 32, message = "患者号不能超过32个字符")
    private String patientNo;

    /**
     * 患者姓名
     */
    @NotBlank(message = "缺少患者姓名")
    @Size(max = 50, message = "患者姓名不能超过50个字符")
    private String patientName;

    /**
     * 药品ID
     */
    private Long drugId;

    /**
     * 药品编码
     */
    @Size(max = 32, message = "药品编码不能超过32个字符")
    private String drugCode;

    /**
     * 药品名称
     */
    @NotBlank(message = "缺少药品名称")
    @Size(max = 200, message = "药品名称不能超过200个字符")
    private String drugName;

    /**
     * 规格
     */
    @Size(max = 100, message = "规格不能超过100个字符")
    private String specification;

    /**
     * 单位
     */
    @Size(max = 20, message = "单位不能超过20个字符")
    private String unit;

    /**
     * 数量（饮片行已是「每剂克数 × 剂数」，原样抄，发药侧再按库存档案单位换算）
     */
    private BigDecimal quantity;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 金额
     */
    private BigDecimal amount;
}
