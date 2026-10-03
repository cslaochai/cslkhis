package com.his.patient.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 推进订餐状态机（配餐 → 配送 → 签收）或退订。
 *
 * <p>批量、全成功或全不生效：一部分餐被标记"已签收"而另一部分报错，
 * 食堂就说不清今天到底送了几份。
 *
 * <p>状态只许一级推进（0→1→2→3），跳步即拒；4-已取消是旁路且必填原因，
 * 已签收/已取消的行不再接受任何状态变更 —— 签收是闭环，取消要留原因。
 */
@Data
public class MealStatusDTO {

    /** 主键ID集合 */
    @NotEmpty(message = "请选择要处理的订餐")
    private List<Long> ids;

    /** 配餐状态（0-待配餐 1-已配餐 2-已配送 3-已签收 4-已取消） */
    @NotNull(message = "目标状态不能为空")
    private Integer deliverStatus;

    /** 配餐内容（推进到 1-已配餐时可写当日食谱，如"糖尿病午餐：杂粮饭+清蒸鱼"） */
    private String dishContent;

    /** 签收人（患者/家属/护士姓名，推进到 3-已签收时用） */
    private String signBy;

    /** 退订原因（4-已取消必填：停餐/出院/拒餐/转科等） */
    private String cancelReason;
}
