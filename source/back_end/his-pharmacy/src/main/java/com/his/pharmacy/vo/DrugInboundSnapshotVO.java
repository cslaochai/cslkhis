package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 药品入库单快照（本模块 biz_drug_inbound 的三列只读）。
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
