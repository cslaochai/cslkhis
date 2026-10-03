package com.his.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 某个对象的签名情况（含签名链）。
 */
@Data
public class ObjectSignatureVO {

    /** 业务类型 */
    private Integer bizType;
    private String bizTypeText;

    /**
     * 签名对象ID。
     *
     * <p>BIGINT 出参一律 {@code ToStringSerializer}：雪花 ID 是 19 位，
     * 超过 JS 的 {@code Number.MAX_SAFE_INTEGER}，前端拿到的会被静默改成
     * {@code ...8177 → ...8200} 这种"看着像 ID、其实已改值"的数 ——
     * 拿它去比对签名链上的 ID 永远不相等，而报错信息只会说"不一致"。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    private String bizNo;
    /** 患者姓名 */
    private String patientName;

    /** 该对象当前锚点状态（0-未签名 1-已签名 2-签名已失效），由业务侧提供 */
    private Integer objectSignStatus;
    private String objectSignStatusText;

    /** 该对象的业务状态文案 */
    private Integer bizStatus;
    private String bizStatusText;

    /** 当前有效签名ID（为空表示没有有效签名） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long currentSignId;
    private String currentSignNo;

    /** 最近一次签名时刻 */
    private LocalDateTime lastSignedTime;

    /** 能否立刻补签 */
    private Boolean canSign;

    /** 不能补签的原因 */
    private String blockReason;

    /** 签名链（按 chain_no 升序，含已作废的） */
    private List<SignatureVO> chain;
}
