package com.his.appoint.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 急诊记录查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmergencyQueryDTO extends PageParam {
    /**
     * 分诊级别（1-I级濒危 2-II级危重 3-III级急症 4-IV级非急症）
     */
    private Integer triageLevel;

    /**
     * 急诊状态（1-候诊 2-诊治中 3-留观 4-转住院 5-离院 6-死亡）
     */
    private Integer emergencyStatus;

    /**
     * 关键字（模糊匹配：患者姓名 / 患者号 / 急诊号）
     */
    private String keyword;

    /**
     * 只看待派单池（true = 候诊中且没有接诊医生）
     */
    private Boolean unassignedOnly;

    /**
     * 只看已超时（true = 候诊中且已候诊超过登记时快照的应接诊时限）
     */
    private Boolean overdueOnly;

    /**
     * 留观榜（非空 = 只看留观中且已留观 &ge; 该小时数；0=全部在观，48/72 由统计卡带入）
     */
    private Integer observationMinHours;
}
