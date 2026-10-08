package com.his.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签名记录展示对象。
 *
 * <p>{@code contentSnapshot} 只在**详情**接口里回填 —— 列表页带上全文快照会让
 * 一页 20 条变成几百 KB 的无用流量。
 */
@Data
public class SignatureVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 签名流水号
     */
    private String signNo;

    /**
     * 签名对象类型
     */
    private Integer bizType;
    private String bizTypeText;

    /**
     * 签名对象ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    /**
     * 对象单号
     */
    private String bizNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;
    /**
     * 对象所属科室名称
     */
    private String deptName;

    /**
     * 签名场景
     */
    private Integer signScene;
    private String signSceneText;
    /**
     * 同对象第几次签名
     */
    private Integer chainNo;

    /**
     * 前一次签名ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prevSignId;

    /**
     * 签名人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long signerId;

    /**
     * 签名人姓名
     */
    private String signerName;
    /**
     * 签名人科室名称
     */
    private String signerDeptName;
    /**
     * 签名人职称
     */
    private String signerTitle;

    /**
     * 所用证书ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long certId;

    /**
     * 所用证书编号
     */
    private String certNo;

    /**
     * 摘要算法
     */
    private String digestAlgo;
    /**
     * 签名算法
     */
    private String signAlgo;

    /**
     * 被签内容摘要（SHA-256 十六进制小写）
     */
    private String contentDigest;

    /**
     * 摘要前 16 位，页面显示用
     */
    private String contentDigestShort;

    /**
     * 签名时刻
     */
    private LocalDateTime signedTime;

    /**
     * 时间来源（1-本机时钟 2-院内授时服务器 3-第三方TSA）
     */
    private Integer timeSource;
    private String timeSourceText;

    /**
     * 时间来源说明：不具备可信时间效力时必须显式说明
     */
    private String timeSourceNote;

    /**
     * 时间戳序列号（time_source=3 时有值）
     */
    private String tsaSerial;

    /**
     * TSA 授时时刻（time_source=3 时有值）
     */
    private LocalDateTime tsaTime;

    /**
     * 签名状态（1-有效 2-已作废）
     */
    private Integer signStatus;
    private String signStatusText;

    /**
     * 最近一次验签结果（0-未校验 1-通过 2-失败）
     */
    private Integer verifyStatus;
    private String verifyStatusText;
    /**
     * 最近一次验签时间
     */
    private LocalDateTime verifyTime;
    /**
     * 累计验签次数
     */
    private Integer verifyCount;

    /**
     * 作废原因
     */
    private String invalidReason;
    /**
     * 作废时间
     */
    private LocalDateTime invalidTime;
    /**
     * 作废操作人姓名
     */
    private String invalidByName;

    /**
     * 是否留存了被签内容快照
     */
    private Boolean hasSnapshot;

    /**
     * 被签内容快照（仅详情接口回填）
     */
    private String contentSnapshot;

    /**
     * 是否可验签
     */
    private Boolean canVerify;

    /**
     * 是否可作废
     */
    private Boolean canInvalidate;

    /**
     * 操作不可用的原因（可用时为空）
     */
    private String actionHint;
}
