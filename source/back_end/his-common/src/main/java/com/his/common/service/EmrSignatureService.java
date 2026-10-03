package com.his.common.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.dto.SignCommandDTO;
import com.his.common.dto.SignatureQueryPageDTO;
import com.his.common.vo.ObjectSignatureVO;
import com.his.common.vo.SignVerifyVO;
import com.his.common.vo.SignatureSummaryVO;
import com.his.common.vo.SignatureVO;

import java.util.List;

/**
 * 电子签名能力（his-common 通用层，不依赖任何业务模块）。
 */
public interface EmrSignatureService {

    /** 签名（业务动作自动触发，或管理员补签） */
    SignatureVO sign(SignCommandDTO command);

    /** 按签名ID验签 */
    SignVerifyVO verify(Long signId);

    /** 按对象验签（验该对象全部签名，含已作废的） */
    List<SignVerifyVO> verifyByBiz(Integer bizType, Long bizId);

    /** 作废签名（必须写理由；不改历史行，只追加作废信息） */
    SignatureVO invalidate(Long signId, String reason, Long operatorId, String operatorName);

    SignatureVO getById(Long id);

    IPage<SignatureVO> listPage(SignatureQueryPageDTO query);

    List<SignatureVO> listByBiz(Integer bizType, Long bizId);

    /** 某对象的签名情况（含签名链、能否补签） */
    ObjectSignatureVO objectStatus(Integer bizType, Long bizId);

    SignatureSummaryVO summary();

    /**
     * 最终生效的时间来源。
     *
     * <p>规则：配置为 2/3 但当前**没有对应的时间戳实现**时，一律降级成 1-本机时钟。
     * 这是"宁可承认不可信，也不谎报可信"的落点 —— 没有 TSA 却写 3，
     * 等于给一份可随手修改的时间盖上"可信时间戳"的章。
     */
    int effectiveTimeSource();
}
