package com.his.emr.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.spi.dto.SignCommandDTO;
import com.his.emr.dto.SignaturePageQueryDTO;
import com.his.emr.vo.ObjectSignatureVO;
import com.his.spi.vo.SignVerifyVO;
import com.his.emr.vo.SignatureSummaryVO;
import com.his.spi.vo.SignatureVO;

import java.util.List;

/**
 * 电子签名能力（his-system 通用层，不依赖任何业务模块）。
 */
public interface EmrSignatureService {

    /**
     * 签名记录分页：可按对象类型、签名场景、签名人、签名状态、验签结果与时间区间筛选。
     * 用于签名中心「签名记录」签的查询卡表格。
     */
    IPage<SignatureVO> listPage(SignaturePageQueryDTO query);

    /**
     * 签名（业务动作自动触发，或管理员补签）
     */
    SignatureVO sign(SignCommandDTO command);

    /**
     * 按签名ID验签
     */
    SignVerifyVO verify(Long signId);

    /**
     * 按对象验签（验该对象全部签名，含已作废的）
     */
    List<SignVerifyVO> verifyByBiz(Integer bizType, Long bizId);

    /**
     * 作废签名（必须写理由；不改历史行，只追加作废信息）
     */
    SignatureVO invalidate(Long signId, String reason, Long operatorId, String operatorName);

    /**
     * 单条签名详情：被签内容快照、摘要、签名证书与时间戳来源一并取回。
     * 用于签名记录点开一行后的「签名详情与验签」抽屉。
     */
    SignatureVO getById(Long id);

    /**
     * 一个业务对象的签名链：按签名序号升序给出该对象名下全部签名，作废的历史签一并列出。
     * 供被签文书、处方回看「谁在何时签过、为何作废」的证据核查环节取数。
     */
    List<SignatureVO> listByBiz(Integer bizType, Long bizId);

    /**
     * 某对象的签名情况（含签名链、能否补签）
     */
    ObjectSignatureVO objectStatus(Integer bizType, Long bizId);

    /**
     * 签名概览统计：签名与证书计数、各对象类型的签名覆盖率，并带时间来源与证书信任级别说明。
     * 用于签名中心页面顶部的概览卡片区。
     */
    SignatureSummaryVO summary();
}
