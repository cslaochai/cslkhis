package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.enums.CertIssuedModeEnum;
import com.his.common.enums.CertStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.platform.config.SignProperties;
import com.his.spi.dto.SignCertIssueDTO;
import com.his.platform.dto.SignCertPageQueryDTO;
import com.his.spi.dto.SignCertRevokeDTO;
import com.his.platform.entity.SysSignCert;
import com.his.platform.mapper.SignConfigMapper;
import com.his.platform.mapper.SysSignCertMapper;
import com.his.platform.service.RedisSequenceService;
import com.his.platform.service.SignCertService;
import com.his.platform.utils.KeyPairFactory;
import com.his.platform.utils.KeyProtectorUtil;
import com.his.platform.utils.PemCodecUtil;
import com.his.platform.utils.SignCryptoUtil;
import com.his.platform.utils.SignPinUtil;
import com.his.platform.vo.SignCertVO;
import com.his.spi.dto.SignCertBindDTO;
import com.his.spi.dto.SignCertKeygenDTO;
import com.his.spi.vo.SignCaApplyVO;
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
 * <p>CA 交互全程线下人工：HIS 本地生成密钥对（私钥加密托管，不出 HIS）→ 管理员把
 * 「CA 签发申请单」（Subject DN + 公钥）线下送 CA 认证中心人工签发 → 管理员把返回的
 * 证书 PEM 回填本系统。全程不调用任何 CA 接口。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignCertServiceImpl extends ServiceImpl<SysSignCertMapper, SysSignCert> implements SignCertService {

    private static final String CERT_NO_PREFIX = "CERT";

    private final SysSignCertMapper sysSignCertMapper;

    private final SignConfigMapper signConfigMapper;

    private final KeyProtectorUtil keyProtectorUtil;

    private final SignPinUtil signPinUtil;

    private final SignProperties signProperties;

    private final RedisSequenceService redisSequenceService;

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
        // CA 交互已改为线下人工，系统无法自动完成签发——此时必须说清该怎么办，而不是静默发一张无 CA 背书的证书
        throw new BusinessException("员工「" + (TextUtil.hasText(empName) ? empName : empId)
                + "」没有有效的签名证书；证书签发需线下办理："
                + "请在「签名中心 → 证书管理」先生成密钥对，把 CA 签发申请单线下送 CA 认证中心签发，再回填证书");
    }

    /**
     * 生成密钥对（CA 线下签发第一步）：私钥本地生成并加密托管，公钥随申请单线下送 CA。
     *
     * <p>不调用任何 CA 接口——HIS 与 CA 认证中心的交互全程线下人工完成。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        // 公钥一致性校验：CA 回执的证书必须是用这把公钥签的，否则回填了也验不过签
        String certPublicKey = PemCodecUtil.publicKeyFromCertificate(dto.getCertPem());
        if (certPublicKey == null) {
            throw new BusinessException("无法从回填的证书中解析出公钥，请确认贴入的是完整的证书 PEM");
        }
        if (!certPublicKey.trim().equals(cert.getPublicKey().trim())) {
            throw new BusinessException("回填的证书与本证书的公钥不一致（证书指纹 "
                    + SignCryptoUtil.fingerprintGroups(cert.getPublicKey()) + " ≠ "
                    + SignCryptoUtil.fingerprintGroups(certPublicKey) + "）；"
                    + "请核对是否贴错了别的证书");
        }

        int validDays = effectiveValidDays(dto.getValidDays());
        LocalDateTime now = TimeUtil.nowSeconds();
        cert.setCertPem(dto.getCertPem());
        cert.setCaSerial(TextUtil.hasText(dto.getCaSerial()) ? dto.getCaSerial() : null);
        cert.setCaIssuer(TextUtil.hasText(dto.getCaIssuer()) ? dto.getCaIssuer() : null);
        cert.setValidFrom(now);
        cert.setValidTo(now.plusDays(validDays));
        cert.setCertStatus(CertStatusEnum.ACTIVE.getCode());
        if (TextUtil.hasText(dto.getRemark())) {
            cert.setRemark(dto.getRemark());
        } else {
            cert.setRemark("CA 线下签发，证书由管理员人工回填（私钥本地生成并加密托管，未出 HIS）");
        }
        sysSignCertMapper.updateById(cert);
        log.info("回填 CA 签发证书 certNo={} empId={} caIssuer={} 有效期至={}",
                cert.getCertNo(), cert.getEmpId(), cert.getCaIssuer(), cert.getValidTo());
        return toVO(cert, true);
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
            cert.setDigestAlgo(SignCryptoUtil.digestAlgoFor(keyAlgo));
            cert.setSignAlgo(SignCryptoUtil.signAlgoFor(keyAlgo));
            cert.setPublicKey(pair.publicPem());
            cert.setKeyFingerprint(SignCryptoUtil.fingerprint(pair.publicPem()));
            cert.setProtectedPrivateKey(protectedKey);
            cert.setKeySalt(salt);
            cert.setKeyIterations(signProperties.getIterations());
            cert.setPinHash(pinHash);
            cert.setPinSalt(pinSalt);
            cert.setPinIterations(signPinUtil.defaultIterations());
            cert.setCaSubjectDn(dn);
            cert.setIssuedMode(CertIssuedModeEnum.MANUAL.getCode());
            cert.setCertStatus(CertStatusEnum.PENDING_CA.getCode());
            cert.setValidFrom(now);
            cert.setValidTo(to);
            cert.setSignCount(0);
            cert.setRemark(remark);
            try {
                sysSignCertMapper.insert(cert);
                log.info("生成签名密钥对 certNo={} empId={} 算法={} subject={}（待线下送 CA 签发）",
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
     * CA 签发申请单上的 Subject DN：CN=姓名, O=机构, OU=科室
     */
    private String buildSubjectDn(String empName, String deptName) {
        StringBuilder dn = new StringBuilder("CN=").append(TextUtil.hasText(empName) ? empName : "unknown")
                .append(", O=长沙市麓康医院");
        if (TextUtil.hasText(deptName)) {
            dn.append(", OU=").append(deptName);
        }
        return dn.toString();
    }

    // 查询

    /**
     * 外部 CA 签发路径：本地生成密钥对（私钥不出本地、主口令加密落库）→ 组 Subject+公钥提交 CA 签发。
     *
     * <p>csssl 适配器返回证书链后：公钥/指纹/受保护私钥沿用院内口径落库，CA 回执的
     * 序列号/签发CA/证书链写入新增列（cert_pem / ca_serial / ca_issuer），有效期以
     * CA 回执证书为准（CSSSL 证书 valid_from/valid_to 由 CA 生成）。
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
        cert.setRevokeBy(operatorId);
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
                    + "请先线下送 CA 签发并回填证书后才能用于签名");
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
     * 软签签名：先校验签名人 PIN，再解密私钥完成签名。
     *
     * <p>PIN 是软签的「签名人专有」替身——UKey 时代由芯片内的 PIN 保证「签署时仅由签名人控制」，
     * 无硬件时用一次性口令把这一步补上：口令不入库，只存 PBKDF2 哈希。
     *
     * @param cert    证书行
     * @param content 待签内容
     * @param pin     签名人本次输入的 PIN
     * @return Base64 签名值
     */
    @Override
    public String signWithPin(SysSignCert cert, String content, String pin) {
        if (cert == null) {
            throw new BusinessException("证书不存在");
        }
        if (!TextUtil.hasText(pin)) {
            throw new BusinessException("请输入签名 PIN：软签要求每次签名都由签名人本人确认");
        }
        // PIN 校验先于私钥解密：口令不对就不碰私钥
        boolean pinOk = signPinUtil.verify(pin, cert.getPinSalt(),
                cert.getPinIterations() == null ? signPinUtil.defaultIterations() : cert.getPinIterations(),
                cert.getPinHash());
        if (!pinOk) {
            log.warn("签名 PIN 校验失败 certNo={} empId={}", cert.getCertNo(), cert.getEmpId());
            throw new BusinessException("签名 PIN 错误，无法完成签名");
        }
        String privatePem = privatePemOf(cert);
        return SignCryptoUtil.sign(privatePem, content);
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
     * 系统参数优先，其次 yml，最后兜底 365
     */
    private int effectiveValidDays(Integer fromDto) {
        if (fromDto != null && fromDto > 0) {
            return fromDto;
        }
        Integer cfg = intValue(signConfigMapper.selectValue(SystemConfigKeyConst.SIGN_CERT_VALID_DAYS));
        if (cfg != null && cfg > 0) {
            return cfg;
        }
        return signProperties.getDefaultValidDays() > 0 ? signProperties.getDefaultValidDays() : 365;
    }

    private String nextCertNo() {
        return CERT_NO_PREFIX + LocalDate.now().format(DateFormats.COMPACT_DATE)
                + String.format("%04d", redisSequenceService.next("CERT"));
    }

    /**
     * 生成「CA 签发申请单」：管理员拿这份单据线下送 CA 认证中心人工签发。
     */
    private SignCaApplyVO toApplyVO(SysSignCert c) {
        SignCaApplyVO vo = new SignCaApplyVO();
        vo.setCertId(c.getId());
        vo.setCertNo(c.getCertNo());
        vo.setEmpName(c.getEmpName());
        vo.setSubjectDn(c.getCaSubjectDn());
        vo.setPublicKeyPem(c.getPublicKey());
        vo.setKeyFingerprintGroups(SignCryptoUtil.fingerprintGroups(c.getKeyFingerprint()));
        vo.setValidDays(c.getValidTo() != null && c.getValidFrom() != null
                ? (int) java.time.Duration.between(c.getValidFrom(), c.getValidTo()).toDays() : null);
        vo.setCertStatusText(CertStatusEnum.getLabel(c.getCertStatus()));
        vo.setGuide("线下签发流程：① 把下方 Subject DN 与公钥 PEM 送 CA 认证中心人工签发；"
                + "② CA 返回证书 PEM 后，在「证书管理」用 certId=" + c.getCertNo() + " 回填证书；"
                + "③ 回填后证书方可用于签名。私钥全程留在本系统，未曾送出。");
        return vo;
    }

    private SignCertVO toVO(SysSignCert c, boolean withPublicKey) {
        SignCertVO vo = new SignCertVO();
        vo.setId(c.getId());
        vo.setCertNo(c.getCertNo());
        vo.setEmpId(c.getEmpId());
        vo.setEmpName(c.getEmpName());
        vo.setDeptId(c.getDeptId());
        vo.setDeptName(c.getDeptName());
        vo.setKeyAlgo(c.getKeyAlgo());
        vo.setDigestAlgo(c.getDigestAlgo());
        vo.setSignAlgo(c.getSignAlgo());
        vo.setKeyFingerprint(c.getKeyFingerprint());
        vo.setKeyFingerprintGroups(SignCryptoUtil.fingerprintGroups(c.getKeyFingerprint()));
        vo.setCaIssuer(c.getCaIssuer());
        vo.setCaSerial(c.getCaSerial());
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
