package com.his.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.config.SignProperties;
import com.his.common.dto.SignCertIssueDTO;
import com.his.common.dto.SignCertQueryPageDTO;
import com.his.common.dto.SignCertRevokeDTO;
import com.his.common.entity.SysSignCert;
import com.his.common.enums.CertIssuedModeEnum;
import com.his.common.enums.CertStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.mapper.SignConfigMapper;
import com.his.common.mapper.SysSignCertMapper;
import com.his.common.service.ExternalCaChannelService;
import com.his.common.service.RedisSequenceService;
import com.his.common.service.SignCertService;
import com.his.common.util.*;
import com.his.common.vo.SignCertVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 签名证书服务实现。
 *
 * <p>三条不能破的约定：
 * <ol>
 *   <li><b>私钥只以密文形态落库</b>，{@link #privatePemOf} 是**唯一的解密出口**，
 *       且只允许签名服务调用（不给任何 Controller 端点）。</li>
 *   <li><b>吊销不删行</b>：历史签名上存着证书编号，删证书会让那些签名
 *       永远无法验证（"当时用哪把公钥"丢失）。</li>
 *   <li><b>同一员工同时刻只有一张有效证书</b>，但**不做唯一索引** ——
 *       并发自动签发可能瞬时造出两张，靠"过期作废 + 取最新一张"消化，
 *       用唯一索引会在签名请求里直接抛 DuplicateKey，把留痕动作变成业务中断。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignCertServiceImpl extends ServiceImpl<SysSignCertMapper, SysSignCert> implements SignCertService {

    private static final String CERT_NO_PREFIX = "CERT";
    private static final String CFG_VALID_DAYS = "sign.cert.valid_days";
    private static final String CFG_AUTO_ISSUE = "sign.cert.auto_issue";

    private final SysSignCertMapper sysSignCertMapper;
    private final SignConfigMapper signConfigMapper;
    private final KeyProtectorUtil keyProtectorUtil;
    private final SignProperties signProperties;
    private final RedisSequenceService redisSequenceService;
    /**
     * 外部 CA 适配器（M8 留口子）：无实现/未配置 external 时为 null，走院内自签
     */
    private final ExternalCaChannelService externalCaChannelService;

    // 取证书 / 自动签发

    private static Integer intValue(String s) {
        if (!StringUtils.hasText(s)) {
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
        // C 类保留：签名服务在签发证书前直接调本方法（标量形参，无登录态时也会被调），不经 HTTP 绑定，注解跑不到
        if (empId == null) {
            throw new BusinessException("签名人不能为空（未取到当前登录用户的员工ID）；"
                    + "签名留痕必须落到员工，不能落成系统账号");
        }
        LocalDateTime now = TimeUtil.nowSeconds();
        SysSignCert exist = sysSignCertMapper.selectActiveByEmp(empId, now);
        if (exist != null) {
            return exist;
        }
        if (!autoIssueEnabled()) {
            throw new BusinessException("员工「" + (StringUtils.hasText(empName) ? empName : empId)
                    + "」没有有效的签名证书，且系统已关闭自动签发；"
                    + "请先在「签名中心 → 证书管理」为其签发证书");
        }
        log.info("自动签发签名证书：empId={} empName={}", empId, empName);
        return doIssue(empId, empName, deptId, deptName, effectiveValidDays(null),
                CertIssuedModeEnum.AUTO, null, null, "首次签名时由系统按需自动签发（院内托管证书）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignCertVO issue(SignCertIssueDTO dto, Long operatorId, String operatorName) {
        // C 类保留：本方法是能力层 API，只被签名中心的 service 转发调用（操作人由登录态在层外补），
        // 要下沉就得在别的模块的 Controller 上补 @Valid，超出本模块边界；姓名是签发时写入证书的快照，必填性归这里判
        if (dto == null || dto.getEmpId() == null) {
            throw new BusinessException("员工ID不能为空");
        }
        if (!StringUtils.hasText(dto.getEmpName())) {
            throw new BusinessException("员工姓名不能为空（证书上必须能看出这是谁）");
        }
        SysSignCert exist = sysSignCertMapper.selectActiveByEmp(dto.getEmpId(), TimeUtil.nowSeconds());
        if (exist != null) {
            throw new BusinessException("员工「" + dto.getEmpName() + "」已持有有效证书 "
                    + exist.getCertNo() + "（有效期至 " + exist.getValidTo() + "）；"
                    + "如需换新证书请先吊销原证书（吊销会留痕）");
        }
        SysSignCert cert = doIssue(dto.getEmpId(), dto.getEmpName(), dto.getDeptId(), dto.getDeptName(),
                effectiveValidDays(dto.getValidDays()), CertIssuedModeEnum.MANUAL,
                operatorId, operatorName, dto.getRemark());
        return toVO(cert, true);
    }

    private SysSignCert doIssue(Long empId, String empName, Long deptId, String deptName,
                                int validDays, CertIssuedModeEnum mode,
                                Long operatorId, String operatorName, String remark) {
        // 没有主口令就不签发：宁可不发证，也不让私钥明文落库
        keyProtectorUtil.requireSecret();

        // M8 留口子：外部 CA 模式下先本地生成密钥对、把 Subject+公钥交给 CA 适配器
        //（PKCS#10 常规流程：私钥不出本地，CA 只签公钥）。当前适配器是控制台打印桩，
        // 打印 CSR 后返回 null —— 中断签发，绝不静默回退院内自签（那等于伪造信任根）。
        if (externalCaChannelService.available()) {
            issueViaExternalCa(empName, deptName, validDays);
        }

        KeyPairFactory.KeyPairPem pair = KeyPairFactory.generate();
        String salt = keyProtectorUtil.newSalt();
        String protectedKey = keyProtectorUtil.protect(pair.privatePem(), salt, signProperties.getIterations());

        LocalDateTime now = TimeUtil.nowSeconds();
        LocalDateTime to = now.plusDays(validDays);

        for (int attempt = 1; attempt <= 3; attempt++) {
            SysSignCert cert = new SysSignCert();
            cert.setCertNo(nextCertNo());
            cert.setEmpId(empId);
            cert.setEmpName(empName);
            cert.setDeptId(deptId);
            cert.setDeptName(deptName);
            cert.setKeyAlgo("RSA" + KeyPairFactory.KEY_SIZE);
            cert.setDigestAlgo(SignCryptoUtil.DIGEST_ALGO);
            cert.setSignAlgo(SignCryptoUtil.SIGN_ALGO);
            cert.setPublicKey(pair.publicPem());
            cert.setKeyFingerprint(SignCryptoUtil.fingerprint(pair.publicPem()));
            cert.setProtectedPrivateKey(protectedKey);
            cert.setKeySalt(salt);
            cert.setKeyIterations(signProperties.getIterations());
            cert.setIssuedMode(mode.getCode());
            cert.setCertStatus(CertStatusEnum.ACTIVE.getCode());
            cert.setValidFrom(now);
            cert.setValidTo(to);
            cert.setSignCount(0);
            cert.setRemark(remark);
            try {
                sysSignCertMapper.insert(cert);
                log.info("签发签名证书 certNo={} empId={} 有效期至={} 方式={}",
                        cert.getCertNo(), empId, to, mode.getText());
                return cert;
            } catch (DuplicateKeyException e) {
                if (attempt == 3) {
                    throw new BusinessException("证书编号连续冲突 3 次（" + cert.getCertNo()
                            + "），请稍后重试；若持续出现请检查是否有脚本在批量造证");
                }
                log.warn("证书编号冲突，重试第 {} 次：{}", attempt, cert.getCertNo());
            }
        }
        throw new BusinessException("证书签发失败");
    }

    // 查询

    /**
     * 外部 CA 签发路径（M8 留口子）：本地生成密钥对 → 提交 CSR。
     * 桩实现会在打印后返回 null → 抛异常中断；接入真 CA 后，在 TODO 处
     * 用 {@code issued.certPem()} 替代自签公钥落库（私钥托管/签名链/验签均不变）。
     */
    private SysSignCert issueViaExternalCa(String empName, String deptName,
                                           int validDays) {
        KeyPairFactory.KeyPairPem pair = KeyPairFactory.generate();
        String subjectDn = "CN=" + (StringUtils.hasText(empName) ? empName : "unknown")
                + ", O=长沙市麓康医院"
                + (StringUtils.hasText(deptName) ? ", OU=" + deptName : "");
        ExternalCaChannelService.IssuedCert issued = externalCaChannelService.issueCert(
                new ExternalCaChannelService.IssueRequest(subjectDn, pair.publicPem(), validDays));
        if (issued == null) {
            throw new BusinessException("外部 CA（" + externalCaChannelService.name() + "）未返回证书，签发已中断；"
                    + "未接入真 CA 前请将 his.sign.ca-mode 改回 internal 使用院内自签");
        }
        // 拿到证书也故意不落库：M8 只留出口子形态，真 CA 到位前必须中断，
        // 免得发出一张「看起来是真 CA」却没人验证过的证书。
        throw new BusinessException("外部 CA 签发落库尚未接入（M8 留口子）：拿到 CA 证书后在 SignCertServiceImpl.issueViaExternalCa 补齐落库");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignCertVO revoke(SignCertRevokeDTO dto, Long operatorId, String operatorName) {
        // C 类保留：同 issue —— 由他模块的 service 直接调用，注解与 @Valid 挂在那一侧的接口上，本层拿不到绑定时的校验；
        // 吊销理由是废止签名能力的留痕依据，任何调用路径都必须带上
        if (dto == null || dto.getCertId() == null) {
            throw new BusinessException("证书ID不能为空");
        }
        if (!StringUtils.hasText(dto.getReason())) {
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
    public IPage<SignCertVO> listPage(SignCertQueryPageDTO query) {
        LambdaQueryWrapper<SysSignCert> w = new LambdaQueryWrapper<>();
        w.eq(query.getEmpId() != null, SysSignCert::getEmpId, query.getEmpId());
        w.eq(query.getCertStatus() != null, SysSignCert::getCertStatus, query.getCertStatus());
        w.eq(query.getIssuedMode() != null, SysSignCert::getIssuedMode, query.getIssuedMode());
        if (StringUtils.hasText(query.getKeyword())) {
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
        if (StringUtils.hasText(keyword)) {
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
        LocalDateTime now = TimeUtil.nowSeconds();
        if (cert.getValidTo() != null && cert.getValidTo().isBefore(now)) {
            throw new BusinessException("证书 " + cert.getCertNo() + " 已于 " + cert.getValidTo()
                    + " 过期，不能用于签名；请重新签发");
        }
        return keyProtectorUtil.unprotect(cert.getProtectedPrivateKey(), cert.getKeySalt(),
                cert.getKeyIterations() == null ? signProperties.getIterations() : cert.getKeyIterations());
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
        Integer cfg = intValue(signConfigMapper.selectValue(CFG_VALID_DAYS));
        if (cfg != null && cfg > 0) {
            return cfg;
        }
        return signProperties.getDefaultValidDays() > 0 ? signProperties.getDefaultValidDays() : 365;
    }

    private boolean autoIssueEnabled() {
        String v = signConfigMapper.selectValue(CFG_AUTO_ISSUE);
        if (StringUtils.hasText(v)) {
            return !"0".equals(v.trim());
        }
        return signProperties.isAutoIssueCert();
    }

    private String nextCertNo() {
        String prefix = CERT_NO_PREFIX + LocalDate.now().format(DateFormats.COMPACT_DATE);
        return prefix + String.format("%04d", sysSignCertMapper.countByCertNoPrefix(prefix) + 1);
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
        vo.setIssuedMode(c.getIssuedMode());
        vo.setIssuedModeText(CertIssuedModeEnum.textOf(c.getIssuedMode()));
        vo.setCertStatus(c.getCertStatus());
        vo.setCertStatusText(CertStatusEnum.textOf(c.getCertStatus()));
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
