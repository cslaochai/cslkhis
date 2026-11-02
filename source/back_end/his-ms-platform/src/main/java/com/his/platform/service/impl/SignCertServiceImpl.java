package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.his.spi.dto.SignCaPushDTO;
import com.his.spi.dto.SignCertBindDTO;
import com.his.spi.dto.SignCertKeygenDTO;
import com.his.spi.dto.SignCertRevokeDTO;
import com.his.spi.dto.SignPinChangeDTO;
import com.his.spi.vo.SignCaApplyRecordVO;
import com.his.spi.vo.SignCaApplyVO;
import com.his.spi.vo.SignCaStatusVO;
import com.his.spi.vo.SignCaPushResultVO;
import com.his.spi.vo.SignPreCheckVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    // ---------------- CA 在线申请 ----------------

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
            SysSignCaApply latestApply = signCaApplyService.latestOf(pending.getId());
            String applyState = latestApply != null && TextUtil.hasText(latestApply.getApplyNo())
                    ? "已提交 CA 申请（申请单 " + latestApply.getApplyNo() + "，当前状态："
                    + CaApplyStatusEnum.getLabel(latestApply.getApplyStatus(), latestApply.getApplyStatus()) + "）"
                    : "已生成密钥对，尚未完成 CA 签发";
            throw new BusinessException("员工「" + (TextUtil.hasText(empName) ? empName : empId)
                    + "」的签名证书 " + pending.getCertNo() + " " + applyState
                    + "；CA 审核签发后会主动推送回 HIS 并自动回填生效，无需人工刷新，签发完成后再签名");
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
        sysSignCertMapper.updateById(cert);
        log.info("回填 CA 签发证书 certNo={} empId={} 序列号={} 有效期至={}",
                cert.getCertNo(), cert.getEmpId(), cert.getCaSerial(), cert.getValidTo());
        return toVO(cert, true);
    }

    /**
     * 上传 CA 系统「导出ZIP」得到的证书压缩包（未加密）回填证书：
     * 解包后挑出公钥与本地密钥对一致的那张证书，复用 bindCertificate 校验并生效。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignCertVO bindFile(Long certId, byte[] fileBytes, Long operatorId, String operatorName) {
        if (certId == null) {
            throw new BusinessException("证书ID不能为空");
        }
        SysSignCert cert = sysSignCertMapper.selectById(certId);
        if (cert == null) {
            throw new BusinessException("证书不存在");
        }
        if (!Integer.valueOf(CertStatusEnum.PENDING_CA.getCode()).equals(cert.getCertStatus())) {
            throw new BusinessException("证书 " + cert.getCertNo() + " 当前状态为「"
                    + CertStatusEnum.getLabel(cert.getCertStatus()) + "」，只有「待CA签发」的证书才可回填；"
                    + "如需换证请先生成新的密钥对");
        }
        if (fileBytes == null || fileBytes.length == 0) {
            throw new BusinessException("上传文件内容为空");
        }
        java.security.cert.X509Certificate x509 = extractCertFromFile(fileBytes, cert);
        bindCertificate(cert, PemCodecUtil.toPem(x509),
                "CA 线下签发，证书由管理员上传 CA 导出的证书文件回填（私钥本地生成并加密托管，未出 HIS）");
        // 线下回填同样给最近一单打上「回填生效」的时刻；从没向 CA 送过单（纯人工流程）就没有流水行可记
        SysSignCaApply apply = signCaApplyService.latestOf(cert.getId());
        if (apply != null && TextUtil.hasText(apply.getApplyNo())) {
            signCaApplyService.markBound(apply, issuerOf(x509));
        }
        sysSignCertMapper.updateById(cert);
        log.info("回填 CA 签发证书(压缩包) certNo={} empId={} 序列号={} 有效期至={}",
                cert.getCertNo(), cert.getEmpId(), cert.getCaSerial(), cert.getValidTo());
        return toVO(cert, true);
    }

    /**
     * 从上传文件里挑出公钥与本地密钥对一致的那张证书。
     *
     * <p>按内容嗅探而不是按扩展名硬判：
     * <ul>
     *   <li>ZIP（PK 魔数，CA「导出ZIP」产物，<b>不加密、不带口令</b>）→ 用 JDK ZipInputStream 解包后逐个条目解析；</li>
     *   <li>证书文件本身（.pem/.crt/.cer/.der，常带整条链）→ 直接解析。</li>
     * </ul>
     * 解析出的可能是证书链（终端实体证 + 签发 CA 证），只有终端实体证公钥才与本地一致。
     */
    private java.security.cert.X509Certificate extractCertFromFile(byte[] data, SysSignCert cert) {
        List<java.security.cert.X509Certificate> certs = isZip(data)
                ? readCertsFromZip(data)
                : parseCertsAny(data);
        if (certs.isEmpty()) {
            throw new BusinessException("文件内容里未找到任何证书（支持 CA 导出的 .pem/.crt/.cer/.der，或 CA「导出ZIP」产出的证书压缩包）");
        }
        // 优先取公钥与本地密钥对一致的那张
        String localPub = cert.getPublicKey();
        for (java.security.cert.X509Certificate x : certs) {
            if (PemCodecUtil.toPem(x.getPublicKey()).trim().equals(localPub.trim())) {
                return x;
            }
        }
        if (certs.size() == 1) {
            // 仅一张但公钥不匹配：交给 bindCertificate 给出精确的「公钥不一致」报错
            return certs.get(0);
        }
        throw new BusinessException("文件里未找到与本地密钥对公钥一致的证书（共解析到 " + certs.size()
                + " 张，多为签发 CA 的证书）；请确认这是 CA 为本证书签发的那一份，而不是只下了 CA 根证");
    }

    /**
     * 按 ZIP 魔数判定：PK\x03\x04（普通）/ PK\x05\x06（空归档）/ PK\x07\x08（跨卷）。
     */
    private boolean isZip(byte[] data) {
        return data.length > 3
                && data[0] == 'P' && data[1] == 'K'
                && (data[2] == 3 || data[2] == 5 || data[2] == 7);
    }

    /**
     * 解压压缩包并把包内每个条目都试着解析成证书（跳过非证书条目，如说明文件）。
     *
     * <p>CA「导出ZIP」产物是不加密的普通 ZIP，JDK 自带 java.util.zip 直接读，全程在内存流里完成、不落临时文件。
     * 不接受也不处理加密包：带口令的压缩包请改用 CA「下载证书」拿到的 .pem，或让 CA 导出未加密包。
     */
    private List<java.security.cert.X509Certificate> readCertsFromZip(byte[] zipBytes) {
        List<java.security.cert.X509Certificate> certs = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                certs.addAll(parseCertsAny(zis.readAllBytes()));
            }
        } catch (java.util.zip.ZipException e) {
            // 加密条目 / 结构损坏
            throw new BusinessException("压缩包解析失败（文件损坏，或压缩包是加密的——CA「导出ZIP」产物不带密码）：" + e.getMessage());
        } catch (java.io.IOException e) {
            throw new BusinessException("压缩包读取失败：" + e.getMessage());
        }
        return certs;
    }

    /**
     * 把一段内容解析成 X.509 证书列表：PEM 里可能连着好几张（终端实体证 + 签发 CA 证的证书链），
     * DER 只有一张；不是证书则返回空列表（调用方跳过）。
     */
    private List<java.security.cert.X509Certificate> parseCertsAny(byte[] data) {
        if (data == null || data.length == 0) {
            return List.of();
        }
        List<java.security.cert.X509Certificate> out = new ArrayList<>();
        try {
            java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509",
                    new org.bouncycastle.jce.provider.BouncyCastleProvider());
            for (java.security.cert.Certificate c : cf.generateCertificates(new ByteArrayInputStream(data))) {
                if (c instanceof java.security.cert.X509Certificate x) {
                    out.add(x);
                }
            }
        } catch (Exception ignored) {
            // 非证书内容（说明文本、无关文件）：跳过，不算错误
        }
        return out;
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
        // 已提交且仍在 CA 审核/待签发：状态由 CA 推送，不重复送单（HIS 无查询权限）
        SysSignCaApply latest = signCaApplyService.latestOf(cert.getId());
        boolean waiting = latest != null && TextUtil.hasText(latest.getApplyNo())
                && (CaApplyStatusEnum.PENDING.getCode().equals(latest.getApplyStatus())
                || CaApplyStatusEnum.APPROVED.getCode().equals(latest.getApplyStatus()));
        if (!waiting) {
            submitToCa(cert, true, operatorName);
        }
        return toApplyVO(cert);
    }

    /**
     * 接收 CA 推送的申请单状态变更（HIS 唯一的入站入口）。
     *
     * <p>幂等与状态机：同一状态重复推送按重放处理、不重复回填；只允许
     * PENDING→APPROVED/REJECTED/ISSUED/CANCELLED、APPROVED→REJECTED/ISSUED/CANCELLED，
     * 终态（ISSUED/REJECTED/CANCELLED）不再接受其他状态，杜绝状态回退与跳变。
     * ISSUED 必须随报文带证书 PEM，校验公钥与本地密钥对一致后回填生效。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignCaPushResultVO applyCaPush(SignCaPushDTO dto) {
        if (dto == null || !TextUtil.hasText(dto.getApplyNo())) {
            throw new BusinessException("CA 推送缺少申请单号 applyNo");
        }
        CaApplyStatusEnum target = CaApplyStatusEnum.fromCode(dto.getStatus());
        if (target == null || target == CaApplyStatusEnum.FAILED || target == CaApplyStatusEnum.PENDING) {
            throw new BusinessException("CA 推送状态不合法：" + dto.getStatus()
                    + "（只接受 APPROVED / REJECTED / ISSUED / CANCELLED）");
        }
        SysSignCaApply apply = findApplyForPush(dto);
        CaApplyStatusEnum current = CaApplyStatusEnum.fromCode(apply.getApplyStatus());

        SignCaPushResultVO result = new SignCaPushResultVO();
        result.setAccepted(true);
        result.setApplyStatus(target.getCode());

        // 幂等：同一状态重复推送（含 CA 重试），不做任何变更；ISSUED 允许补一次未完成的证书回填
        if (target == current) {
            result.setIdempotentReplay(true);
            result.setMessage("同一状态重复推送，按幂等重放处理");
            if (target == CaApplyStatusEnum.ISSUED) {
                bindIfNeeded(apply, dto, result);
            }
            return result;
        }
        if (!transitionAllowed(current, target)) {
            throw new BusinessException("CA 推送状态流转非法：" + label(current) + " → " + label(target)
                    + "（申请单 " + apply.getApplyNo() + "）");
        }

        apply.setApplyStatus(target.getCode());
        apply.setCaMessage(statusMessage(dto));
        apply.setReviewedAt(dto.getReviewedAt());
        apply.setIssuedAt(dto.getIssuedAt());
        signCaApplyService.update(apply);

        if (target == CaApplyStatusEnum.ISSUED) {
            bindIfNeeded(apply, dto, result);
        }
        log.info("CA 推送状态 applyNo={} {} → {}（event={}）",
                apply.getApplyNo(), label(current), label(target), dto.getEventId());
        return result;
    }

    /**
     * 状态机：只允许单向推进；ISSUED/REJECTED/CANCELLED 为终态。
     */
    private static boolean transitionAllowed(CaApplyStatusEnum current, CaApplyStatusEnum target) {
        if (current == null) {
            return target == CaApplyStatusEnum.APPROVED;
        }
        return switch (current) {
            case PENDING -> target == CaApplyStatusEnum.APPROVED
                    || target == CaApplyStatusEnum.REJECTED
                    || target == CaApplyStatusEnum.ISSUED
                    || target == CaApplyStatusEnum.CANCELLED;
            case APPROVED -> target == CaApplyStatusEnum.REJECTED
                    || target == CaApplyStatusEnum.ISSUED
                    || target == CaApplyStatusEnum.CANCELLED;
            case FAILED -> target == CaApplyStatusEnum.APPROVED || target == CaApplyStatusEnum.REJECTED
                    || target == CaApplyStatusEnum.ISSUED;
            case ISSUED, REJECTED, CANCELLED -> false;
        };
    }

    private static String label(CaApplyStatusEnum status) {
        return status == null ? "未知" : status.getLabel();
    }

    private static String statusMessage(SignCaPushDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append(TextUtil.hasText(dto.getStatusText())
                ? dto.getStatusText() : CaApplyStatusEnum.getLabel(dto.getStatus(), dto.getStatus()));
        if (TextUtil.hasText(dto.getReviewedByName())) {
            sb.append("；审核人：").append(dto.getReviewedByName());
        }
        if (TextUtil.hasText(dto.getReviewComment())) {
            sb.append("；意见：").append(dto.getReviewComment());
        }
        return sb.toString();
    }

    /**
     * 按申请单号定位流水；找不到时回退按 externalRequestId 定位（兼容 CA 早于 HIS 落 applyNo 的推送）。
     */
    private SysSignCaApply findApplyForPush(SignCaPushDTO dto) {
        SysSignCaApply apply = signCaApplyService.findByApplyNoOrExternal(dto.getApplyNo(), dto.getExternalRequestId());
        if (apply == null) {
            throw new BusinessException("CA 推送的申请单在 HIS 不存在：" + dto.getApplyNo());
        }
        return apply;
    }

    /**
     * ISSUED：用推送报文里的证书 PEM 回填。已回填过则跳过；回填失败只记说明，状态仍按 ISSUED 留痕，可等 CA 重推。
     */
    private void bindIfNeeded(SysSignCaApply apply, SignCaPushDTO dto, SignCaPushResultVO result) {
        if (apply.getBoundAt() != null) {
            result.setMessage("证书已回填生效，无需重复回填");
            return;
        }
        SignCaPushDTO.Certificate certificate = dto.getCertificate();
        if (certificate == null || !TextUtil.hasText(certificate.getCertPem())) {
            result.setMessage("CA 已推送已签发，但未携带证书 PEM，暂无法回填");
            signCaApplyService.updateMessage(apply, result.getMessage());
            return;
        }
        SysSignCert cert = sysSignCertMapper.selectById(apply.getCertId());
        if (cert == null) {
            throw new BusinessException("申请单 " + apply.getApplyNo() + " 对应的证书不存在，无法回填");
        }
        try {
            java.security.cert.X509Certificate x509 = bindCertificate(cert, certificate.getCertPem(),
                    "CA 在线签发（申请单 " + apply.getApplyNo() + "，CA 推送）；私钥本地生成并加密托管，未出 HIS");
            signCaApplyService.markBound(apply, issuerOf(x509));
            signCaApplyService.updateMessage(apply, "CA 已签发并自动回填生效；证书序列号 " + cert.getCaSerial()
                    + "，有效期至 " + cert.getValidTo());
            sysSignCertMapper.updateById(cert);
            result.setMessage("证书已校验公钥并回填生效");
            log.info("CA 推送签发回填 certNo={} applyNo={} serial={}",
                    cert.getCertNo(), apply.getApplyNo(), cert.getCaSerial());
        } catch (RuntimeException e) {
            signCaApplyService.updateMessage(apply, "CA 已签发，但证书回填失败：" + e.getMessage()
                    + "；请核对公钥后等待 CA 重新推送");
            log.warn("CA 推送签发回填失败 certNo={} applyNo={} err={}",
                    apply.getCertNo(), apply.getApplyNo(), e.getMessage());
            result.setMessage("证书回填失败：" + e.getMessage());
        }
    }

    @Override
    public SignCaStatusVO caStatus() {
        SignProperties.Ca ca = signProperties.getCa();
        SignCaStatusVO vo = new SignCaStatusVO();
        vo.setTemplateCode(ca.getTemplateCode());
        vo.setKeyAlgorithm(signProperties.getKeyAlgorithm());
        vo.setAppId(TextUtil.hasText(ca.getAppId()) ? ca.getAppId() : null);
        vo.setBaseUrl(TextUtil.hasText(ca.getBaseUrl()) ? ca.getBaseUrl() : null);
        vo.setAppSecretConfigured(TextUtil.hasText(ca.getAppSecret()));
        if (!ca.isEnabled()) {
            vo.setCaMode("offline");
            vo.setAvailable(true);
            vo.setHint("CA 在线申请未启用：HIS 本地生成密钥对（私钥加密托管、不出 HIS），"
                    + "管理员把「CA 签发申请单」线下送 CA 认证中心人工签发，再回填证书");
            return vo;
        }
        vo.setCaMode("online");
        String missing = ca.missingCredentialParams();
        if (missing != null) {
            vo.setAvailable(false);
            vo.setHint("CA 在线申请已启用，但接入凭证未配置：" + missing
                    + "；请补全后再使用在线申请，配置前可继续走线下签发流程");
            return vo;
        }
        vo.setAvailable(true);
        vo.setHint("已接入 CA 在线申请：App ID " + ca.getAppId() + "，CA 地址 " + ca.getBaseUrl()
                + "，模板 " + ca.getTemplateCode()
                + "；HIS 只提交申请与吊销，审核/签发/撤销状态由 CA 主动推送，无需手动刷新");
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
     * 证书行不留申请单号与状态的副本，各阶段的时刻与说明一律以流水为准。
     *
     * @param throwOnError true=提交失败向上抛（页面重试路径）；false=只落 FAILED 状态
     */
    private void submitToCa(SysSignCert cert, boolean throwOnError, String operatorName) {
        SignProperties.Ca ca = signProperties.getCa();
        SysSignCaApply apply = null;
        try {
            signCaApiClient.requireEnabled();
            String externalRequestId = externalRequestIdOf(cert);
            String subjectDn = buildSubjectDn(cert.getEmpName(), empCodeOf(cert.getEmpId()));
            apply = signCaApplyService.start(cert, externalRequestId, ca.getTemplateCode(), subjectDn);

            SignCaApiClient.CaApplication app = signCaApiClient.submit(new SignCaApiClient.CaSubmitPayload(
                    externalRequestId, ca.getApplicantType(), cert.getEmpName(), subjectDn,
                    ca.getTemplateCode(), cert.getPublicKey(), null,
                    "HIS 电子签名证书 " + cert.getCertNo(),
                    String.valueOf(cert.getEmpId()), empCodeOf(cert.getEmpId()), cert.getDeptName()));

            String caMessage = statusMessage(app);
            cert.setIssuedMode(CertIssuedModeEnum.ONLINE.getCode());
            signCaApplyService.recordCaResult(apply, app, caMessage);
            if (CaApplyStatusEnum.ISSUED.getCode().equals(app.status())) {
                // CA 若在提交回执里直接带出已签发证书（极少数自动签发模板），当场按推送口径回填
                bindIfNeeded(apply, pushOf(app), new SignCaPushResultVO());
            }
            sysSignCertMapper.updateById(cert);
            log.info("提交 CA 在线申请 certNo={} applyNo={} status={} 操作人={}",
                    cert.getCertNo(), app.applyNo(), app.status(), operatorName);
        } catch (RuntimeException e) {
            // 失败原因只落流水：证书行没有申请列可写，也不该为一次失败的外网往返刷新证书行的审计时刻
            signCaApplyService.recordFailure(apply, e.getMessage());
            log.warn("CA 在线申请提交失败 certNo={} err={}", cert.getCertNo(), e.getMessage());
            if (throwOnError) {
                throw e;
            }
        }
    }

    /**
     * 提交回执里的申请单转成推送报文形状：只有 ISSUED 时带证书，其余状态只带状态与说明。
     */
    private static SignCaPushDTO pushOf(SignCaApiClient.CaApplication app) {
        SignCaPushDTO dto = new SignCaPushDTO();
        dto.setApplyNo(app.applyNo());
        dto.setExternalRequestId(app.externalRequestId());
        dto.setStatus(app.status());
        dto.setStatusText(app.statusText());
        dto.setReviewedByName(app.reviewedByName());
        dto.setReviewComment(app.reviewComment());
        dto.setReviewedAt(app.reviewedAt());
        dto.setIssuedAt(app.issuedAt());
        if (app.certificate() != null) {
            SignCaPushDTO.Certificate certificate = new SignCaPushDTO.Certificate();
            certificate.setSerialHex(app.certificate().serialHex());
            certificate.setSubjectDn(app.certificate().subjectDn());
            certificate.setIssuerDn(app.certificate().issuerDn());
            certificate.setAlgorithm(app.certificate().algorithm());
            certificate.setNotBefore(app.certificate().notBefore());
            certificate.setNotAfter(app.certificate().notAfter());
            certificate.setFingerprintSha256(app.certificate().fingerprintSha256());
            certificate.setCertPem(app.certificate().certPem());
            certificate.setChainPem(app.certificate().chainPem());
            dto.setCertificate(certificate);
        }
        return dto;
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
        String dn = buildSubjectDn(empName, empCodeOf(empId));

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
        SysSignCaApply latest = signCaApplyService.latestOf(cert.getId());
        String status = latest == null ? null : latest.getApplyStatus();
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
        return buildSubjectDn(cert.getEmpName(), empCodeOf(cert.getEmpId()));
    }

    /**
     * 签发该证书的 CA 名称（证书里的 Issuer DN）：回填时留进申请流水，事后能答「这张证是谁发的」。
     */
    private String issuerOf(java.security.cert.X509Certificate x509) {
        return x509.getIssuerX500Principal().getName(javax.security.auth.x500.X500Principal.RFC2253);
    }

    /**
     * CA 签发申请单上的 Subject DN：CN=姓名, O=机构, OU=员工编号（工号）
     */
    private String buildSubjectDn(String empName, String empCode) {
        String organization = signProperties.getOrganization();
        if (!TextUtil.hasText(organization)) {
            throw new BusinessException("未配置机构名（his.sign.organization），无法生成证书申请单主体 DN");
        }
        StringBuilder dn = new StringBuilder("CN=").append(empName)
                .append(", O=").append(organization);
        if (TextUtil.hasText(empCode)) {
            dn.append(", OU=").append(empCode);
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
        // 已由 CA 签发的证书必须同步吊销 CA 侧记录，否则 CA 那边仍是有效证书
        pushRevokeToCa(cert, dto.getReason());
        log.info("吊销签名证书 certNo={} empId={} 原因={} 操作人={}",
                cert.getCertNo(), cert.getEmpId(), dto.getReason(), operatorName);
        return toVO(cert, true);
    }

    /**
     * 吊销推送 CA：只有被 CA 签发过的证书（有序列号）才需要推；未启用在线申请或证书还没签发时跳过。
     * 推送失败直接抛出，本地吊销随事务回滚——不能让 HIS 显示已吊销而 CA 侧证书仍然有效。
     */
    private void pushRevokeToCa(SysSignCert cert, String reason) {
        if (!signProperties.getCa().isEnabled()) {
            log.info("CA 在线申请未启用，证书 {} 仅本地吊销", cert.getCertNo());
            return;
        }
        if (!TextUtil.hasText(cert.getCaSerial())) {
            log.info("证书 {} 尚未被 CA 签发（无序列号），无需推送吊销", cert.getCertNo());
            return;
        }
        signCaApiClient.revoke(cert.getCaSerial(), reason);
        SysSignCaApply latest = signCaApplyService.latestOf(cert.getId());
        if (latest != null) {
            signCaApplyService.updateMessage(latest, "HIS 已吊销证书并向 CA 推送吊销（原因："
                    + reason + "）；证书序列号 " + cert.getCaSerial());
        }
        log.info("已推送 CA 吊销 certNo={} serial={}", cert.getCertNo(), cert.getCaSerial());
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
        // 「CA 申请」列读各证书最新一条流水：一次批量取回，避免逐行 N+1
        List<Long> certIds = new ArrayList<>(page.getRecords().size());
        for (SysSignCert c : page.getRecords()) {
            certIds.add(c.getId());
        }
        Map<Long, SysSignCaApply> latestApplies = signCaApplyService.latestOf(certIds);
        List<SignCertVO> rows = new ArrayList<>(page.getRecords().size());
        for (SysSignCert c : page.getRecords()) {
            rows.add(toVO(c, latestApplies.get(c.getId()), false));
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
                    + "CA 签发后会主动推送状态并自动回填，或由管理员回填线下签发的证书后才能用于签名");
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
        // 申请单号 / 状态 / 说明以最新一条流水为准，证书行不留副本
        SysSignCaApply latest = signCaApplyService.latestOf(c.getId());
        String applyNo = latest == null ? null : latest.getApplyNo();
        String applyStatus = latest == null ? null : latest.getApplyStatus();
        String applyMessage = latest == null ? null : latest.getCaMessage();
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
        vo.setOnline(TextUtil.hasText(applyStatus));
        vo.setApplyNo(applyNo);
        vo.setApplyStatus(applyStatus);
        vo.setApplyStatusText(CaApplyStatusEnum.getLabel(applyStatus, applyStatus));
        vo.setApplyMessage(applyMessage);
        vo.setTemplateCode(TextUtil.hasText(applyStatus) ? signProperties.getCa().getTemplateCode() : null);
        if (Objects.equals(CertStatusEnum.ACTIVE.getCode(), c.getCertStatus())) {
            vo.setGuide("证书已由 CA 签发并生效，可用于签名；私钥始终留在本系统");
        } else if (CaApplyStatusEnum.PENDING.getCode().equals(applyStatus)
                || CaApplyStatusEnum.APPROVED.getCode().equals(applyStatus)) {
            vo.setGuide("申请单已提交 CA（" + applyNo + "），当前状态："
                    + CaApplyStatusEnum.getLabel(applyStatus)
                    + "；CA 审核签发后会主动推送状态到 HIS，已签发时自动校验公钥并回填生效，无需人工刷新");
        } else if (CaApplyStatusEnum.ISSUED.getCode().equals(applyStatus)) {
            vo.setGuide("CA 已签发，HIS 正在等待 CA 重新推送携带证书的报文完成自动回填："
                    + (applyMessage == null ? "" : applyMessage)
                    + "；如长时间未生效，请联系 CA 管理员重新推送");
        } else if (CaApplyStatusEnum.FAILED.getCode().equals(applyStatus)) {
            vo.setGuide("提交 CA 在线申请失败：" + (applyMessage == null ? "原因未知" : applyMessage)
                    + "；确认 CA 地址/凭证/模板后点「重试提交」，或改用线下送 CA 回填");
        } else if (CaApplyStatusEnum.REJECTED.getCode().equals(applyStatus)
                || CaApplyStatusEnum.CANCELLED.getCode().equals(applyStatus)) {
            vo.setGuide("CA 申请单" + CaApplyStatusEnum.getLabel(applyStatus)
                    + (applyMessage == null ? "" : "：" + applyMessage)
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
        return toVO(c, signCaApplyService.latestOf(c.getId()), withPublicKey);
    }

    /**
     * @param latestApply 该证书最新一条 CA 申请流水（列表页批量预取后传入；从未发起过在线申请为 null）
     */
    private SignCertVO toVO(SysSignCert c, SysSignCaApply latestApply, boolean withPublicKey) {
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
        vo.setCaApplyNo(latestApply == null ? null : latestApply.getApplyNo());
        vo.setCaApplyStatus(latestApply == null ? null : latestApply.getApplyStatus());
        vo.setCaApplyStatusText(latestApply == null ? null
                : CaApplyStatusEnum.getLabel(latestApply.getApplyStatus(), latestApply.getApplyStatus()));
        vo.setCaLastMessage(latestApply == null ? null : latestApply.getCaMessage());
        vo.setIssuedMode(c.getIssuedMode());
        vo.setIssuedModeText(CertIssuedModeEnum.getLabel(c.getIssuedMode()));
        vo.setCertStatus(c.getCertStatus());
        vo.setCertStatusText(CertStatusEnum.getLabel(c.getCertStatus()));
        vo.setValidFrom(c.getValidFrom());
        vo.setValidTo(c.getValidTo());
        // 有效期来源：只有「待 CA 签发」时库里的值才是申请预估；一旦回填（ACTIVE/REVOKED）
        // 就是证书文件 notBefore/notAfter 的真值
        vo.setValidityEstimated(Objects.equals(CertStatusEnum.PENDING_CA.getCode(), c.getCertStatus()));
        vo.setRevokeReason(c.getRevokeReason());
        vo.setRevokeTime(c.getRevokeTime());
        vo.setRevokeByName(c.getRevokeByName());
        vo.setLastUsedTime(c.getLastUsedTime());
        vo.setSignCount(c.getSignCount());
        if (withPublicKey) {
            vo.setPublicKey(c.getPublicKey());
        }
        // 待 CA 签发的证书，库里的有效期只是申请预估，不能据此判「已过期」
        boolean pendingCa = Objects.equals(CertStatusEnum.PENDING_CA.getCode(), c.getCertStatus());
        boolean expired = !pendingCa && c.getValidTo() != null && c.getValidTo().isBefore(TimeUtil.nowSeconds());
        vo.setExpired(expired);
        boolean revoked = Objects.equals(CertStatusEnum.REVOKED.getCode(), c.getCertStatus());
        vo.setCanRevoke(!revoked);
        if (revoked) {
            vo.setActionHint("已吊销（" + c.getRevokeReason() + "），不可再用；如需继续签名请重新签发");
        } else if (expired) {
            vo.setActionHint("证书已过有效期（" + c.getValidTo() + "），不可用于新的签名；"
                    + "历史签名仍可用本证书公钥验签，不要吊销它");
        } else if (pendingCa) {
            vo.setActionHint("待 CA 签发：有效期以 CA 实际签发的证书为准，当前显示的是申请时的预期值");
        } else {
            vo.setActionHint(null);
        }
        return vo;
    }
}
