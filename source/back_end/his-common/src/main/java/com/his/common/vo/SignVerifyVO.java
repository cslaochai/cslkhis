package com.his.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 验签结果。
 *
 * <p>刻意把结论拆成**两个独立断言**，而不是给一个 {@code pass} 布尔值：
 * <ul>
 *   <li>{@link #signatureValid}：拿证书公钥去验 {@code content_snapshot} + {@code sign_value}。
 *       回答"这份签名值是不是这把私钥对这份快照签的"（= 证据本身有没有被换）。</li>
 *   <li>{@link #contentMatched}：拿**当前业务内容**重算摘要，与 {@code content_digest} 比对。
 *       回答"这份病历自签名之后有没有被改过"。</li>
 * </ul>
 * 合成一个布尔值就会丢掉最关键的信息：签名值坏掉（证据被篡改，性质严重）与
 * 内容变了（文书被改，可能是正常补录也可能是篡改）是两件事，处理方式完全不同。
 */
@Data
public class SignVerifyVO {

    /** 当前有效签名ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long signId;

    private String signNo;

    /** 业务类型 */
    private Integer bizType;
    private String bizTypeText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    private String bizNo;
    /** 患者姓名 */
    private String patientName;

    private Integer signScene;
    private String signSceneText;
    private Integer chainNo;

    private String signerName;
    private String signerDeptName;
    private String certNo;

    private LocalDateTime signedTime;
    private Integer timeSource;
    private String timeSourceText;

    private Integer signStatus;
    private String signStatusText;

    /** 签名值校验：公钥能否验通签名值（false = 证据被换过） */
    private Boolean signatureValid;

    /** 内容比对：当前内容摘要 == 签名时摘要（false = 签名后被改过） */
    private Boolean contentMatched;

    /** 可信时间戳校验（仅 time_source=3 的行有值）：令牌是否由 TSA 签发且与摘要/时刻一致 */
    private Boolean tsaValid;

    /** 时间戳序列号（time_source=3 时回显） */
    private String tsaSerial;

    /** TSA 授时时刻（time_source=3 时回显） */
    private LocalDateTime tsaTime;

    /** 时间戳校验说明（通过为空；失败时说明缺什么/哪一环没对上） */
    private String tsaNote;

    /** 签名时的内容摘要 */
    private String digestAtSign;

    /** 当前重算的内容摘要（对象已不存在时为空） */
    private String digestNow;

    /** 结论文案 */
    private String conclusion;

    /** 结论级别：1-通过 2-警告（内容已变更/对象缺失） 3-失败（签名值不通过） */
    private Integer conclusionLevel;

    private LocalDateTime checkedAt;
}
