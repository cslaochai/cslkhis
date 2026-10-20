package com.his.spi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 渠道异步通知留痕入参：一次通知进来后本方的处理结论与报文原文。
 *
 * <p>内部指令而非 HTTP 入参。报文里能自取的事实（单号、渠道交易号、状态、金额、应用ID）由实现方从原文提取，
 * 调用方不得自行拼键名——键名抄进业务模块就会与渠道层漂移成两套。
 */
@Data
@NoArgsConstructor
public class ChannelNotifyRecordDTO {

    /**
     * 通知场景（取值见通知场景枚举，收费台扫码留痕对应「收费台」一档）
     */
    private Integer notifyScene;

    /**
     * 本方处理结果（取值见通知处理结果枚举）
     */
    private Integer notifyResult;

    /**
     * 本方应答原文（渠道协议字段，决定渠道是否重试）
     */
    private String ackBody;

    /**
     * 验签结果（true-通过 false-未通过）
     */
    private Boolean signVerified;

    /**
     * 失败或被拒原因（正常入账传 null）
     */
    private String errorMsg;

    /**
     * 收款渠道（1-微信 2-支付宝 3-云闪付）；表单参数通知不必传，由实现方按报文类型确定
     */
    private Integer channel;

    /**
     * 表单参数报文（支付宝这类键值对通知）；非表单报文传 null
     */
    private Map<String, String> notifyParams;

    /**
     * 非表单报文原文（加密报文解密前取不出任何字段时整体留痕）
     */
    private String rawBody;
}
