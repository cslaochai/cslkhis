package com.his.charge.vo;

import com.his.charge.api.EmrGateway;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 处方明细跨域摘要：诊断依据匹配只用到药名。
 *
 * @see EmrGateway
 */
@Data
@NoArgsConstructor
public class PrescriptionDetailBriefVO {

    /**
     * 处方明细ID
     */
    private Long id;

    /**
     * 所属处方ID
     */
    private Long prescriptionId;

    /**
     * 药品名（诊断依据关键词匹配的素材）
     */
    private String drugName;
}
