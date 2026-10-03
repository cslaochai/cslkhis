package com.his.operation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.operation.entity.BizAnesthesiaVital;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 麻醉生命体征出参（额外带上"是否异常"的机器判定）。
 *
 * <p><b>异常判定是"提示"不是"诊断"</b>：血压 89/50 在主动脉夹层的患者身上可能是刻意控制的。
 * 所以这里只做常识边界标记（成人收缩压 &lt; 90 或 ≥ 180、SpO2 &lt; 92、心率 &lt; 50 或 &gt; 120），
 * 且不写任何结论文本 —— 与检验「未判定 ≠ 正常」同源：**能给的只有观测值本身**。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnesthesiaVitalVO extends BizAnesthesiaVital {

    private String abnormalText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime sampleTimeLabel;
}
