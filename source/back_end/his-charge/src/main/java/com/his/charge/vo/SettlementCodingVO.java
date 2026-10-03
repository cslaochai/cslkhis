package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 结算清单编码明细出参（诊断 + 手术操作）
 */
@Data
public class SettlementCodingVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long settlementId;

    /**
     * 结算清单号
     */
    private String settlementNo;

    /**
     * 诊断明细
     */
    private List<SettlementDiagnosisVO> diagnoses;

    /**
     * 手术操作明细
     */
    private List<SettlementOperationVO> operations;
}
