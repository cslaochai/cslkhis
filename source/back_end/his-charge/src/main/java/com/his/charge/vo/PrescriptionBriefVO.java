package com.his.charge.vo;

import com.his.charge.api.EmrGateway;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 处方跨域摘要：目前只用于判断"本次就诊开没开过处方"（入院适应证核查 C02）。
 *
 * <p>收费域不读处方内容，所以这里只带 ID 与单号；将来真要读处方正文，
 * 按"新增字段、不改语义"的方式加，不要把它变成整张处方表的镜像。
 *
 * @see EmrGateway
 */
@Data
@NoArgsConstructor
public class PrescriptionBriefVO {

    /**
     * 处方ID
     */
    private Long id;

    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 处方状态（1-草稿 2-已开立 …，取值口径见 his-emr 的 PrescriptionStatusEnum）
     */
    private Integer prescriptionStatus;
}
