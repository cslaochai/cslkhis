package com.his.spi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.spi.dto.SignCertBindDTO;
import com.his.spi.dto.SignCertKeygenDTO;
import com.his.spi.dto.SignCertPageQueryDTO;
import com.his.spi.dto.SignCertRevokeDTO;
import com.his.spi.vo.SignCaApplyVO;
import com.his.spi.vo.SignCaStatusVO;
import com.his.spi.vo.SignCertBriefVO;
import com.his.spi.vo.SignCertSelectListVO;
import com.his.spi.vo.SignCertVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 签名证书端口：证书取用、私钥解密与签名运算的唯一出口，证书事实仍由签名域持有。
 *
 * <p>临床各域只拿「证书编号 + 公钥」完成验签，不持有证书档案行；
 * 签名一律走 {@link #signWithCert(Long, String, String)}（带 PIN 二次确认），
 * 私钥的解密与使用全部收在签名域内，绝不外发。
 * 证书台账的查询与运维（密钥生成、证书回填、吊销、分页）是签名验签自己的页面口径，不经本端口。
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
     * 带 PIN 二次确认的签名（软签主路径）：校验签名人 PIN → 解密私钥 → 签名。
     *
     * <p>私钥的解密与使用全部收在签名域内完成，调用方只拿到签名值，私钥不出签名域。
     *
     * @param certId  证书ID
     * @param content 待签内容
     * @param pin     签名人本次输入的 PIN；为空或错误一律拒绝签名
     * @return Base64 签名值
     */
    String signWithCert(Long certId, String content, String pin);

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

    // ---------- 证书台账运维（签名证书页面口径，跨模块经本端口暴露） ----------

    /**
     * 证书台账分页：按持有人、证书状态、签发方式与关键字过滤。
     */
    IPage<SignCertVO> listCertPage(SignCertPageQueryDTO query);

    /**
     * 按员工取当前有效证书：员工管理「签名设置」弹框只读展示用；无有效证书返回 null。
     */
    SignCertVO getActiveCertByEmp(Long empId);

    /**
     * 单张证书详情：含公钥 PEM、指纹与签发信息；证书不存在抛业务异常。
     */
    SignCertVO getCertDetail(Long certId);

    /**
     * 生成签名密钥对并返回「CA 签发申请单」（CA 线下签发流程第一步）。
     *
     * <p>私钥在 HIS 本地生成并加密托管，全程不出 HIS；管理员把申请单上的 Subject DN 与公钥 PEM
     * 线下送 CA 认证中心人工签发，CA 返回证书 PEM 后用 {@link #bindCert(SignCertBindDTO)} 回填。
     */
    SignCaApplyVO keygenCert(SignCertKeygenDTO dto);

    /**
     * 回填 CA 线下签发的证书（CA 线下签发流程第二步）：校验证书公钥与本地密钥对一致后转为有效。
     */
    SignCertVO bindCert(SignCertBindDTO dto);

    /**
     * 吊销证书：必须写明理由，只改状态不删行——历史签名仍要靠它的公钥验通。
     */
    void revokeCert(SignCertRevokeDTO dto);

    /**
     * 有效证书下拉选项：按员工姓名 / 证书号模糊查有效证书，供证书与签发人选择。
     */
    List<SignCertSelectListVO> listActiveCertOptions(String keyword);

    /**
     * CA 模式状态：证书签发走线下人工（模式、是否就绪、行为提示）。
     */
    SignCaStatusVO caStatus();
}
