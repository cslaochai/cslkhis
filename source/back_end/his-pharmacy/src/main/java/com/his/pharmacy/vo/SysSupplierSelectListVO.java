package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 供应商下拉出参：采购单/退货单选供应商只认「哪一家」，
 * 证照号、有效期、评分、联系人属台账列，走供应商分页与详情看。
 */
@Data
@Schema(name = "SysSupplierSelectListVO", description = "供应商下拉出参")
public class SysSupplierSelectListVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;

    /**
     * 供应商名称
     */
    private String supplierName;
}
