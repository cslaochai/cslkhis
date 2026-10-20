package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 验签结果。
 */
@Data
public class SignVerifyVO {

    /**
     * 当前有效签名ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long signId;

    /** 签名流水号 */
    private String signNo;

    /**
     * 业务类型
     */
    private Integer bizType;
    /** 业务类型文案（由枚举 label 渲染） */
    private String bizTypeText;

    /** 业务对象ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    /** 对象单号 */
    private String bizNo;
    /**
     * 患者姓名
     */
    private String patientName;

    /** 签名场景（码值见 SignSceneEnum） */
    private Integer signScene;
    /** 签名场景文案（由枚举 label 渲染） */
    private String signSceneText;
    /** 同对象第几次签名（从 1 递增） */
    private Integer chainNo;

    /** 签名人姓名 */
    private String signerName;
    /** 签名人科室名称 */
    private String signerDeptName;
    /** 签名所用证书编号 */
    private String certNo;

    /** 签名时刻 */
    private LocalDateTime signedTime;
    /** 时间来源（1-本机时钟 2-院内授时服务器 3-第三方可信时间戳） */
    private Integer timeSource;
    /** 时间来源文案（由枚举 label 渲染） */
    private String timeSourceText;

    /** 签名状态（1-有效 2-已作废） */
    private Integer signStatus;
    /** 签名状态文案（由枚举 label 渲染） */
    private String signStatusText;

    /**
     * 签名值校验：公钥能否验通签名值（false = 证据被换过）
     */
    private Boolean signatureValid;

    /**
     * 内容比对：当前内容摘要 == 签名时摘要（false = 签名后被改过）
     */
    private Boolean contentMatched;

    /**
     * 可信时间戳校验（仅 time_source=3 的行有值）：令牌是否由 TSA 签发且与摘要/时刻一致
     */
    private Boolean tsaValid;

    /**
     * 时间戳序列号（time_source=3 时回显）
     */
    private String tsaSerial;

    /**
     * TSA 授时时刻（time_source=3 时回显）
     */
    private LocalDateTime tsaTime;

    /**
     * 时间戳校验说明（通过为空；失败时说明缺什么/哪一环没对上）
     */
    private String tsaNote;

    /**
     * 签名时的内容摘要
     */
    private String digestAtSign;

    /**
     * 当前重算的内容摘要（对象已不存在时为空）
     */
    private String digestNow;

    /**
     * 结论文案
     */
    private String conclusion;

    /**
     * 结论级别：1-通过 2-警告（内容已变更/对象缺失） 3-失败（签名值不通过）
     */
    private Integer conclusionLevel;

    /** 本次验签的服务端时刻 */
    private LocalDateTime checkedAt;
}
