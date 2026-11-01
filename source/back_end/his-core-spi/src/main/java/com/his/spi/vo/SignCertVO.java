package com.his.spi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签名证书展示对象（台账分页 / 详情 / 签发结果共用）。
 */
@Data
public class SignCertVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 证书编号
     */
    private String certNo;

    /**
     * 签名人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long empId;

    /**
     * 签名人姓名
     */
    private String empName;

    /**
     * 员工编号（工号，sys_employee.emp_code）：线下送 CA 做 RA 申请时取用，可作 Subject DN 的 OU
     */
    private String empCode;

    /**
     * 所属科室ID（雪花 ID 超 JS 安全整数范围，Number 化会静默改值）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 所属科室名称
     */
    private String deptName;

    /**
     * 密钥算法
     */
    private String keyAlgo;

    /**
     * 摘要算法
     */
    private String digestAlgo;

    /**
     * 签名算法
     */
    private String signAlgo;

    /**
     * 公钥指纹
     */
    private String keyFingerprint;

    /**
     * 指纹分组展示（每 4 位一组，便于人工核对）
     */
    private String keyFingerprintGroups;

    /**
     * CA 签发申请主体 DN（CN=姓名, O=机构, OU=员工编号）；证书已回填时取证书里的实际主体
     */
    private String caSubjectDn;

    /**
     * CA 证书序列号（CA 回执）
     */
    private String caSerial;

    /**
     * CA 在线申请单号（csssl applyNo；线下人工流程为空）
     */
    private String caApplyNo;

    /**
     * CA 申请单状态码（PENDING/APPROVED/ISSUED/REJECTED/CANCELLED/FAILED）
     */
    private String caApplyStatus;

    /**
     * CA 申请单状态文案（由枚举 label 渲染）
     */
    private String caApplyStatusText;

    /**
     * CA 最近一次状态说明 / 提交失败原因
     */
    private String caLastMessage;

    /**
     * 签发方式（1-人工签发 2-系统自动签发）
     */
    private Integer issuedMode;

    /**
     * 签发方式文案（由枚举 label 渲染）
     */
    private String issuedModeText;

    /**
     * 证书状态（1-有效 2-已吊销）
     */
    private Integer certStatus;

    /**
     * 证书状态文案（由枚举 label 渲染）
     */
    private String certStatusText;

    /**
     * 生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTo;

    /**
     * 有效期的来源标记：true=「申请时按有效天数估算」的预期值（证书待 CA 签发，CA 实际签发后会被覆盖）；
     * false=已由 CA 签发证书文件的 notBefore/notAfter 确定的真值。
     *
     * <p>前端据此区分展示：预期值不许当真实有效期用（否则用户会以为证书当下就能签、或已过期）。
     */
    private Boolean validityEstimated;

    /**
     * 是否已过期（有效期过了但状态还是"有效"时单独提示）
     */
    private Boolean expired;

    /**
     * 吊销原因
     */
    private String revokeReason;

    /**
     * 吊销时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokeTime;

    /**
     * 吊销操作人姓名
     */
    private String revokeByName;

    /**
     * 最近一次使用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUsedTime;

    /**
     * 累计签名次数
     */
    private Integer signCount;

    /**
     * 公钥 PEM（详情接口回填，列表不带）
     */
    private String publicKey;

    /**
     * 是否可吊销
     */
    private Boolean canRevoke;

    /**
     * 操作提示文案：证书已吊销或已过有效期时说明后续动作，正常证书为空
     */
    private String actionHint;
}
