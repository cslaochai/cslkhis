package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 麻精药品空安瓿回收 / 剩余液销毁登记入参。
 *
 * <p>《麻醉药品和精神药品管理条例》要求麻醉药品注射剂的<b>空安瓿必须回收</b>、
 * 剩余液须双人监督销毁并记录。本接口只补记回收信息，**不改动专册的业务字段** ——
 * 一旦允许改数量或批号，专册就不再是证据。
 */
@Data
public class AmpouleReturnDTO {

    /**
     * 专册登记行ID（必填）
     */
    @NotNull(message = "专册登记行ID不能为空")
    private Long id;

    /**
     * 回收空安瓿数（必填，须与实际发出量核对）
     */
    @NotNull(message = "回收空安瓿数不能为空")
    private BigDecimal ampouleReturned;

    /**
     * 剩余液销毁量（可空；注射剂未用完部分）
     */
    private BigDecimal ampouleDestroyed;

    /**
     * 回收/销毁说明（双人销毁须写明见证人姓名）
     */
    private String returnRemark;
}
