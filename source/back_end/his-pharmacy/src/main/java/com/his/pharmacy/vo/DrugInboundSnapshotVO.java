package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 药品入库单快照（本模块 {@code biz_drug_inbound} 的三列只读）。
 *
 * <p>对应 {@code BizDrugTraceMapper#selectInboundSnapshot}：采集追溯码时按入库单 ID 取一次，
 * 只为把「这批货是哪张入库单收进来的」这条来源链钉在台账上。
 *
 * <p>只取 3 列而不是整个 {@code DrugInboundVO}：入库单上会变的审核状态、金额、审核人
 * 与「这枚码从哪来」无关，采集那一刻就该定死，带进来只会让人误以为台账跟着单据走。
 */
@Data
public class DrugInboundSnapshotVO implements Serializable {

    /**
     * 入库单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 入库单号（唯一）
     */
    private String inboundNo;

    /**
     * 供应商（文本快照）
     */
    private String supplier;
}
