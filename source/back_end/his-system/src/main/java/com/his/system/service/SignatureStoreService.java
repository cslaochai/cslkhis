package com.his.system.service;

import com.his.system.entity.BizEmrSignature;
import com.his.system.entity.SysSignCert;
import com.his.common.enums.SignSceneEnum;

import java.time.LocalDateTime;

/**
 * 签名落库的事务边界：写签名行 → 回写业务锚点 → 更新证书使用计数
 */
public interface SignatureStoreService {

    void insertAndAnchor(BizEmrSignature entity, SignableContentProvider provider, SignSceneEnum scene, SysSignCert cert);

    void updateVerifyResult(Long signId, Integer verifyStatus, LocalDateTime verifyTime);

    void updateInvalidate(Long signId, String reason, LocalDateTime time, Long operatorId, String operatorName);
}
