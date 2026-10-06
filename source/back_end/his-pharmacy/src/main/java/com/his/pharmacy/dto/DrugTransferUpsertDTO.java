package com.his.pharmacy.dto;

import com.his.common.enums.DrugTransferTypeEnum;
import com.his.common.validation.InEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 药品调拨单建单/改单入参（sql/154）
 *
 * <p>id 为空 = 新建（按点到的批次抓库存快照写明细）；id 非空 = 整单替换明细（仅「待发出」允许）。
 * <p>⚠ 成本价、批号、效期、药名等一律服务端从批次快照，不接受前端传值：
 * 调拨金额是账实核对的依据，让前端传就等于让它编。
 */
@Data
public class DrugTransferUpsertDTO {

    /** 调拨单ID（null=新建） */
    private Long id;

    /** 方向（1-药库下拨药房 2-药房退回药库） */
    @NotNull(message = "调拨方向不能为空")
    @InEnum(value = DrugTransferTypeEnum.class, message = "调拨方向取值不合法（1-药库下拨药房 2-药房退回药库）")
    private Integer transferType;

    /** 事由（必填：事后没人记得为什么搬这批货，就等于没有溯源） */
    @NotBlank(message = "调拨事由不能为空")
    private String reason;

    /** 备注（服务端截到列宽 500，入参层不限长度：AGENTS §3） */
    private String remark;

    /** 明细项集合 */
    @NotEmpty(message = "调拨明细不能为空，至少要选一个批次")
    @Valid
    private List<Item> items;

    /**
     * 一条明细 = 一个发出方批次
     */
    @Data
    public static class Item {

        /** 发出方库存批次ID */
        @NotNull(message = "库存批次不能为空")
        private Long stockId;

        /** 调拨数量（不支持部分数量：发出与接收都按它走） */
        @NotNull(message = "调拨数量不能为空")
        private BigDecimal applyQuantity;

        /** 行备注 */
        private String remark;
    }
}
