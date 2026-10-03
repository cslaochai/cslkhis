package com.his.ai.vo;

import lombok.Data;

/**
 * 检验结果逐项概览（由确定性规则生成，不依赖模型）
 */
@Data
public class LabItemOverviewVO {

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 结果值
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
     * 异常标志文本
     */
    private String abnormalFlagText;

    /**
     * 是否达到危急值
     */
    private Boolean critical;

    /**
     * 危急值说明
     */
    private String criticalDesc;

    /**
     * 未做判定的原因（判定成功时为 null）
     */
    private String judgeNote;
}
