package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.CaApplyStatusEnum;
import com.his.common.enums.CertIssuedModeEnum;
import com.his.common.enums.CertStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.common.utils.UserUtils;
import com.his.platform.config.SignProperties;
import com.his.platform.constant.SignConst;
import com.his.platform.dto.SignCertPageQueryDTO;
import com.his.platform.entity.SysEmployee;
import com.his.platform.entity.SysSignCaApply;
import com.his.platform.entity.SysSignCert;
import com.his.platform.mapper.SignConfigMapper;
import com.his.platform.mapper.SysEmployeeMapper;
import com.his.platform.mapper.SysSignCertMapper;
import com.his.platform.service.RedisSequenceService;
import com.his.platform.service.SignCaApplyService;
import com.his.platform.service.SignCertService;
import com.his.platform.service.SignPinSessionCache;
import com.his.platform.support.SignCaApiClient;
import com.his.platform.utils.*;
import com.his.platform.vo.SignCertVO;
import com.his.spi.dto.SignCertBindDTO;
import com.his.spi.dto.SignCertKeygenDTO;
import com.his.spi.dto.SignCertRevokeDTO;
import com.his.spi.dto.SignPinChangeDTO;
import com.his.spi.vo.SignCaApplyRecordVO;
import com.his.spi.vo.SignCaApplyVO;
import com.his.spi.vo.SignCaStatusVO;
import com.his.spi.vo.SignPreCheckVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 签名证书服务实现。
 *
 * <p>签发主路径为 CA 在线申请（csssl RA）：HIS 本地生成密钥对（私钥加密托管，不出 HIS）→
 * 立即向 CA 提交申请（externalRequestId = certNo，断网重试幂等）→ CA 审核签发后
 * HIS 刷新状态并自动下载证书链回填生效。CA 未接入或提交失败时保留线下人工兜底：
 * 管理员拿「CA 签发申请单」（Subject DN + 公钥）线下送 CA 签发，再把证书 PEM 回填本系统。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignCertServiceImpl extends ServiceImpl<SysSignCertMapper, SysSignCert> implements SignCertService {

    private static final String CERT_NO_PREFIX = "CERT";

    /**
     * 签名 PIN 免输入窗口的代码默认时长（分钟）：系统参数 sign.pin.ttl_minutes 没配时按这个走
     */
    private static final int DEFAULT_PIN_TTL_MINUTES = 30;

    private final SysSignCertMapper sysSignCertMapper;

    private final SysEmployeeMapper sysEmployeeMapper;

    private final SignConfigMapper signConfigMapper;

    private final KeyProtectorUtil keyProtectorUtil;

    private final SignPinUtil signPinUtil;

    private final SignPinSessionCache signPinSessionCache;

    private final SignProperties signProperties;

    private final RedisSequenceService redisSequenceService;

    private final SignCaApiClient signCaApiClient;

    private final SignCaApplyService signCaApplyService;

    // 取证书 / 自动签发

    private static Integer intValue(String s) {
        if (!TextUtil.hasText(s)) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static SignCaApiClient.CaTemplate findTemplate(List<SignCaApiClient.CaTemplate> templates,
                                                           String templateCode) {
        if (templates == null || !TextUtil.hasText(templateCode)) {
            return null;
        }
        for (SignCaApiClient.CaTemplate tpl : templates) {
            if (templateCode.equalsIgnoreCase(tpl.templateCode())) {
                return tpl;
            }
        }
        return null;
    }

    /**
     * 算法口径：RSA2048/RSA 同族，SM2/EC 同族（CA 侧模板与 HIS 配置写法可能不同）
     */
    private static boolean sameAlgorithm(String caAlgorithm, String hisAlgorithm) {
        if (!TextUtil.hasText(caAlgorithm) || !TextUtil.hasText(hisAlgorithm)) {
            return true;
        }
        String a = caAlgorithm.trim().toUpperCase();
        String b = hisAlgorithm.trim().toUpperCase();
        if (a.startsWith("RSA") && b.startsWith("RSA")) {
            return true;
        }
        return (a.startsWith("SM2") || "EC".equals(a)) && (b.startsWith("SM2") || "EC".equals(b));
    }

    private static String statusMessage(SignCaApiClient.CaApplication app) {
        StringBuilder sb = new StringBuilder();
        sb.append(TextUtil.hasText(app.statusText()) ? app.statusText() : app.status());
        if (TextUtil.hasText(app.reviewedByName())) {
            sb.append("；审核人：").append(app.reviewedByName());
        }
        if (TextUtil.hasText(app.reviewComment())) {
            sb.append("；意见：").append(app.reviewComment());
        }
        return sb.toString();
    }

    private static String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() <= 500 ? text : text.substring(0, 500);
    }

    // ---------------- CA 在线申请 ----------------

    private static String mask(String appId) {
        if (!TextUtil.hasText(appId) || appId.length() <= 10) {
            return appId;
        }
        return appId.substring(0, 7) + "..." + appId.substring(appId.length() - 4);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysSignCert ensureActiveCert(Long empId, String empName, Long deptId, String deptName) {
        // C-非 web 入参：EmrSignatureServiceImpl.sign 签发前直接调用（标量形参 empId，无登录态的批处理也会调），Bean Validation 不覆盖，保留
        if (empId == null) {
            throw new BusinessException("签名人不能为空（未取到当前登录用户的员工ID）；"
                    + "签名留痕必须落到员工，不能落成系统账号");
        }
        LocalDateTime now = TimeUtil.nowSeconds();
        SysSignCert exist = sysSignCertMapper.selectActiveByEmp(empId, now);
        if (exist != null) {
            return exist;
        }
        // CA 交互改为在线申请 + 线下兜底，系统仍无法在签名瞬间同步拿到证书——此时必须说清卡在哪一步
        SysSignCert pending = sysSignCertMapper.selectOne(new LambdaQueryWrapper<SysSignCert>()
                .eq(SysSignCert::getEmpId, empId)
                .eq(SysSignCert::getCertStatus, CertStatusEnum.PENDING_CA.getCode())
                .orderByDesc(SysSignCert::getId)
                .last("LIMIT 1"));
        if (pending != null) {
            String applyState = TextUtil.hasText(pending.getCaApplyNo())
                    ? "已提交 CA 申请（申请单 " + pending.getCaApplyNo() + "，当前状态："
                    + CaApplyStatusEnum.getLabel(pending.getCaApplyStatus(), pending.getCaApplyStatus()) + "）"
                    : "已生成密钥对，尚未完成 CA 签发";
            throw new BusinessException("员工「" + (TextUtil.hasText(empName) ? empName : empId)
                    + "」的签名证书 " + pending.getCertNo() + " " + applyState
                    + "；请在「组织与资源 → 签名证书」刷新申请状态或完成签发（CA 未接入时走线下送 CA 回填）后再签名");
        }
        throw new BusinessException("员工「" + (TextUtil.hasText(empName) ? empName : empId)
                + "」没有有效的签名证书；证书签发需先在「组织与资源 → 签名证书」生成密钥对，"
                + "提交 CA 在线申请（或把申请单线下送 CA 签发并回填）后再签名");
    }

    /**
     * 生成密钥对并默认向 CA 提交在线申请；CA 送不出去（不可达、凭证缺失、模板不符）当场报错。
     *
     * <p>私钥本地生成并加密托管，CA 只经手公钥。证书行与申请流水先提交再送单，
     * 所以报错不会把已生成的密钥对回滚掉——管理员照提示转线下回填即可，不必重新生成。
     *
     * <p>刻意不加事务：一次密钥对生成只有一条 INSERT，而向 CA 送单是外网往返。
     * 把两者并成一个事务，送单失败会连证书行一起回滚，线下回填就没有可附着的证书了。
     */
    @Override
    public SignCaApplyVO keygen(SignCertKeygenDTO dto, Long operatorId, String operatorName) {
        if (dto.getEmpId() == null) {
            throw new BusinessException("员工ID不能为空");
        }
        if (!TextUtil.hasText(dto.getEmpName())) {
            throw new BusinessException("员工姓名不能为空（证书上必须能看出这是谁）");
        }
        SysSignCert exist = sysSignCertMapper.selectActiveByEmp(dto.getEmpId(), TimeUtil.nowSeconds());
        if (exist != null) {
            throw new BusinessException("员工「" + dto.getEmpName() + "」已持有有效证书 "
                    + exist.getCertNo() + "（有效期至 " + exist.getValidTo() + "）；"
                    + "如需换新证书请先吊销原证书（吊销会留痕）");
        }
        SysSignCert cert = doKeygen(dto.getEmpId(), dto.getEmpName(), dto.getDeptId(), dto.getDeptName(),
                effectiveValidDays(null), dto.getPin(), dto.getRemark());
        // 默认走在线申请：没接入 CA 才留待线下回填
        submitOnKeygen(cert, operatorName);
        return toApplyVO(cert);
    }

    /**
     * 回填 CA 签发的证书（CA 线下签发第二步）：校验证书公钥与本地密钥对一致后，转为有效状态。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignCertVO bind(SignCertBindDTO dto, Long operatorId, String operatorName) {
        if (dto.getCertId() == null) {
            throw new BusinessException("证书ID不能为空");
        }
        SysSignCert cert = sysSignCertMapper.selectById(dto.getCertId());
        if (cert == null) {
            throw new BusinessException("证书不存在");
        }
        if (!Integer.valueOf(CertStatusEnum.PENDING_CA.getCode()).equals(cert.getCertStatus())) {
            throw new BusinessException("证书 " + cert.getCertNo() + " 当前状态为「"
                    + CertStatusEnum.getLabel(cert.getCertStatus()) + "」，只有「待CA签发」的证书才可回填；"
                    + "如需换证请先生成新的密钥对");
        }
        if (!TextUtil.hasText(dto.getCertPem()) || !dto.getCertPem().contains("BEGIN CERTIFICATE")) {
            throw new BusinessException("证书内容不合法：应为 CA 签发的 PEM 文本（含 -----BEGIN CERTIFICATE----- 段）");
        }
        java.security.cert.X509Certificate x509 = bindCertificate(cert, dto.getCertPem(),
                TextUtil.hasText(dto.getRemark()) ? dto.getRemark()
                        : "CA 线下签发，证书由管理员人工回填（私钥本地生成并加密托管，未出 HIS）");
        // 线下回填同样给最近一单打上「回填生效」的时刻；从没向 CA 送过单（纯人工流程）就没有流水行可记
        SysSignCaApply apply = signCaApplyService.latestOf(cert.getId());
        if (apply != null && TextUtil.hasText(apply.getApplyNo())) {
            signCaApplyService.markBound(apply, issuerOf(x509));
        }
        // 线下回填后在线申请状态不再代表证书现状，清掉状态码避免列表同时显示「已生效 + 提交失败」
        // （updateById 默认忽略 null，必须显式 set 才能把列清空）
        cert.setCaApplyStatus(null);
        sysSignCertMapper.updateById(cert);
        sysSignCertMapper.update(null, new LambdaUpdateWrapper<SysSignCert>()
                .eq(SysSignCert::getId, cert.getId())
                .set(SysSignCert::getCaApplyStatus, null));
        log.info("回填 CA 签发证书 certNo={} empId={} 序列号={} 有效期至={}",
                cert.getCertNo(), cert.getEmpId(), cert.getCaSerial(), cert.getValidTo());
        return toVO(cert, true);
    }

    /**
     * 用 CA 证书 PEM 覆盖证书行：序列号与有效期全部从证书文件解析，
     * 并校验证书公钥与本地密钥对一致——证书文件是唯一事实源，抄写值不许进库。
     *
     * @return 解析出的证书，供调用方取签发者与主体
     */
    private java.security.cert.X509Certificate bindCertificate(SysSignCert cert, String certPem, String remark) {
        java.security.cert.X509Certificate x509;
        try {
            x509 = PemCodecUtil.parseCertificate(certPem);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("CA 证书解析失败：" + e.getMessage());
        }
        String certPublicKey = PemCodecUtil.toPem(x509.getPublicKey());
        if (!certPublicKey.trim().equals(cert.getPublicKey().trim())) {
            throw new BusinessException("CA 证书与本证书的公钥不一致（证书指纹 "
                    + SignCryptoUtil.fingerprintGroups(cert.getPublicKey()) + " ≠ "
                    + SignCryptoUtil.fingerprintGroups(certPublicKey) + "）；"
                    + "请核对签发的是否为本次申请的公钥");
        }
        cert.setCertPem(certPem);
        cert.setCaSerial(x509.getSerialNumber().toString(16));
        cert.setValidFrom(LocalDateTime.ofInstant(x509.getNotBefore().toInstant(), java.time.ZoneId.systemDefault()));
        cert.setValidTo(LocalDateTime.ofInstant(x509.getNotAfter().toInstant(), java.time.ZoneId.systemDefault()));
        cert.setCertStatus(CertStatusEnum.ACTIVE.getCode());
        cert.setRemark(remark);
        return x509;
    }

    @Override
    public SignCaApplyVO applyToCa(Long certId, Long operatorId, String operatorName) {
        SysSignCert cert = requireCertForCa(certId);
        if (Objects.equals(CertStatusEnum.ACTIVE.getCode(), cert.getCertStatus())) {
            return toApplyVO(cert);
        }
        // 已在 CA 审核/待签发：重试动作等价于刷新，不重复建单
        boolean waiting = TextUtil.hasText(cert.getCaApplyNo())
                && (CaApplyStatusEnum.PENDING.getCode().equals(cert.getCaApplyStatus())
                || CaApplyStatusEnum.APPROVED.getCode().equals(cert.getCaApplyStatus()));
        if (waiting) {
            refreshInternal(cert, true);
        } else {
            submitToCa(cert, true, operatorName);
        }
        return toApplyVO(cert);
    }

    @Override
    public SignCaApplyVO refreshCa(Long certId, Long operatorId, String operatorName) {
        SysSignCert cert = requireCertForCa(certId);
        if (Objects.equals(CertStatusEnum.ACTIVE.getCode(), cert.getCertStatus())) {
            return toApplyVO(cert);
        }
        if (!TextUtil.hasText(cert.getCaApplyNo())) {
            throw new BusinessException("证书 " + cert.getCertNo()
                    + " 尚未提交 CA 在线申请；请先提交申请，或改用线下送 CA 回填流程");
        }
        refreshInternal(cert, true);
        return toApplyVO(cert);
    }

    @Override
    public SignCaStatusVO caStatus() {
        SignProperties.Ca ca = signProperties.getCa();
        SignCaStatusVO vo = new SignCaStatusVO();
        vo.setTemplateCode(ca.getTemplateCode());
        vo.setKeyAlgorithm(signProperties.getKeyAlgorithm());
        vo.setAppId(TextUtil.hasText(ca.getAppId()) ? ca.getAppId() : null);
        if (!ca.isEnabled()) {
            vo.setCaMode("offline");
            vo.setAvailable(true);
            vo.setHint("CA 在线申请未启用：HIS 本地生成密钥对（私钥加密托管、不出 HIS），"
                    + "管理员把「CA 签发申请单」线下送 CA 认证中心人工签发，再回填证书");
            return vo;
        }
        vo.setCaMode("online");
        if (!signCaApiClient.enabled()) {
            vo.setAvailable(false);
            vo.setHint("CA 在线申请已启用，但接入凭证未配置完整：请填写 his.sign.ca.base-url / app-id / app-secret"
                    + "（由 CA 侧建租户时颁发）；配置前可继续走线下签发流程");
            return vo;
        }
        try {
            SignCaApiClient.CaTemplate tpl = findTemplate(signCaApiClient.listTemplates(), ca.getTemplateCode());
            if (tpl == null) {
                vo.setAvailable(false);
                vo.setHint("CA 已连通，但模板「" + ca.getTemplateCode() + "」对本租户不可用；"
                        + "请在 CA 侧启用模板或调整 his.sign.ca.template-code");
            } else if (!sameAlgorithm(tpl.algorithm(), signProperties.getKeyAlgorithm())) {
                vo.setAvailable(false);
                vo.setHint("CA 已连通，但模板「" + ca.getTemplateCode() + "」算法为 " + tpl.algorithm()
                        + "，与 HIS 新证书算法 " + signProperties.getKeyAlgorithm() + " 不一致；"
                        + "请改用匹配的模板或调整 his.sign.key-algorithm");
            } else {
                vo.setAvailable(true);
                vo.setHint("已接入 CA 在线申请（租户 App ID " + mask(ca.getAppId()) + "，模板 "
                        + ca.getTemplateCode() + "）：生成密钥对后自动提交申请，CA 签发后自动下载回填；"
                        + "CA 不可用时仍可转线下送 CA 流程");
            }
        } catch (BusinessException e) {
            vo.setAvailable(false);
            vo.setHint("CA 在线申请自检未通过：" + e.getMessage() + "；可继续走线下签发流程");
        }
        return vo;
    }

    /**
     * 密钥对落库后的默认动作：启用了在线申请就当场向 CA 送单，送不出去直接把原因抛给页面。
     *
     * <p>只有配置里明确关掉在线申请（his.sign.ca.enabled=false）才静默留给线下回填；
     * 「配了在线但连不上 / 凭证没填全」不再伪装成线下流程——那样操作员会以为已经在等 CA 出证。
     */
    private void submitOnKeygen(SysSignCert cert, String operatorName) {
        if (!signProperties.getCa().isEnabled()) {
            log.info("CA 在线申请未启用，证书 {} 保留线下送 CA 回填流程", cert.getCertNo());
            return;
        }
        submitToCa(cert, true, operatorName);
    }

    /**
     * 提交 CA 申请：externalRequestId 按 certNo 幂等，驳回/撤单/提交失败后重提时追加时间戳。
     *
     * <p>送单前先落一条申请流水（第几次申请、提交时刻），CA 回执或失败原因都回写那一行；
     * 证书行上的申请单号与状态只是「当前挂哪张单」的指针，各阶段的时刻与说明看流水。
     *
     * @param throwOnError true=提交失败向上抛（页面重试路径）；false=只落 FAILED 状态
     */
    private void submitToCa(SysSignCert cert, boolean throwOnError, String operatorName) {
        SignProperties.Ca ca = signProperties.getCa();
        SysSignCaApply apply = null;
        try {
            signCaApiClient.requireEnabled();
            String externalRequestId = externalRequestIdOf(cert);
            String subjectDn = buildSubjectDn(cert.getEmpName(), cert.getDeptName());
            apply = signCaApplyService.start(cert, externalRequestId, ca.getTemplateCode(), subjectDn);
            SignCaApiClient.CaTemplate tpl = findTemplate(signCaApiClient.listTemplates(), ca.getTemplateCode());
            if (tpl == null) {
                throw new BusinessException("CA 模板「" + ca.getTemplateCode() + "」不存在或未对本租户开放；"
                        + "请在 CA 侧启用模板或调整 his.sign.ca.template-code");
            }
            if (!sameAlgorithm(tpl.algorithm(), cert.getKeyAlgo())) {
                throw new BusinessException("CA 模板「" + ca.getTemplateCode() + "」算法为 " + tpl.algorithm()
                        + "，与本证书密钥算法 " + cert.getKeyAlgo() + " 不一致；"
                        + "请改用匹配的模板或先用匹配算法重新生成密钥对");
            }

            SignCaApiClient.CaApplication app = signCaApiClient.submit(new SignCaApiClient.CaSubmitPayload(
                    externalRequestId, ca.getApplicantType(), cert.getEmpName(), subjectDn,
                    ca.getTemplateCode(), cert.getPublicKey(), null,
                    "HIS 电子签名证书 " + cert.getCertNo(),
                    String.valueOf(cert.getEmpId()), empCodeOf(cert.getEmpId()), cert.getDeptName()));

            String caMessage = statusMessage(app);
            cert.setIssuedMode(CertIssuedModeEnum.ONLINE.getCode());
            cert.setCaApplyNo(app.applyNo());
            cert.setCaApplyStatus(app.status());
            cert.setCaLastMessage(truncate(caMessage));
            signCaApplyService.recordCaResult(apply, app, caMessage);
            if (CaApplyStatusEnum.ISSUED.getCode().equals(app.status())) {
                tryBindIssued(cert, app.applyNo(), apply);
            }
            sysSignCertMapper.updateById(cert);
            log.info("提交 CA 在线申请 certNo={} applyNo={} status={} 操作人={}",
                    cert.getCertNo(), app.applyNo(), app.status(), operatorName);
        } catch (RuntimeException e) {
            cert.setCaApplyStatus(CaApplyStatusEnum.FAILED.getCode());
            cert.setCaLastMessage(truncate(e.getMessage()));
            sysSignCertMapper.updateById(cert);
            signCaApplyService.recordFailure(apply, e.getMessage());
            log.warn("CA 在线申请提交失败 certNo={} err={}", cert.getCertNo(), e.getMessage());
            if (throwOnError) {
                throw e;
            }
        }
    }

    private void refreshInternal(SysSignCert cert, boolean throwOnError) {
        SysSignCaApply apply = signCaApplyService.latestOf(cert.getId());
        try {
            SignCaApiClient.CaApplication app = signCaApiClient.detailByApplyNo(cert.getCaApplyNo());
            String caMessage = statusMessage(app);
            cert.setCaApplyStatus(app.status());
            cert.setCaLastMessage(truncate(caMessage));
            signCaApplyService.recordCaResult(apply, app, caMessage);
            if (CaApplyStatusEnum.ISSUED.getCode().equals(app.status())
                    && !Objects.equals(CertStatusEnum.ACTIVE.getCode(), cert.getCertStatus())) {
                tryBindIssued(cert, app.applyNo(), apply);
            }
            sysSignCertMapper.updateById(cert);
            log.info("刷新 CA 申请状态 certNo={} applyNo={} status={} certStatus={}",
                    cert.getCertNo(), app.applyNo(), app.status(), cert.getCertStatus());
        } catch (RuntimeException e) {
            log.warn("刷新 CA 申请状态失败 certNo={} err={}", cert.getCertNo(), e.getMessage());
            if (throwOnError) {
                throw e;
            }
        }
    }

    /**
     * CA 已签发：下载证书链并校验公钥回填。下载失败不掩盖「已签发」事实，提示可再次刷新。
     */
    private void tryBindIssued(SysSignCert cert, String applyNo, SysSignCaApply apply) {
        try {
            SignCaApiClient.CertificateDownload download =
                    signCaApiClient.download(applyNo, signProperties.getCa().isChain());
            java.security.cert.X509Certificate x509 = bindCertificate(cert, download.pem(), "CA 在线签发（申请单 "
                    + applyNo + "）；私钥本地生成并加密托管，未出 HIS");
            signCaApplyService.markBound(apply, issuerOf(x509));
            cert.setCaLastMessage(truncate("CA 已签发并自动回填生效；证书序列号 " + cert.getCaSerial()
                    + "，有效期至 " + cert.getValidTo()));
        } catch (RuntimeException e) {
            cert.setCaLastMessage(truncate("CA 已签发，但证书自动下载/回填失败：" + e.getMessage()
                    + "；请再次刷新状态重试"));
            log.warn("CA 证书自动回填失败 certNo={} applyNo={} err={}",
                    cert.getCertNo(), applyNo, e.getMessage());
        }
    }

    private SysSignCert requireCertForCa(Long certId) {
        if (certId == null) {
            throw new BusinessException("证书ID不能为空");
        }
        SysSignCert cert = sysSignCertMapper.selectById(certId);
        if (cert == null) {
            throw new BusinessException("证书不存在");
        }
        if (Objects.equals(CertStatusEnum.REVOKED.getCode(), cert.getCertStatus())) {
            throw new BusinessException("证书 " + cert.getCertNo() + " 已吊销，不能继续申请签发");
        }
        return cert;
    }

    /**
     * 生成密钥对并落库（状态=待CA签发）。
     *
     * <p>私钥在本机生成、经主口令加密后落库，全程不出 HIS；CA 认证中心只经手公钥。
     */
    private SysSignCert doKeygen(Long empId, String empName, Long deptId, String deptName,
                                 int validDays, String pin, String remark) {
        // 没有主口令就不签发：宁可不发证，也不让私钥明文落库
        keyProtectorUtil.requireSecret();

        KeyPairFactory.KeyPairPem pair = KeyPairFactory.generate(signProperties.getKeyAlgorithm());
        String keyAlgo = signProperties.getKeyAlgorithm();
        String salt = keyProtectorUtil.newSalt();
        String protectedKey = keyProtectorUtil.protect(pair.privatePem(), salt, signProperties.getIterations());

        // PIN 只存哈希：明文不入库、不落日志
        String pinSalt = signPinUtil.newSalt();
        String pinHash = signPinUtil.hash(pin, pinSalt, signPinUtil.defaultIterations());

        LocalDateTime now = TimeUtil.nowSeconds();
        LocalDateTime to = now.plusDays(validDays);
        String dn = buildSubjectDn(empName, deptName);

        for (int attempt = 1; attempt <= 3; attempt++) {
            SysSignCert cert = new SysSignCert();
            cert.setCertNo(nextCertNo());
            cert.setEmpId(empId);
            cert.setEmpName(empName);
            cert.setDeptId(deptId);
            cert.setDeptName(deptName);
            cert.setKeyAlgo(keyAlgo);
            cert.setPublicKey(pair.publicPem());
            cert.setKeyFingerprint(SignCryptoUtil.fingerprint(pair.publicPem()));
            cert.setProtectedPrivateKey(protectedKey);
            cert.setKeySalt(salt);
            cert.setKeyIterations(signProperties.getIterations());
            cert.setPinHash(pinHash);
            cert.setPinSalt(pinSalt);
            cert.setPinIterations(signPinUtil.defaultIterations());
            cert.setIssuedMode(CertIssuedModeEnum.MANUAL.getCode());
            cert.setCertStatus(CertStatusEnum.PENDING_CA.getCode());
            cert.setValidFrom(now);
            cert.setValidTo(to);
            cert.setSignCount(0);
            cert.setRemark(remark);
            try {
                sysSignCertMapper.insert(cert);
                log.info("生成签名密钥对 certNo={} empId={} 算法={} subject={}（待 CA 签发）",
                        cert.getCertNo(), empId, keyAlgo, dn);
                return cert;
            } catch (DuplicateKeyException e) {
                if (attempt == 3) {
                    throw new BusinessException("证书编号连续冲突 3 次（" + cert.getCertNo()
                            + "），请稍后重试；若持续出现请检查是否有脚本在批量造证");
                }
                log.warn("证书编号冲突，重试第 {} 次：{}", attempt, cert.getCertNo());
            }
        }
        throw new BusinessException("密钥对生成失败");
    }

    /**
     * CA 签发申请的幂等外部请求号：首次用证书编号；上一张单已进终态（驳回 / 撤单 / 本地提交失败）
     * 时必须换新键，否则 CA 按幂等键只回原单，重提永远拿不到新单。
     */
    private String externalRequestIdOf(SysSignCert cert) {
        String status = cert.getCaApplyStatus();
        boolean closed = CaApplyStatusEnum.REJECTED.getCode().equals(status)
                || CaApplyStatusEnum.CANCELLED.getCode().equals(status)
                || CaApplyStatusEnum.FAILED.getCode().equals(status);
        return closed
                ? cert.getCertNo() + "-" + TimeUtil.nowSeconds().format(DateFormats.COMPACT_DATETIME_MS)
                : cert.getCertNo();
    }

    /**
     * 证书主体 DN：已回填 CA 证书就从证书文件读实际主体，未回填才按申请人拼（送 CA 的申请单上就是这个）。
     */
    private String subjectDnOf(SysSignCert cert) {
        if (TextUtil.hasText(cert.getCertPem())) {
            try {
                return PemCodecUtil.parseCertificate(cert.getCertPem())
                        .getSubjectX500Principal().getName(javax.security.auth.x500.X500Principal.RFC2253);
            } catch (IllegalArgumentException e) {
                log.warn("证书 {} 的 PEM 解析主体失败，退回按申请人拼 DN：{}", cert.getCertNo(), e.getMessage());
            }
        }
        return buildSubjectDn(cert.getEmpName(), cert.getDeptName());
    }

    /**
     * 签发该证书的 CA 名称（证书里的 Issuer DN）：回填时留进申请流水，事后能答「这张证是谁发的」。
     */
    private String issuerOf(java.security.cert.X509Certificate x509) {
        return x509.getIssuerX500Principal().getName(javax.security.auth.x500.X500Principal.RFC2253);
    }

    /**
     * CA 签发申请单上的 Subject DN：CN=姓名, O=机构, OU=科室
     */
    private String buildSubjectDn(String empName, String deptName) {
        String organization = signProperties.getOrganization();
        if (!TextUtil.hasText(organization)) {
            throw new BusinessException("未配置机构名（his.sign.organization），无法生成证书申请单主体 DN");
        }
        StringBuilder dn = new StringBuilder("CN=").append(empName)
                .append(", O=").append(organization);
        if (TextUtil.hasText(deptName)) {
            dn.append(", OU=").append(deptName);
        }
        return dn.toString();
    }

    // 查询

    /**
     * 吊销证书：只改本地状态并留痕，不删行、不回滚历史签名（历史签名仍要用它的公钥验通）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignCertVO revoke(SignCertRevokeDTO dto, Long operatorId, String operatorName) {
        // C-非 web 入参：同 issue —— system 模块的 SignatureCenterServiceImpl 直接调用，注解与 @Valid 挂那侧接口，本层拿不到绑定校验；
        // 吊销理由是废止签名能力的留痕依据，任何调用路径都必须带上，Bean Validation 不覆盖，保留
        if (dto.getCertId() == null) {
            throw new BusinessException("证书ID不能为空");
        }
        if (!TextUtil.hasText(dto.getReason())) {
            throw new BusinessException("吊销原因必填（吊销会直接废止后续签名能力，必须写明依据）");
        }
        SysSignCert cert = sysSignCertMapper.selectById(dto.getCertId());
        if (cert == null) {
            throw new BusinessException("证书不存在");
        }
        if (Objects.equals(CertStatusEnum.REVOKED.getCode(), cert.getCertStatus())) {
            throw new BusinessException("证书 " + cert.getCertNo() + " 已是吊销状态，不能重复吊销");
        }
        cert.setCertStatus(CertStatusEnum.REVOKED.getCode());
        cert.setRevokeReason(dto.getReason());
        cert.setRevokeTime(TimeUtil.nowSeconds());
        cert.setRevokeByName(operatorName);
        sysSignCertMapper.updateById(cert);
        log.info("吊销签名证书 certNo={} empId={} 原因={} 操作人={}",
                cert.getCertNo(), cert.getEmpId(), dto.getReason(), operatorName);
        return toVO(cert, true);
    }

    @Override
    public SignCertVO getById(Long id) {
        SysSignCert cert = sysSignCertMapper.selectById(id);
        if (cert == null) {
            throw new BusinessException("证书不存在");
        }
        return toVO(cert, true);
    }

    @Override
    public List<SignCaApplyRecordVO> caApplyHistory(Long certId) {
        List<SignCaApplyRecordVO> list = new ArrayList<>();
        for (SysSignCaApply apply : signCaApplyService.listByCertId(certId)) {
            list.add(toApplyRecordVO(apply));
        }
        return list;
    }

    /**
     * 申请流水行 → 详情时间线行：状态文案由枚举渲染，操作人取这一单是谁提交的。
     */
    private SignCaApplyRecordVO toApplyRecordVO(SysSignCaApply apply) {
        SignCaApplyRecordVO vo = new SignCaApplyRecordVO();
        vo.setApplySeq(apply.getApplySeq());
        vo.setApplyNo(apply.getApplyNo());
        vo.setApplyStatus(apply.getApplyStatus());
        vo.setApplyStatusText(CaApplyStatusEnum.getLabel(apply.getApplyStatus(), apply.getApplyStatus()));
        vo.setCaMessage(apply.getCaMessage());
        vo.setCaIssuer(apply.getCaIssuer());
        vo.setSubmittedAt(apply.getSubmittedAt());
        vo.setReviewedAt(apply.getReviewedAt());
        vo.setIssuedAt(apply.getIssuedAt());
        vo.setBoundAt(apply.getBoundAt());
        vo.setOperatorName(apply.getCreateBy());
        return vo;
    }

    @Override
    public SignCertVO getActiveCertVO(Long empId) {
        SysSignCert cert = sysSignCertMapper.selectActiveByEmp(empId, TimeUtil.nowSeconds());
        return cert == null ? null : toVO(cert, false);
    }

    @Override
    public SignPreCheckVO preCheckForSign(Long empId) {
        SignPreCheckVO vo = new SignPreCheckVO();
        vo.setSignable(false);
        vo.setCertRevoked(false);
        vo.setCertExpired(false);
        vo.setPinRequired(true);
        vo.setPinMustChange(false);
        SysSignCert usable = sysSignCertMapper.selectActiveByEmp(empId, TimeUtil.nowSeconds());
        if (usable != null) {
            vo.setSignable(true);
            vo.setCertId(usable.getId());
            vo.setCertNo(usable.getCertNo());
            vo.setCertStatus(usable.getCertStatus());
            vo.setCertStatusText(CertStatusEnum.getLabel(usable.getCertStatus()));
            vo.setValidTo(usable.getValidTo());
            vo.setCertStateText("证书 " + usable.getCertNo() + " 有效");
            // 初始口令签不了、也开不了免输入窗口：必须让界面弹框，走「先改口令再签名」
            vo.setPinMustChange(isInitialPin(usable));
            vo.setPinRequired(Boolean.TRUE.equals(vo.getPinMustChange()) || effectivePinTtlMinutes() <= 0
                    || !signPinSessionCache.isActive(usable.getEmpId(), usable.getId()));
            return vo;
        }
        // 没有可用证书：区分「没办过」「办了还没发下来」「过期」「被吊销」，四种要给的话不一样
        SysSignCert latest = sysSignCertMapper.selectLatestByEmp(empId);
        if (latest == null) {
            vo.setCertStateText("未办理签名证书");
            vo.setCheckMessage("您名下还没有电子签名证书，本次业务无法完成电子签名。"
                    + "请先按纸质流程手写签名并交病案管理岗核验，同时联系信息科尽快为您办理签名证书。");
            return vo;
        }
        vo.setCertNo(latest.getCertNo());
        vo.setCertStatus(latest.getCertStatus());
        vo.setCertStatusText(CertStatusEnum.getLabel(latest.getCertStatus()));
        vo.setValidTo(latest.getValidTo());
        if (Objects.equals(CertStatusEnum.REVOKED.getCode(), latest.getCertStatus())) {
            vo.setCertRevoked(true);
            vo.setCertStateText("证书 " + latest.getCertNo() + " 已吊销");
            vo.setCheckMessage("您的签名证书 " + latest.getCertNo() + " 已被吊销"
                    + (TextUtil.hasText(latest.getRevokeReason()) ? "（" + latest.getRevokeReason() + "）" : "")
                    + "，电子签名能力已废止，本次业务不能提交。"
                    + "请携带有效证件到信息科重新办理签名证书后再操作。");
            return vo;
        }
        boolean expired = latest.getValidTo() != null && latest.getValidTo().isBefore(TimeUtil.nowSeconds());
        vo.setCertExpired(expired);
        if (expired) {
            String validToText = latest.getValidTo().format(DateFormats.DATETIME);
            vo.setCertStateText("证书 " + latest.getCertNo() + " 已于 " + validToText + " 过期");
            vo.setCheckMessage("您的签名证书 " + latest.getCertNo() + " 已于 " + validToText
                    + " 过期，不能用于新的签名。本次请先按纸质流程手写签名并交病案管理岗核验，"
                    + "并尽快到信息科换发新证书。");
            return vo;
        }
        vo.setCertStateText("证书 " + latest.getCertNo() + " "
                + CertStatusEnum.getLabel(latest.getCertStatus(), "状态未知"));
        vo.setCheckMessage("您的签名证书 " + latest.getCertNo() + " 尚未完成签发（当前状态："
                + CertStatusEnum.getLabel(latest.getCertStatus(), "状态未知") + "），暂时不能用于电子签名。"
                + "本次请先按纸质流程手写签名并交病案管理岗核验，签发完成后即可正常电子签名。");
        return vo;
    }

    @Override
    public IPage<SignCertVO> listPage(SignCertPageQueryDTO query) {
        LambdaQueryWrapper<SysSignCert> w = new LambdaQueryWrapper<>();
        w.eq(query.getEmpId() != null, SysSignCert::getEmpId, query.getEmpId());
        w.eq(query.getCertStatus() != null, SysSignCert::getCertStatus, query.getCertStatus());
        w.eq(query.getIssuedMode() != null, SysSignCert::getIssuedMode, query.getIssuedMode());
        if (TextUtil.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            w.and(x -> x.like(SysSignCert::getCertNo, kw)
                    .or().like(SysSignCert::getEmpName, kw)
                    .or().like(SysSignCert::getKeyFingerprint, kw));
        }
        w.orderByDesc(SysSignCert::getId);

        IPage<SysSignCert> page = sysSignCertMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), w);
        Page<SignCertVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<SignCertVO> rows = new ArrayList<>(page.getRecords().size());
        for (SysSignCert c : page.getRecords()) {
            rows.add(toVO(c, false));
        }
        result.setRecords(rows);
        return result;
    }

    @Override
    public List<SignCertVO> selectList(String keyword) {
        LambdaQueryWrapper<SysSignCert> w = new LambdaQueryWrapper<>();
        w.eq(SysSignCert::getCertStatus, CertStatusEnum.ACTIVE.getCode());
        if (TextUtil.hasText(keyword)) {
            String kw = keyword.trim();
            w.and(x -> x.like(SysSignCert::getCertNo, kw).or().like(SysSignCert::getEmpName, kw));
        }
        w.orderByDesc(SysSignCert::getId).last("LIMIT 50");
        List<SignCertVO> list = new ArrayList<>();
        for (SysSignCert c : sysSignCertMapper.selectList(w)) {
            list.add(toVO(c, false));
        }
        return list;
    }

    @Override
    public String privatePemOf(SysSignCert cert) {
        if (cert == null) {
            throw new BusinessException("证书不存在");
        }
        if (Objects.equals(CertStatusEnum.REVOKED.getCode(), cert.getCertStatus())) {
            throw new BusinessException("证书 " + cert.getCertNo() + " 已吊销，不能用于签名");
        }
        if (Objects.equals(CertStatusEnum.PENDING_CA.getCode(), cert.getCertStatus())) {
            throw new BusinessException("证书 " + cert.getCertNo() + " 尚未完成 CA 签发（当前状态："
                    + CertStatusEnum.getLabel(cert.getCertStatus()) + "）；"
                    + "请先在「签名证书」刷新 CA 申请状态，或完成线下送 CA 回填后才能用于签名");
        }
        LocalDateTime now = TimeUtil.nowSeconds();
        if (cert.getValidTo() != null && cert.getValidTo().isBefore(now)) {
            throw new BusinessException("证书 " + cert.getCertNo() + " 已于 " + cert.getValidTo()
                    + " 过期，不能用于签名；请重新签发");
        }
        return keyProtectorUtil.unprotect(cert.getProtectedPrivateKey(), cert.getKeySalt(),
                cert.getKeyIterations() == null ? signProperties.getIterations() : cert.getKeyIterations());
    }

    /**
     * 软签签名：先过签名人确认这一道，再解密私钥完成签名。
     *
     * <p>PIN 是软签的「签名人专有」替身——UKey 时代由芯片内的 PIN 保证「签署时仅由签名人控制」，
     * 无硬件时用口令把这一步补上：口令不入库，只存 PBKDF2 哈希。
     * 口令只在输入当场使用，验证通过后开一段免输入窗口（系统参数 sign.pin.ttl_minutes），
     * 窗口内继续签名不必反复输口令，签一次顺延一次。
     *
     * @param cert    证书行
     * @param content 待签内容
     * @param pin     签名人本次输入的 PIN；为空时凭免输入窗口放行
     * @return Base64 签名值
     */
    @Override
    public String signWithPin(SysSignCert cert, String content, String pin) {
        if (cert == null) {
            throw new BusinessException("证书不存在");
        }
        assertPinConfirmed(cert, pin);
        String privatePem = privatePemOf(cert);
        return SignCryptoUtil.sign(privatePem, content);
    }

    /**
     * 签名人确认：PIN 校验通过，或落在本人近期的 PIN 免输入窗口内；两条都不满足就拒绝签名。
     *
     * <p>校验先于私钥解密：口令不对就不碰私钥。通过后统一顺延窗口——所以「窗口内又签了一笔」
     * 也算一次确认，医生连续接诊不必反复输口令。
     */
    private void assertPinConfirmed(SysSignCert cert, String pin) {
        int ttlMinutes = effectivePinTtlMinutes();
        if (TextUtil.hasText(pin)) {
            if (SignConst.INITIAL_SIGN_PIN.equals(pin)) {
                // 初始口令是全网已知的过渡值，拿它签等于没有二次确认；先拒再判哈希，不 renew 窗口
                throw new BusinessException("签名 PIN 仍是系统下发的初始口令，请先修改为自己的签名 PIN");
            }
            boolean pinOk = signPinUtil.verify(pin, cert.getPinSalt(),
                    cert.getPinIterations() == null ? signPinUtil.defaultIterations() : cert.getPinIterations(),
                    cert.getPinHash());
            if (!pinOk) {
                log.warn("签名 PIN 校验失败 certNo={} empId={}", cert.getCertNo(), cert.getEmpId());
                throw new BusinessException("签名 PIN 错误，无法完成签名");
            }
            signPinSessionCache.renew(cert.getEmpId(), cert.getId(), ttlMinutes);
            return;
        }
        // 窗口按「分钟」计，配 0 就是关掉免输入：先判时长，避免残留的窗口键继续放行
        if (ttlMinutes > 0 && signPinSessionCache.isActive(cert.getEmpId(), cert.getId())) {
            if (isInitialPin(cert)) {
                throw new BusinessException("签名 PIN 仍是系统下发的初始口令，请先修改为自己的签名 PIN");
            }
            signPinSessionCache.renew(cert.getEmpId(), cert.getId(), ttlMinutes);
            return;
        }
        throw new BusinessException("请输入签名 PIN：软签要求签名由签名人本人确认");
    }

    /**
     * 这张证书的 PIN 哈希是否还是建号下发的初始口令（口令从未被本人换过）。
     */
    private boolean isInitialPin(SysSignCert cert) {
        return TextUtil.hasText(cert.getPinHash())
                && signPinUtil.verify(SignConst.INITIAL_SIGN_PIN, cert.getPinSalt(),
                cert.getPinIterations() == null ? signPinUtil.defaultIterations() : cert.getPinIterations(),
                cert.getPinHash());
    }

    /**
     * 换掉本人的签名 PIN：先验当前口令，再用新口令重算哈希。
     *
     * <p>明文口令只在这次调用里用：既不入库、不进日志，也不回显；库里只换 PBKDF2 哈希与派生参数。
     * 新口令不许等于旧口令，也不许等于系统下发的初始口令——那等于没换。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeOwnPin(SignPinChangeDTO dto) {
        CurrentUser current = UserUtils.getCurrentUser();
        Long empId = current == null ? null : current.getEmployeeId();
        if (empId == null) {
            throw new BusinessException("未取到当前登录人的员工身份，不能修改签名 PIN");
        }
        SysSignCert cert = sysSignCertMapper.selectActiveByEmp(empId, TimeUtil.nowSeconds());
        if (cert == null) {
            // 只查可用证书：没办过证、已吊销、已过期都走这里——换 PIN 的前提是这张证还能签
            throw new BusinessException("您名下没有可用签名证书，请先办理签名证书再设置签名 PIN");
        }
        if (SignConst.INITIAL_SIGN_PIN.equals(dto.getNewPin())) {
            throw new BusinessException("新签名 PIN 不能是系统下发的初始口令");
        }
        if (dto.getNewPin().equals(dto.getOldPin())) {
            throw new BusinessException("新签名 PIN 不能与当前 PIN 相同");
        }
        int iterations = cert.getPinIterations() == null
                ? signPinUtil.defaultIterations() : cert.getPinIterations();
        if (!signPinUtil.verify(dto.getOldPin(), cert.getPinSalt(), iterations, cert.getPinHash())) {
            log.warn("修改签名 PIN 失败：当前 PIN 校验不通过 certNo={} empId={}", cert.getCertNo(), empId);
            throw new BusinessException("当前签名 PIN 不正确，不能修改");
        }
        String pinSalt = signPinUtil.newSalt();
        cert.setPinSalt(pinSalt);
        cert.setPinIterations(iterations);
        cert.setPinHash(signPinUtil.hash(dto.getNewPin(), pinSalt, iterations));
        sysSignCertMapper.updateById(cert);
        // 只留「谁改过口令」这一事实，改成了什么绝不落日志
        log.info("修改签名 PIN 成功 certNo={} empId={}", cert.getCertNo(), empId);
    }

    /**
     * PIN 免输入窗口时长：取系统参数 sign.pin.ttl_minutes，未配置按代码默认 30 分钟；
     * 配 0 表示每次签名都要本人重新输入 PIN。
     */
    private int effectivePinTtlMinutes() {
        Integer cfg = intValue(signConfigMapper.selectValue(SystemConfigKeyConst.SIGN_PIN_TTL_MINUTES));
        if (cfg == null || cfg < 0) {
            return DEFAULT_PIN_TTL_MINUTES;
        }
        return cfg;
    }

    @Override
    public long countByStatus(Integer certStatus) {
        return sysSignCertMapper.selectCount(new LambdaQueryWrapper<SysSignCert>()
                .eq(SysSignCert::getCertStatus, certStatus));
    }

    // 私有辅助

    @Override
    public long countAutoIssued() {
        return sysSignCertMapper.selectCount(new LambdaQueryWrapper<SysSignCert>()
                .eq(SysSignCert::getIssuedMode, CertIssuedModeEnum.AUTO.getCode()));
    }

    @Override
    public long countActiveEmployees() {
        List<SysSignCert> list = sysSignCertMapper.selectList(new LambdaQueryWrapper<SysSignCert>()
                .select(SysSignCert::getEmpId)
                .eq(SysSignCert::getCertStatus, CertStatusEnum.ACTIVE.getCode()));
        return list.stream().map(SysSignCert::getEmpId).filter(Objects::nonNull).distinct().count();
    }

    /**
     * 入参优先，其次系统参数，最后取配置 his.sign.default-valid-days；未配置即报错，不内联兜底值
     */
    private int effectiveValidDays(Integer fromDto) {
        if (fromDto != null && fromDto > 0) {
            return fromDto;
        }
        Integer cfg = intValue(signConfigMapper.selectValue(SystemConfigKeyConst.SIGN_CERT_VALID_DAYS));
        if (cfg != null && cfg > 0) {
            return cfg;
        }
        int days = signProperties.getDefaultValidDays();
        if (days <= 0) {
            throw new BusinessException("未配置证书默认有效期（his.sign.default-valid-days）");
        }
        return days;
    }

    private String nextCertNo() {
        return CERT_NO_PREFIX + LocalDate.now().format(DateFormats.COMPACT_DATE)
                + String.format("%04d", redisSequenceService.next("CERT"));
    }

    /**
     * 生成申请单视图：在线申请带回 CA 申请单号与状态，未接入时给出线下送 CA 的办理指引。
     */
    private SignCaApplyVO toApplyVO(SysSignCert c) {
        SignCaApplyVO vo = new SignCaApplyVO();
        vo.setCertId(c.getId());
        vo.setCertNo(c.getCertNo());
        vo.setEmpName(c.getEmpName());
        vo.setSubjectDn(subjectDnOf(c));
        vo.setPublicKeyPem(c.getPublicKey());
        vo.setKeyFingerprintGroups(SignCryptoUtil.fingerprintGroups(c.getKeyFingerprint()));
        vo.setValidDays(c.getValidTo() != null && c.getValidFrom() != null
                ? (int) java.time.Duration.between(c.getValidFrom(), c.getValidTo()).toDays() : null);
        vo.setCertStatus(c.getCertStatus());
        vo.setCertStatusText(CertStatusEnum.getLabel(c.getCertStatus()));
        vo.setOnline(TextUtil.hasText(c.getCaApplyStatus()));
        vo.setApplyNo(c.getCaApplyNo());
        vo.setApplyStatus(c.getCaApplyStatus());
        vo.setApplyStatusText(CaApplyStatusEnum.getLabel(c.getCaApplyStatus(), c.getCaApplyStatus()));
        vo.setApplyMessage(c.getCaLastMessage());
        vo.setTemplateCode(TextUtil.hasText(c.getCaApplyStatus()) ? signProperties.getCa().getTemplateCode() : null);
        if (Objects.equals(CertStatusEnum.ACTIVE.getCode(), c.getCertStatus())) {
            vo.setGuide("证书已由 CA 签发并生效，可用于签名；私钥始终留在本系统");
        } else if (CaApplyStatusEnum.PENDING.getCode().equals(c.getCaApplyStatus())
                || CaApplyStatusEnum.APPROVED.getCode().equals(c.getCaApplyStatus())) {
            vo.setGuide("申请单已提交 CA（" + c.getCaApplyNo() + "），当前状态："
                    + CaApplyStatusEnum.getLabel(c.getCaApplyStatus()) + "；CA 审核签发完成后点「刷新状态」，"
                    + "系统会自动下载证书链并让证书生效");
        } else if (CaApplyStatusEnum.ISSUED.getCode().equals(c.getCaApplyStatus())) {
            vo.setGuide("CA 已签发，但证书尚未回填生效：" + (c.getCaLastMessage() == null ? "" : c.getCaLastMessage())
                    + "；请点「刷新状态」重试自动下载");
        } else if (CaApplyStatusEnum.FAILED.getCode().equals(c.getCaApplyStatus())) {
            vo.setGuide("提交 CA 在线申请失败：" + (c.getCaLastMessage() == null ? "原因未知" : c.getCaLastMessage())
                    + "；确认 CA 地址/凭证/模板后点「重试提交」，或改用线下送 CA 回填");
        } else if (CaApplyStatusEnum.REJECTED.getCode().equals(c.getCaApplyStatus())
                || CaApplyStatusEnum.CANCELLED.getCode().equals(c.getCaApplyStatus())) {
            vo.setGuide("CA 申请单" + CaApplyStatusEnum.getLabel(c.getCaApplyStatus())
                    + (c.getCaLastMessage() == null ? "" : "：" + c.getCaLastMessage())
                    + "；可修正后「重新提交」（生成新申请单），或改用线下送 CA 回填");
        } else {
            vo.setGuide("线下签发流程：① 把下方 Subject DN 与公钥 PEM 送 CA 认证中心人工签发；"
                    + "② CA 返回证书 PEM 后，在证书办理页用 certNo=" + c.getCertNo() + " 回填证书；"
                    + "③ 回填后证书方可用于签名。私钥全程留在本系统，未曾送出。");
        }
        return vo;
    }

    /**
     * 工号回查（RA 申请与 Subject DN OU 取用）；员工已删除时返回 null，不阻塞证书展示
     */
    private String empCodeOf(Long empId) {
        if (empId == null) {
            return null;
        }
        SysEmployee emp = sysEmployeeMapper.selectById(empId);
        return emp == null ? null : emp.getEmpCode();
    }

    private SignCertVO toVO(SysSignCert c, boolean withPublicKey) {
        SignCertVO vo = new SignCertVO();
        vo.setId(c.getId());
        vo.setCertNo(c.getCertNo());
        vo.setEmpId(c.getEmpId());
        vo.setEmpName(c.getEmpName());
        vo.setEmpCode(empCodeOf(c.getEmpId()));
        vo.setDeptId(c.getDeptId());
        vo.setDeptName(c.getDeptName());
        vo.setKeyAlgo(c.getKeyAlgo());
        vo.setDigestAlgo(SignCryptoUtil.digestAlgoFor(c.getKeyAlgo()));
        vo.setSignAlgo(SignCryptoUtil.signAlgoFor(c.getKeyAlgo()));
        vo.setKeyFingerprint(c.getKeyFingerprint());
        vo.setKeyFingerprintGroups(SignCryptoUtil.fingerprintGroups(c.getKeyFingerprint()));
        vo.setCaSubjectDn(subjectDnOf(c));
        vo.setCaSerial(c.getCaSerial());
        vo.setCaApplyNo(c.getCaApplyNo());
        vo.setCaApplyStatus(c.getCaApplyStatus());
        vo.setCaApplyStatusText(CaApplyStatusEnum.getLabel(c.getCaApplyStatus(), c.getCaApplyStatus()));
        vo.setCaLastMessage(c.getCaLastMessage());
        vo.setIssuedMode(c.getIssuedMode());
        vo.setIssuedModeText(CertIssuedModeEnum.getLabel(c.getIssuedMode()));
        vo.setCertStatus(c.getCertStatus());
        vo.setCertStatusText(CertStatusEnum.getLabel(c.getCertStatus()));
        vo.setValidFrom(c.getValidFrom());
        vo.setValidTo(c.getValidTo());
        vo.setRevokeReason(c.getRevokeReason());
        vo.setRevokeTime(c.getRevokeTime());
        vo.setRevokeByName(c.getRevokeByName());
        vo.setLastUsedTime(c.getLastUsedTime());
        vo.setSignCount(c.getSignCount());
        if (withPublicKey) {
            vo.setPublicKey(c.getPublicKey());
        }
        boolean expired = c.getValidTo() != null && c.getValidTo().isBefore(TimeUtil.nowSeconds());
        vo.setExpired(expired);
        boolean revoked = Objects.equals(CertStatusEnum.REVOKED.getCode(), c.getCertStatus());
        vo.setCanRevoke(!revoked);
        if (revoked) {
            vo.setActionHint("已吊销（" + c.getRevokeReason() + "），不可再用；如需继续签名请重新签发");
        } else if (expired) {
            vo.setActionHint("证书已过有效期（" + c.getValidTo() + "），不可用于新的签名；"
                    + "历史签名仍可用本证书公钥验签，不要吊销它");
        } else {
            vo.setActionHint(null);
        }
        return vo;
    }
}
