package com.his.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 药品供应商退货单建单/改单入参（sql/154 ③级）
 *
 * <p>id 为空 = 新建；id 非空 = 整单替换明细（仅「待退货」允许）。
 * <p>⚠ 退货金额不从这里收：服务端按批次成本价 × 数量重算，供应商名称也按 supplier_id 现查快照。
 */
@Data
public class SupplierReturnUpsertDTO {

    /** 退货单ID（null=新建） */
    private Long id;

    /** 供应商ID（必填：不知道退给谁就无法向供应商主张退款） */
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    /** 退货原因（近效期 / 质量问题 / 冷链断链 / 采购让价退货…，必填） */
    @NotBlank(message = "退货原因不能为空")
    private String returnReason;

    /** 原入库单号/采购单号（选填，人工填的溯源线索） */
    private String srcRefNo;

    /** 备注（服务端截到列宽 500） */
    private String remark;

    /** 明细项集合 */
    @NotEmpty(message = "退货明细不能为空，至少要选一个批次")
    @Valid
    private List<Item> items;

    /**
     * 一条明细 = 一个库存批次
     */
    @Data
    public static class Item {

        /** 库存批次ID（必须已挂该供应商，且库位=批次实际所在库位） */
        @NotNull(message = "库存批次不能为空")
        private Long stockId;

        /** 退货数量（可小于批次可用量：只退一部分） */
        @NotNull(message = "退货数量不能为空")
        private BigDecimal quantity;

        /** 行备注 */
        private String remark;
    }
}
