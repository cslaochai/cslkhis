package com.his.emr.service;

import com.his.system.service.SignableContentService;
import com.his.common.enums.SignSceneEnum;
import com.his.emr.entity.BizEmrSignature;
import com.his.spi.vo.SignCertBriefVO;

import java.time.LocalDateTime;

/**
 * 签名落库的事务边界：写签名行 → 回写业务锚点 → 更新证书使用计数
 */
public interface SignatureStoreService {

    /**
     * 一次签名的落库事务：写签名行 → 把签名锚点回写到被签业务对象 → 累加所用证书的使用次数与最近使用时间。
     * 用于签名动作算出签名值之后入库并锁定内容的收尾；三件事同事务，不允许只落一半。
     *
     * @param cert 本次签名使用的证书，可为 null（无证书场景照常落签名行，只是不累计使用数）
     */
    void insertAndAnchor(BizEmrSignature entity, SignableContentService provider, SignSceneEnum scene, SignCertBriefVO cert);

    /**
     * 回写一次验签结论：核查状态、核查时刻，并把累计核查次数加一，签名证据列一律不动。
     * 用于验签动作出完结论后留痕。
     */
    void updateVerifyResult(Long signId, Integer verifyStatus, LocalDateTime verifyTime);

    /**
     * 登记签名作废：只追加作废理由、作废时刻与作废人，历史签名内容保持原样。
     * 用于签名详情抽屉里作废签名的落库收尾。
     */
    void updateInvalidate(Long signId, String reason, LocalDateTime time, Long operatorId, String operatorName);
}
