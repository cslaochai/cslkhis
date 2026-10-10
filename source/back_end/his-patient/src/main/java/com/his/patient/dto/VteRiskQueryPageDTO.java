package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * VTE 中高危名单查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VteRiskQueryPageDTO extends PageParam {

    /**
     * 在院状态：1-在院 0-已出院；空 = 全部
     */
    private Integer admitStatus;

    /**
     * 病区ID
     */
    private Long wardId;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 风险等级（1-低 2-中 3-高 4-极高）；默认只查中高危（>=2）由 onlyHighRisk 控制
     */
    private Integer riskLevel;

    /**
     * 是否只要中高危（true 时 risk_level>=2）
     */
    private Boolean onlyHighRisk;

    /**
     * 落实状态：0-未登记 1-部分落实 2-已落实；空 = 全部
     */
    private Integer preventStatus;

    /**
     * 关键字：患者姓名 / 住院号 / 入院记录号
     */
    private String keyword;
}
