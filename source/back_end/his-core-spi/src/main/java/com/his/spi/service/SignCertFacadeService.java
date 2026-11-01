package com.his.spi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.spi.dto.SignCaPushDTO;
import com.his.spi.dto.SignCertBindDTO;
import com.his.spi.dto.SignCertKeygenDTO;
import com.his.spi.dto.SignCertPageQueryDTO;
import com.his.spi.dto.SignCertRevokeDTO;
import com.his.spi.dto.SignPinChangeDTO;
import com.his.spi.vo.*;

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
     * @param pin     签名人本次输入的 PIN；窗口外的空 PIN 一律拒绝签名
     * @return Base64 签名值
     */
    String signWithCert(Long certId, String content, String pin);

    /**
     * 签名预检：证书能不能用于本次签名、PIN 是否还需本人输入。
     *
     * <p>用于业务动作（结诊、开立、执行等）发起签名前决定放行还是拦下：无证书与已过期属于
     * 「业务照做、签名留白，改走线下纸质签名并催办办证」，已吊销属于「签名能力被废止，必须重新办证」。
     * 空 PIN 能不能签由 PIN 免输入窗口决定，判定口径都在签名域内。
     *
     * @param empId 签名人员工ID
     */
    SignPreCheckVO preCheckForSign(Long empId);

    /**
     * 签名人本人换掉自己的签名 PIN（当前口令校验通过才允许换）。
     *
     * <p>用于首次软签前把建号自动造钥时下发的初始口令换成签名人自己的口令；证书行按当前登录人
     * 定位，不经参数传入，别人拿不到改口令的入口。
     */
    void changeOwnPin(SignPinChangeDTO dto);

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
     * 一张证书的 CA 申请流水（最近一次在前）：第几次申请、当前走到哪一步、提交/审核/签发/回填各阶段的时刻。
     *
     * <p>用于证书详情里回答「这张证办到哪一步了、什么时候走到那一步的」；证书行只留当前挂着的申请单，
     * 每一次尝试的过程与 CA 的说明都在这条流水上。
     */
    List<SignCaApplyRecordVO> listCaApplies(Long certId);

    /**
     * 生成签名密钥对并返回「CA 签发申请单」（在线申请自动提交；未接入时走线下流程）。
     *
     * <p>私钥在 HIS 本地生成并加密托管，全程不出 HIS；在线模式直接向 CA 提交申请，
     * 线下模式由管理员把 Subject DN 与公钥 PEM 送 CA 人工签发，再用
     * {@link #bindCert(SignCertBindDTO)} 回填。
     */
    SignCaApplyVO keygenCert(SignCertKeygenDTO dto);

    /**
     * 提交/重试 CA 在线申请（幂等键为 certNo）。
     */
    SignCaApplyVO submitCaApply(Long certId);

    /**
     * 接收 CA 推送的申请单状态变更：幂等校验 + 状态机流转；ISSUED 随报文带证书，校验公钥后自动回填生效。
     *
     * <p>HIS 不主动查询 CA（对外只保留提交与吊销两个能力），状态事实一律以 CA 推送为准。</p>
     */
    SignCaPushResultVO applyCaPush(SignCaPushDTO dto);

    /**
     * 回填 CA 线下签发的证书（CA 线下签发流程第二步）：校验证书公钥与本地密钥对一致后转为有效。
     */
    SignCertVO bindCert(SignCertBindDTO dto);

    /**
     * 上传 CA 系统导出的证书文件回填（CA 线下签发流程第二步）。
     *
     * <p>按内容嗅探形态，不靠扩展名：带密码的 ZIP 先解包，否则把文件本身当证书解析；
     * 解析出的证书链里取公钥与本地密钥对一致的那张生效。证书（含链）存库，不落文件。
     */
    SignCertVO bindCertFile(Long certId, String password, byte[] fileBytes);

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
