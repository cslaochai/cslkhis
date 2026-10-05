package com.his.common.service.impl;

import com.his.common.entity.BizEmrSignature;
import com.his.common.entity.SysSignCert;
import com.his.common.enums.SignScene;
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
 *
 * <p>为什么单独一个类：单号冲突（{@code uk_sign_no}）要在**事务外**重试。
 * 如果重试逻辑写在同一事务方法里，Spring 的 `@Transactional` 已经在第一次异常时把事务
 * 标记成 rollback-only，第二次 insert 会以 "Transaction silently rolled back" 收场 ——
 * 报错信息和真实原因完全对不上（P5.4 质控单踩过同一个坑）。
 *
 * <p>本类只做三件事，都在一个事务里：写签名行 → 回写业务锚点 → 更新证书使用计数。
 * 三件事必须同生共死：签名行写了而锚点没写，病历就会显示"未签名"却存在签名证据；
 * 锚点写了而签名行没写，病历会指向一个不存在的签名ID。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureStoreServiceImpl implements SignatureStoreService {

    private final BizEmrSignatureMapper signMapper;
    private final SysSignCertMapper certMapper;

    @Transactional(rollbackFor = Exception.class)
    public void insertAndAnchor(BizEmrSignature entity, SignableContentProvider provider, SignScene scene,
                                SysSignCert cert) {
        signMapper.insert(entity);
        provider.applySignAnchor(entity.getBizId(), scene, entity.getId(), entity.getSignedTime());
        if (cert != null) {
            cert.setLastUsedTime(entity.getSignedTime());
            cert.setSignCount(cert.getSignCount() == null ? 1 : cert.getSignCount() + 1);
            certMapper.updateById(cert);
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
        BizEmrSignature old = signMapper.selectById(signId);
        patch.setVerifyCount(old == null || old.getVerifyCount() == null ? 1 : old.getVerifyCount() + 1);
        signMapper.updateById(patch);
    }

    /**
     * 作废：只追加作废信息，签名证据列一行不动
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateInvalidate(Long signId, String reason, java.time.LocalDateTime time,
                                 Long operatorId, String operatorName) {
        BizEmrSignature patch = new BizEmrSignature();
        patch.setId(signId);
        patch.setSignStatus(com.his.common.enums.SignStatus.INVALID.getCode());
        patch.setInvalidReason(reason);
        patch.setInvalidTime(time);
        patch.setInvalidBy(operatorId);
        patch.setInvalidByName(operatorName);
        signMapper.updateById(patch);
    }
}
