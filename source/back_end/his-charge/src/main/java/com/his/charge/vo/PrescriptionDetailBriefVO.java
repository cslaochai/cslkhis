package com.his.charge.vo;

import com.his.charge.api.EmrGateway;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 处方明细跨域摘要：诊断依据匹配只用到药名。
 */
@Data
@NoArgsConstructor
public class PrescriptionDetailBriefVO {

    /**
     * 处方明细ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 药品名（诊断依据关键词匹配的素材）
     */
    private String drugName;
}
