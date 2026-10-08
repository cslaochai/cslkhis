package com.his.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.entity.BizEmrSignature;
import com.his.common.entity.SysSignCert;
import com.his.common.enums.SignSceneEnum;
import com.his.common.enums.SignStatusEnum;
import com.his.common.mapper.BizEmrSignatureMapper;
import com.his.common.mapper.SysSignCertMapper;
import com.his.common.service.SignableContentProvider;
import com.his.common.service.SignatureStoreService;
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
     *
     * <p>刻意写成一个**独立方法**而不是让调用方 set 一堆字段再 updateById：
     * 谁也别想从这里顺手改 {@code content_digest / sign_value} 这类证据列。
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
