package com.his.spi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签名预检结论：签名人当前能不能完成电子签名、需要哪些确认动作。
 *
 * <p>业务侧（结诊、开立、执行等）在发起签名前先取这份结论，据此决定是放行、提示还是拦下；
 * 判定口径（证书状态与有效期、PIN 免输入窗口）由签名域持有，调用方不自己拼条件。
 */
@Data
public class SignPreCheckVO {

    /**
     * 本次签名可用的证书ID（无可用证书时为空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long certId;

    /**
     * 本次签名可用的证书编号
     */
    private String certNo;

    /**
     * 能否完成电子签名（有有效且在有效期内的证书）
     */
    private Boolean signable;

    /**
     * 最新一张证书已被吊销：签名能力被本人之外的处置废止，必须重新办证，业务应拦下而不是放行
     */
    private Boolean certRevoked;

    /**
     * 最新一张证书已过了有效期（状态仍是「有效」，只是不能再用于新签名）
     */
    private Boolean certExpired;

    /**
     * 可用证书的签名 PIN 是否仍需本人输入（false=处在免输入窗口内，本次不必再输）
     */
    private Boolean pinRequired;

    /**
     * 证书仍在使用建号下发的初始 PIN，签名前必须先换掉本人口令
     */
    private Boolean pinMustChange;

    /**
     * 最新一张证书的状态（0-待CA签发 1-有效 2-已吊销）
     */
    private Integer certStatus;

    /**
     * 证书状态文案（由枚举 label 渲染）
     */
    private String certStatusText;

    /**
     * 最新一张证书的失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTo;

    /**
     * 证书现状的一句话（给管理侧/催办消息用，不带人称）：如「未办理签名证书」「证书 CERT… 已于 … 过期」
     */
    private String certStateText;

    /**
     * 给签名人看的原因说明：不能签名时写明卡在哪一步、下一步找谁办；能签名时为空
     */
    private String checkMessage;
}
