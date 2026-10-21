package com.his.clinical.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.spi.service.SignableContentService;
import com.his.common.enums.SignSceneEnum;
import com.his.common.enums.SignStatusEnum;
import com.his.clinical.entity.BizEmrSignature;
import com.his.spi.service.SignCertFacadeService;
import com.his.spi.vo.SignCertBriefVO;
import com.his.clinical.mapper.BizEmrSignatureMapper;
import com.his.clinical.service.SignatureStoreService;
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

    private final SignCertFacadeService signCertFacadeService;

    @Transactional(rollbackFor = Exception.class)
    public void insertAndAnchor(BizEmrSignature entity, SignableContentService provider, SignSceneEnum scene,
                                SignCertBriefVO cert) {
        bizEmrSignatureMapper.insert(entity);
        provider.applySignAnchor(entity.getBizId(), scene, entity.getId(), entity.getSignedTime());
        if (cert != null) {
            // 证书使用次数与最近使用时间由签名域台账自行维护，本模块只登记一次使用
            signCertFacadeService.markCertUsed(cert.getId(), entity.getSignedTime());
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
