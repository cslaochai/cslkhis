package com.his.sign.spi;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.sign.config.SignProperties;
import com.his.sign.entity.SysSignCert;
import com.his.sign.mapper.SysSignCertMapper;
import com.his.sign.service.ExternalCaChannelService;
import com.his.sign.service.SignCertService;
import com.his.sign.utils.KeyPairFactory;
import com.his.sign.utils.SignCryptoUtil;
import com.his.spi.dto.SignCertIssueDTO;
import com.his.spi.dto.SignCertPageQueryDTO;
import com.his.spi.dto.SignCertRevokeDTO;
import com.his.spi.service.SignCertFacadeService;
import com.his.spi.vo.SignCaProbeOutboundVO;
import com.his.spi.vo.SignCaStatusVO;
import com.his.spi.vo.SignCertBriefVO;
import com.his.spi.vo.SignCertSelectListVO;
import com.his.spi.vo.SignCertVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SignCertFacadeService 在 his-sign 侧的实现：运行时签名/验签代理内部能力，
 * 证书台账运维（分页 / 详情 / 签发 / 吊销 / 下拉）与 CA 状态探针转调内部服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignCertFacadeServiceImpl implements SignCertFacadeService {

    private final SignCertService signCertService;

    private final SysSignCertMapper sysSignCertMapper;

    private final SignProperties signProperties;

    private final ExternalCaChannelService externalCaChannelService;

    // ---------- 运行时：证书取用 / 私钥 / 签名运算（代理内部能力） ----------

    @Override
    public SignCertBriefVO ensureActiveCert(Long empId, String empName, Long deptId, String deptName) {
        SysSignCert cert = signCertService.ensureActiveCert(empId, empName, deptId, deptName);
        SignCertBriefVO brief = new SignCertBriefVO();
        brief.setId(cert.getId());
        brief.setCertNo(cert.getCertNo());
        brief.setPublicKey(cert.getPublicKey());
        return brief;
    }

    @Override
    public SignCertBriefVO getCertById(Long certId) {
        SysSignCert cert = certId == null ? null : sysSignCertMapper.selectById(certId);
        if (cert == null) {
            return null;
        }
        SignCertBriefVO brief = new SignCertBriefVO();
        brief.setId(cert.getId());
        brief.setCertNo(cert.getCertNo());
        brief.setPublicKey(cert.getPublicKey());
        return brief;
    }

    @Override
    public String privatePemOf(Long certId) {
        SysSignCert cert = sysSignCertMapper.selectById(certId);
        return signCertService.privatePemOf(cert);
    }

    @Override
    public void markCertUsed(Long certId, LocalDateTime usedTime) {
        if (certId == null) {
            return;
        }
        sysSignCertMapper.update(null, new LambdaUpdateWrapper<SysSignCert>()
                .set(SysSignCert::getLastUsedTime, usedTime)
                .setSql("sign_count = sign_count + 1")
                .eq(SysSignCert::getId, certId));
    }

    @Override
    public long countAllCerts() {
        return sysSignCertMapper.selectCount(null);
    }

    @Override
    public long countByStatus(Integer certStatus) {
        return signCertService.countByStatus(certStatus);
    }

    @Override
    public long countAutoIssued() {
        return signCertService.countAutoIssued();
    }

    @Override
    public long countActiveEmployees() {
        return signCertService.countActiveEmployees();
    }

    @Override
    public String sha256Hex(String content) {
        return SignCryptoUtil.sha256Hex(content);
    }

    @Override
    public String signWithPrivateKey(String privatePem, String content) {
        return SignCryptoUtil.sign(privatePem, content);
    }

    @Override
    public boolean verifySignValue(String publicKeyPem, String content, String signValue) {
        return SignCryptoUtil.verify(publicKeyPem, content, signValue);
    }

    @Override
    public String digestAlgo() {
        return SignCryptoUtil.DIGEST_ALGO;
    }

    @Override
    public String signAlgo() {
        return SignCryptoUtil.SIGN_ALGO;
    }

    // ---------- 证书台账运维 ----------

    @Override
    public IPage<SignCertVO> listCertPage(SignCertPageQueryDTO query) {
        com.his.sign.dto.SignCertPageQueryDTO inner = new com.his.sign.dto.SignCertPageQueryDTO();
        BeanUtils.copyProperties(query, inner);
        IPage<com.his.sign.vo.SignCertVO> page = signCertService.listPage(inner);
        Page<SignCertVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<SignCertVO> rows = new ArrayList<>(page.getRecords().size());
        for (com.his.sign.vo.SignCertVO row : page.getRecords()) {
            SignCertVO vo = new SignCertVO();
            BeanUtils.copyProperties(row, vo);
            rows.add(vo);
        }
        result.setRecords(rows);
        return result;
    }

    @Override
    public SignCertVO getCertDetail(Long certId) {
        com.his.sign.vo.SignCertVO inner = signCertService.getById(certId);
        SignCertVO vo = new SignCertVO();
        BeanUtils.copyProperties(inner, vo);
        return vo;
    }

    @Override
    public SignCertVO issueCert(SignCertIssueDTO dto) {
        // 操作人在 web 层补；Facade 路径无登录态，操作人留空，签发事实仍落库
        com.his.sign.vo.SignCertVO inner = signCertService.issue(dto, null, null);
        SignCertVO vo = new SignCertVO();
        BeanUtils.copyProperties(inner, vo);
        return vo;
    }

    @Override
    public void revokeCert(SignCertRevokeDTO dto) {
        signCertService.revoke(dto, null, null);
    }

    @Override
    public List<SignCertSelectListVO> listActiveCertOptions(String keyword) {
        List<com.his.sign.vo.SignCertVO> innerList = signCertService.selectList(keyword);
        List<SignCertSelectListVO> result = new ArrayList<>(innerList.size());
        for (com.his.sign.vo.SignCertVO row : innerList) {
            SignCertSelectListVO vo = new SignCertSelectListVO();
            vo.setId(row.getId());
            vo.setCertNo(row.getCertNo());
            vo.setEmpId(row.getEmpId());
            vo.setEmpName(row.getEmpName());
            vo.setCertStatus(row.getCertStatus());
            vo.setCertStatusText(row.getCertStatusText());
            result.add(vo);
        }
        return result;
    }

    // ---------- CA 模式状态与外发探针 ----------

    @Override
    public SignCaStatusVO caStatus() {
        SignCaStatusVO vo = new SignCaStatusVO();
        String mode = signProperties.getCaMode();
        vo.setCaMode(mode);
        boolean available = externalCaChannelService.available();
        vo.setAvailable(available);
        if (available) {
            vo.setProviderName(externalCaChannelService.name());
            vo.setHint("外部 CA 模式：证书签发走外部机构；适配器未返回证书时中断签发，绝不静默回退院内自签");
        } else {
            vo.setProviderName(null);
            vo.setHint("院内自签（internal）：密钥对本地生成、证书本地托管，信任根为院内，不对外声称法律效力");
        }
        return vo;
    }

    @Override
    public SignCaProbeOutboundVO probeCaOutbound() {
        SignCaProbeOutboundVO vo = new SignCaProbeOutboundVO();
        if (!externalCaChannelService.available()) {
            vo.setProviderName(null);
            vo.setCsrPrinted(false);
            vo.setCertReturned(false);
            vo.setConclusion("当前为院内自签（internal）模式，未向外部 CA 提交 CSR；切换 external 后可再探外发通道");
            return vo;
        }
        // 本地生成密钥对，只把公钥交给适配器试跑一次，私钥不出本地、不落库
        KeyPairFactory.KeyPairPem pair = KeyPairFactory.generate();
        ExternalCaChannelService.IssuedCert issued = externalCaChannelService.issueCert(
                new ExternalCaChannelService.IssueRequest(
                        "CN=CA探针, O=长沙市麓康医院", pair.publicPem(), 30));
        vo.setProviderName(externalCaChannelService.name());
        vo.setCsrPrinted(true);
        vo.setCertReturned(issued != null);
        vo.setConclusion(issued != null
                ? "外部 CA 已返回证书，外发通道可用，可按真实机构流程接入"
                : "CSR 已按适配器口径提交并打印到服务端控制台，适配器未返回证书（未接入真 CA）；签发在此中断，不回退院内自签");
        return vo;
    }
}
