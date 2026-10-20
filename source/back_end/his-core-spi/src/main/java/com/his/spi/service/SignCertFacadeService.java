package com.his.spi.service;

import com.his.spi.vo.SignCertBriefVO;

import java.time.LocalDateTime;

/**
 * 签名证书端口：证书取用、私钥解密与签名运算的唯一出口，证书事实仍由签名域持有。
 *
 * <p>临床各域只拿「证书编号 + 公钥 + 私钥文本」完成签名与验签，不持有证书档案行；
 * 证书台账的查询与运维（签发、吊销、分页）是签名中心自己的页面口径，不经本端口。
 */
public interface SignCertFacadeService {

    /**
     * 取签名人当前可用证书，没有则按需自动签发一张。
     *
     * <p>用于业务动作触发签名前定位证书：签名不能因为「新员工还没发证书」而阻断医嘱签发与病历提交。
     *
     * @param empId    签名人员工ID
     * @param empName  签名人姓名
     * @param deptId   签名人所属科室ID
     * @param deptName 签名人所属科室名称
     * @return 可用证书摘要
     */
    SignCertBriefVO ensureActiveCert(Long empId, String empName, Long deptId, String deptName);

    /**
     * 按证书ID取证书摘要（公钥用于验签）。
     *
     * <p>用于验签时把签名行上留存的证书定位回公钥；证书行已不存在时返回 null，由调用方给出结论。
     */
    SignCertBriefVO getCertById(Long certId);

    /**
     * 取证书私钥明文（PEM）。
     *
     * <p>仅在签名运算那一瞬使用，禁止写进任何出参与日志。
     */
    String privatePemOf(Long certId);

    /**
     * 登记一次证书使用：累加使用次数并刷新最近使用时间。
     *
     * <p>用于签名行落库成功的同一事务内收尾；证书行不存在时静默跳过，不影响签名落库。
     */
    void markCertUsed(Long certId, LocalDateTime usedTime);

    /**
     * 证书总张数（含已吊销），签名概览卡片的分母。
     */
    long countAllCerts();

    /**
     * 按证书状态计张数。
     */
    long countByStatus(Integer certStatus);

    /**
     * 自动签发（签名时按需补发）的证书张数。
     */
    long countAutoIssued();

    /**
     * 已持有有效证书的员工数。
     */
    long countActiveEmployees();

    /**
     * 内容摘要（十六进制小写）。
     */
    String sha256Hex(String content);

    /**
     * 用私钥对内容签名，返回 Base64 签名值。
     *
     * <p>密钥或算法本身坏了属环境故障，按运行时异常上抛，不与业务失败混淆。
     */
    String signWithPrivateKey(String privatePem, String content);

    /**
     * 用公钥验签。
     *
     * @return true=签名值与公钥匹配；false=不匹配（内容被改过或签名值被换过）
     */
    boolean verifySignValue(String publicKeyPem, String content, String signValue);

    /**
     * 摘要算法名，落进签名行的算法列。
     */
    String digestAlgo();

    /**
     * 签名算法名，落进签名行的算法列。
     */
    String signAlgo();
}
