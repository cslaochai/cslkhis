package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检验结果行：一条结果连同宿主模块已算好的判定结论（异常方向、是否未判定、是否危急值）。
 *
 * <p>判定口径由医技域给出，调用方只消费结论，不再各自复算一遍。
 */
@Data
@NoArgsConstructor
public class AiLabResultRowVO {

    /**
     * 结果行ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属检验记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 检验项目编码
     */
    private String laboratoryItemCode;

    /**
     * 检验项目名称
     */
    private String laboratoryItemName;

    /**
     * 检验结果值
     */
    private String resultValue;

    /**
     * 结果单位
     */
    private String resultUnit;

    /**
     * 参考范围
     */
    private String referenceRange;

    /**
     * 异常标志（0-正常 1-偏高 2-偏低 3-异常）
     */
    private Integer abnormalFlag;

    /**
     * 异常标志文案（未判定的结果这里给「未判定」，不给「正常」）
     */
    private String abnormalFlagText;

    /**
     * 未做判定的原因（判定成功时为 null）
     */
    private String judgeNote;

    /**
     * 是否属于未能自动判定的结果
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
     * 结果值里的首个数字（定性结果与无法解析时为 null，趋势计算只认这个值）
     */
    private Double numericValue;
}
