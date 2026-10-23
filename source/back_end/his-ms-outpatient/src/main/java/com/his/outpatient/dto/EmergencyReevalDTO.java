package com.his.outpatient.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 急诊分诊复评入参。
 *
 * <p>只升不降由服务端兜底校验（新级别数值必须 &le; 原级别），前端禁用降级选项只是第一道，
 * 第二道在这里——API 直连绕得过界面，绕不过这里。
 */
@Data
public class EmergencyReevalDTO {

    /**
     * 急诊记录ID
     */
    @NotNull(message = "急诊记录ID不能为空")
    private Long id;

    /**
     * 复评后分诊级别（1-I级濒危 2-II级危重 3-III级急症 4-IV级非急症）
     */
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "分诊级别取值不合法")
    @NotNull(message = "复评分诊级别不能为空")
    private Integer triageLevel;

    /**
     * 复评时意识状态（1-清楚 2-对声音有反应 3-对疼痛有反应 4-无反应）。
     * 复评必须重采意识：MEWS 五项里它是区分「睡着」和「叫不醒」的唯一一项，
     * 也是分诊台上 30 秒能重新测完的项，没有理由复用登记时的旧值
     */
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "意识状态取值不合法")
    @NotNull(message = "意识状态不能为空")
    private Integer consciousness;

    /**
     * 复评时生命体征（JSON 格式；不传 = 沿用登记时的体征，MEWS 按旧体征 + 新意识重算）
     */
    private String vitalSigns;

    /**
     * 复评原因（必填：改级必须留下「凭什么改」，这是台账质控的抓手）
     */
    @NotBlank(message = "复评原因不能为空")
    private String reason;
}
