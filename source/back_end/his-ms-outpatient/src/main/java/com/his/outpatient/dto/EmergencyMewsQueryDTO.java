package com.his.outpatient.dto;

import lombok.Data;

/**
 * MEWS 实时算分入参（纯规则计算，不落库、不鉴权到数据）。
 *
 * <p>单独开这个口而不是让前端自己算：评分规则必须单一出处（WS/T 810—2022 五项版），
 * 前端镜像一份就意味着某天改规则时两边静默不一致——分诊台上分数就是级别建议，错不起。
 */
@Data
public class EmergencyMewsQueryDTO {

    /**
     * 生命体征（JSON 格式，与登记同一份；为空 = 全部缺项）
     */
    private String vitalSigns;

    /**
     * 意识状态（1-清楚 2-对声音有反应 3-对疼痛有反应 4-无反应；缺项按 0 分计并标记）
     */
    private Integer consciousness;
}
