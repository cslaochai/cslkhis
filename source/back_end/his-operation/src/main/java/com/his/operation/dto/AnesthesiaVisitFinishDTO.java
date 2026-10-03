package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 完成术前访视（草稿 → 已完成）。
 *
 * <p>为什么要单独一个动作：访视结论一旦出账就是"麻醉科对这个病人的正式意见"，
 * 之后改它必须重新走完成（留新的时间与新的结论），而不是在某个编辑框里悄悄改。
 * 与"已提交的病历不再直接改同一行"是同一类有意摩擦。
 */
@Data
public class AnesthesiaVisitFinishDTO implements Serializable {

    /** 就诊次ID */
    @NotNull(message = "访视单ID不能为空")
    private Long visitId;

    /** 访视结论：1-可施行麻醉 2-暂缓手术 3-需会诊/进一步评估 */
    @NotNull(message = "访视结论不能为空（没有结论的访视等于没访视）")
    private Integer conclusion;

    /** 结论说明（结论非"可施行麻醉"时必填） */
    private String conclusionNote;
}
