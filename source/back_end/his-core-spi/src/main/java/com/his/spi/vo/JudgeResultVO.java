package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个原始检验值的判定结果：异常方向、是否未判定、是否危急值与解析出的数值，
 * 全部由医技域按自己的规则算完再给出，调用方只消费结论，不再复算。
 *
 * <p>字段口径与 {@link AiLabResultRowVO} 随结果行携带的判定结论一致，
 * 供调用方对「尚未落库的单个原始值」独立做一次判定。</p>
 */
@Data
@NoArgsConstructor
public class JudgeResultVO {

    /**
     * 异常标志（0-正常 1-偏高 2-偏低 3-异常；未能判定时为 null，不写成 0）
     */
    private Integer abnormalFlag;

    /**
     * 异常标志文案（未判定的结果这里给「未判定」，不给「正常」）
     */
    private String abnormalFlagText;

    /**
     * 是否属于未能自动判定的结果（参考区间不可用 / 结果非数值等）
     */
    private Boolean unjudged;

    /**
     * 是否达到危急值
     */
    private Boolean critical;

    /**
     * 危急值说明
     */
    private String criticalDesc;

    /**
     * 结果值里的首个数字（定性结果与无法解析时为 null）
     */
    private Double numericValue;
}
