package com.his.patient.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 推进订餐状态机（配餐 → 配送 → 签收）或退订。
 */
@Data
public class MealStatusDTO {

    /**
     * 主键ID集合
     */
    @NotEmpty(message = "请选择要处理的订餐")
    private List<Long> ids;

    /**
     * 配餐状态（0-待配餐 1-已配餐 2-已配送 3-已签收 4-已取消）
     */
    @NotNull(message = "目标状态不能为空")
    private Integer deliverStatus;

    /**
     * 配餐内容（推进到 1-已配餐时可写当日食谱，如"糖尿病午餐：杂粮饭+清蒸鱼"）
     */
    private String dishContent;

    /**
     * 签收人（患者/家属/护士姓名，推进到 3-已签收时用）
     */
    private String signBy;

    /**
     * 退订原因（4-已取消必填：停餐/出院/拒餐/转科等）
     */
    private String cancelReason;
}
