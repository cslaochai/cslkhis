package com.his.spi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.spi.dto.SignCertIssueDTO;
import com.his.spi.dto.SignCertPageQueryDTO;
import com.his.spi.dto.SignCertRevokeDTO;
import com.his.spi.vo.SignCaProbeOutboundVO;
import com.his.spi.vo.SignCaStatusVO;
import com.his.spi.vo.SignCertBriefVO;
import com.his.spi.vo.SignCertSelectListVO;
import com.his.spi.vo.SignCertVO;

import java.time.LocalDateTime;
import java.util.List;

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

    // ---------- 证书台账运维（签名中心页面口径，跨模块经本端口暴露） ----------

    /**
     * 证书台账分页：按持有人、证书状态、签发方式与关键字过滤。
     */
    IPage<SignCertVO> listCertPage(SignCertPageQueryDTO query);

    /**
     * 单张证书详情：含公钥 PEM、指纹与签发信息；证书不存在抛业务异常。
     */
    SignCertVO getCertDetail(Long certId);

    /**
     * 人工签发证书（已有有效证书时拒绝，必须先吊销），返回签发结果。
     */
    SignCertVO issueCert(SignCertIssueDTO dto);

    /**
     * 吊销证书：必须写明理由，只改状态不删行——历史签名仍要靠它的公钥验通。
     */
    void revokeCert(SignCertRevokeDTO dto);

    /**
     * 有效证书下拉选项：按员工姓名 / 证书号模糊查有效证书，供证书与签发人选择。
     */
    List<SignCertSelectListVO> listActiveCertOptions(String keyword);

    /**
     * CA 模式状态：演示模式 / 远程 CA 的实时自检（模式、适配器名、是否就绪、行为提示）。
     */
    SignCaStatusVO caStatus();

    /**
     * 证书机构外发探针：本地生成密钥对并向适配器提交一次 CSR 试跑，回显提交与回执事实，不落库。
     */
    SignCaProbeOutboundVO probeCaOutbound();
}
