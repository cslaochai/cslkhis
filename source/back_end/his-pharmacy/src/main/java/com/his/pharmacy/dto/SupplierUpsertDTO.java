package com.his.pharmacy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 供应商新增/修改入参（supplierId 为空=新增，非空=修改）
 */
@Data
public class SupplierUpsertDTO {

    /** 供应商ID（null=新增） */
    private Long supplierId;

    /** 供应商编码 */
    @NotBlank(message = "供应商编码不能为空")
    @Size(max = 32, message = "供应商编码最长 32 位")
    private String supplierCode;

    /** 供应商名称 */
    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 128, message = "供应商名称最长 128 位")
    private String supplierName;

    /** 联系人 */
    @Size(max = 32, message = "联系人最长 32 位")
    private String contactPerson;

    /** 联系电话 */
    @Size(max = 32, message = "联系电话最长 32 位")
    private String phone;

    /** 地址 */
    @Size(max = 256, message = "地址最长 256 位")
    private String address;

    /** 营业执照号 */
    @Size(max = 64, message = "营业执照号最长 64 位")
    private String licenseNo;

    /** 资质证照有效期 */
    private LocalDate licenseExpiry;

    /** 评级：1-差 2-一般 3-良好 4-优秀（空则默认 3） */
    @Min(value = 1, message = "供应商评级取值不合法（1-差 2-一般 3-良好 4-优秀）")
    @Max(value = 4, message = "供应商评级取值不合法（1-差 2-一般 3-良好 4-优秀）")
    private Integer rating;

    /** 状态：0-停用 1-正常（空则默认 1） */
    @Min(value = 0, message = "供应商状态取值不合法（0-停用 1-正常）")
    @Max(value = 1, message = "供应商状态取值不合法（0-停用 1-正常）")
    private Integer status;

    /** 备注 */
    @Size(max = 500, message = "备注最长 500 位")
    private String remark;
}
