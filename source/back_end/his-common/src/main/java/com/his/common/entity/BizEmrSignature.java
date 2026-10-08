package com.his.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import com.his.common.enums.SignBizTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 电子签名证据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_emr_signature")
public class BizEmrSignature extends BaseEntity {

    /**
     * 签名流水号（SIG + yyyyMMdd + 6位序号）
     */
    private String signNo;

    /**
     * 签名对象类型（码值权威见 {@link SignBizTypeEnum}，1~9）
     */
    private Integer bizType;

    /**
     * 签名对象ID
     */
    private Long bizId;

    /**
     * 对象单号
     */
    private String bizNo;

    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 患者姓名
     */
    private String patientName;
    /**
     * 对象所属科室ID
     */
    private Long deptId;
    /**
     * 对象所属科室名称
     */
    private String deptName;

    /**
     * 签名场景（1-提交 2-归档 3-开立 4-校对 5-补签）
     */
    private Integer signScene;

    /**
     * 同对象第几次签名（从 1 递增）
     */
    private Integer chainNo;
    /**
     * 前一次签名ID
     */
    private Long prevSignId;
    /**
     * 前一次签名摘要
     */
    private String prevDigest;

    /**
     * 签名人员工ID
     */
    private Long signerId;
    /**
     * 签名人姓名
     */
    private String signerName;
    /**
     * 签名人科室ID
     */
    private Long signerDeptId;
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
     * 签名值（RSA 签名，Base64）
     */
    private String signValue;

    /**
     * 被签内容快照（规范化文本全文，举证用）
     */
    private String contentSnapshot;

    /**
     * 签名时刻（服务端取值，不接受客户端传入）
     */
    private LocalDateTime signedTime;

    /**
     * 时间来源（1-本机时钟 2-院内授时服务器 3-第三方TSA）
     */
    private Integer timeSource;

    /**
     * 第三方时间戳序列号（time_source=3 时必填）
     */
    private String tsaSerial;

    /**
     * TSA 授时时刻（time_source=3 时必填；与 tsa_serial/tsa_token 三列同生共死）
     */
    private LocalDateTime tsaTime;

    /**
     * 时间戳令牌（TSA 私钥对「序列号|摘要|时刻」的签名，Base64；time_source=3 时必填）
     */
    private String tsaToken;

    /**
     * 签名状态（1-有效 2-已作废）
     */
    private Integer signStatus;

    /**
     * 最近一次验签结果（0-未校验 1-通过 2-失败）
     */
    private Integer verifyStatus;
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
     * 作废操作人员工ID
     */
    private Long invalidBy;
    /**
     * 作废操作人姓名
     */
    private String invalidByName;

    /**
     * 签名来源 IP（留痕）
     */
    private String clientIp;
}
