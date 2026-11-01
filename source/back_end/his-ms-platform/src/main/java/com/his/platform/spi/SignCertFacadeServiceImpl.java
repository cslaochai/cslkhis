package com.his.platform.spi;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.platform.config.SignProperties;
import com.his.platform.entity.SysSignCert;
import com.his.platform.mapper.SysSignCertMapper;
import com.his.platform.service.SignCertService;
import com.his.platform.utils.SignCryptoUtil;
import com.his.spi.dto.SignCaPushDTO;
import com.his.spi.dto.SignCertBindDTO;
import com.his.spi.dto.SignCertKeygenDTO;
import com.his.spi.dto.SignCertPageQueryDTO;
import com.his.spi.dto.SignCertRevokeDTO;
import com.his.spi.dto.SignPinChangeDTO;
import com.his.spi.service.SignCertFacadeService;
import com.his.spi.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SignCertFacadeService 在 his-sign 侧的实现：运行时签名/验签代理内部能力，
 * 证书台账运维（分页 / 详情 / 密钥生成 / 证书回填 / 吊销 / 下拉）与 CA 状态转调内部服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignCertFacadeServiceImpl implements SignCertFacadeService {

    private final SignCertService signCertService;

    private final SysSignCertMapper sysSignCertMapper;

    private final SignProperties signProperties;

    // ---------- 运行时：证书取用 / 私钥 / 签名运算（代理内部能力） ----------

    @Override
    public SignCertBriefVO ensureActiveCert(Long empId, String empName, Long deptId, String deptName) {
        SysSignCert cert = signCertService.ensureActiveCert(empId, empName, deptId, deptName);
        return briefOf(cert);
    }

    @Override
    public SignCertBriefVO getCertById(Long certId) {
        SysSignCert cert = certId == null ? null : sysSignCertMapper.selectById(certId);
        return cert == null ? null : briefOf(cert);
    }

    private SignCertBriefVO briefOf(SysSignCert cert) {
        SignCertBriefVO brief = new SignCertBriefVO();
        brief.setId(cert.getId());
        brief.setCertNo(cert.getCertNo());
        brief.setPublicKey(cert.getPublicKey());
        // 摘要/签名算法由密钥算法现算：证书行只存 key_algo 一个事实，签名的留痕列由这里喂值
        brief.setDigestAlgo(SignCryptoUtil.digestAlgoFor(cert.getKeyAlgo()));
        brief.setSignAlgo(SignCryptoUtil.signAlgoFor(cert.getKeyAlgo()));
        return brief;
    }

    @Override
    public String signWithCert(Long certId, String content, String pin) {
        // 私钥的解密与使用全部收在签名域内：调用方只拿到签名值，私钥不出本模块
        SysSignCert cert = sysSignCertMapper.selectById(certId);
        return signCertService.signWithPin(cert, content, pin);
    }

    @Override
    public SignPreCheckVO preCheckForSign(Long empId) {
        return signCertService.preCheckForSign(empId);
    }

    @Override
    public void changeOwnPin(SignPinChangeDTO dto) {
        signCertService.changeOwnPin(dto);
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
    public boolean verifySignValue(String publicKeyPem, String content, String signValue) {
        return SignCryptoUtil.verify(publicKeyPem, content, signValue);
    }

    @Override
    public String digestAlgo() {
        // 当前配置算法的摘要（新证书落库口径；签名记录行按各自证书行内算法留痕）
        return SignCryptoUtil.digestAlgoFor(signProperties.getKeyAlgorithm());
    }

    @Override
    public String signAlgo() {
        return SignCryptoUtil.signAlgoFor(signProperties.getKeyAlgorithm());
    }

    // ---------- 证书台账运维 ----------

    @Override
    public IPage<SignCertVO> listCertPage(SignCertPageQueryDTO queryDTO) {
        com.his.platform.dto.SignCertPageQueryDTO inner = new com.his.platform.dto.SignCertPageQueryDTO();
        BeanUtils.copyProperties(queryDTO, inner);
        IPage<com.his.platform.vo.SignCertVO> page = signCertService.listPage(inner);
        Page<SignCertVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<SignCertVO> rows = new ArrayList<>(page.getRecords().size());
        for (com.his.platform.vo.SignCertVO row : page.getRecords()) {
            SignCertVO vo = new SignCertVO();
            BeanUtils.copyProperties(row, vo);
            rows.add(vo);
        }
        result.setRecords(rows);
        return result;
    }

    @Override
    public SignCertVO getCertDetail(Long certId) {
        com.his.platform.vo.SignCertVO inner = signCertService.getById(certId);
        SignCertVO vo = new SignCertVO();
        BeanUtils.copyProperties(inner, vo);
        return vo;
    }

    @Override
    public List<SignCaApplyRecordVO> listCaApplies(Long certId) {
        return signCertService.caApplyHistory(certId);
    }

    @Override
    public SignCertVO getActiveCertByEmp(Long empId) {
        com.his.platform.vo.SignCertVO inner = signCertService.getActiveCertVO(empId);
        if (inner == null) {
            return null;
        }
        SignCertVO vo = new SignCertVO();
        BeanUtils.copyProperties(inner, vo);
        return vo;
    }

    @Override
    public SignCaApplyVO keygenCert(SignCertKeygenDTO dto) {
        // 操作人在 web 层补；Facade 路径无登录态，操作人留空，签发事实仍落库
        return signCertService.keygen(dto, null, null);
    }

    @Override
    public SignCaApplyVO submitCaApply(Long certId) {
        return signCertService.applyToCa(certId, null, null);
    }

    @Override
    public SignCaPushResultVO applyCaPush(SignCaPushDTO dto) {
        return signCertService.applyCaPush(dto);
    }

    @Override
    public SignCertVO bindCert(SignCertBindDTO dto) {
        com.his.platform.vo.SignCertVO inner = signCertService.bind(dto, null, null);
        SignCertVO vo = new SignCertVO();
        BeanUtils.copyProperties(inner, vo);
        return vo;
    }

    @Override
    public SignCertVO bindCertFile(Long certId, String password, byte[] fileBytes) {
        com.his.platform.vo.SignCertVO inner = signCertService.bindFile(certId, password, fileBytes, null, null);
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
        List<com.his.platform.vo.SignCertVO> innerList = signCertService.selectList(keyword);
        List<SignCertSelectListVO> result = new ArrayList<>(innerList.size());
        for (com.his.platform.vo.SignCertVO row : innerList) {
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
        return signCertService.caStatus();
    }
}
