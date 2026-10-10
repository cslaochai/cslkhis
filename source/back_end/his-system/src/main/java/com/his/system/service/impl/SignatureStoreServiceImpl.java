package com.his.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.system.entity.BizEmrSignature;
import com.his.system.entity.SysSignCert;
import com.his.common.enums.SignSceneEnum;
import com.his.common.enums.SignStatusEnum;
import com.his.system.mapper.BizEmrSignatureMapper;
import com.his.system.mapper.SysSignCertMapper;
import com.his.system.service.SignableContentProvider;
import com.his.system.service.SignatureStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 签名落库的**事务边界**。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureStoreServiceImpl extends ServiceImpl<BizEmrSignatureMapper, BizEmrSignature> implements SignatureStoreService {

    private final BizEmrSignatureMapper bizEmrSignatureMapper;

    private final SysSignCertMapper sysSignCertMapper;

    @Transactional(rollbackFor = Exception.class)
    public void insertAndAnchor(BizEmrSignature entity, SignableContentProvider provider, SignSceneEnum scene,
                                SysSignCert cert) {
        bizEmrSignatureMapper.insert(entity);
        provider.applySignAnchor(entity.getBizId(), scene, entity.getId(), entity.getSignedTime());
        if (cert != null) {
            cert.setLastUsedTime(entity.getSignedTime());
            cert.setSignCount(cert.getSignCount() == null ? 1 : cert.getSignCount() + 1);
            sysSignCertMapper.updateById(cert);
        }
    }

    /**
     * 只更新核查结果三列。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateVerifyResult(Long signId, Integer verifyStatus, java.time.LocalDateTime verifyTime) {
        BizEmrSignature patch = new BizEmrSignature();
        patch.setId(signId);
        patch.setVerifyStatus(verifyStatus);
        patch.setVerifyTime(verifyTime);
        BizEmrSignature old = bizEmrSignatureMapper.selectById(signId);
        patch.setVerifyCount(old == null || old.getVerifyCount() == null ? 1 : old.getVerifyCount() + 1);
        bizEmrSignatureMapper.updateById(patch);
    }

    /**
     * 作废：只追加作废信息，签名证据列一行不动
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateInvalidate(Long signId, String reason, java.time.LocalDateTime time,
                                 Long operatorId, String operatorName) {
        BizEmrSignature patch = new BizEmrSignature();
        patch.setId(signId);
        patch.setSignStatus(SignStatusEnum.INVALID.getCode());
        patch.setInvalidReason(reason);
        patch.setInvalidTime(time);
        patch.setInvalidBy(operatorId);
        patch.setInvalidByName(operatorName);
        bizEmrSignatureMapper.updateById(patch);
    }
}
