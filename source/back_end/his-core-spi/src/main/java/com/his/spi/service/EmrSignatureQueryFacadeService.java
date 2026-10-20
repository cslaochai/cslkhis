package com.his.spi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.spi.dto.SignaturePageQueryDTO;
import com.his.spi.vo.ObjectSignatureVO;
import com.his.spi.vo.SignVerifyVO;
import com.his.spi.vo.SignatureSummaryVO;
import com.his.spi.vo.SignatureVO;

import java.util.List;

/**
 * 签名记录查询端口：签名中心页面读「签名事实」的取数出口，事实仍由病历域持有。
 *
 * <p>与 {@link EmrSignatureFacadeService} 分工：那边只承诺发起签名与作废两个动作，
 * 本端口只承诺查询与验签，不提供任何写入业务内容的通道。
 */
public interface EmrSignatureQueryFacadeService {

    /**
     * 签名记录分页：按对象类型、场景、签名人、签名状态、验签结果与时间区间筛选。
     * 用于签名中心「签名记录」签的查询卡表格。
     */
    IPage<SignatureVO> listPage(SignaturePageQueryDTO query);

    /**
     * 单条签名详情：含被签内容快照全文、摘要、签名证书与时间戳来源。
     * 用于签名中心「签名记录」签点开一行后的「签名详情与验签」抽屉。
     */
    SignatureVO getById(Long id);

    /**
     * 一个业务对象的签名链：按签名序号升序列出全部签名，含已作废的历史签。
     * 供被签文书/处方回看「谁在何时签过、后续为何作废」的证据核查环节取数。
     */
    List<SignatureVO> listByBiz(Integer bizType, Long bizId);

    /**
     * 一个业务对象的签名情况：当前锚点状态、签名链，以及能否补签与不能补签的原因。
     * 供发起补签前先判断该对象还缺哪一环签名的核查节点使用。
     */
    ObjectSignatureVO objectStatus(Integer bizType, Long bizId);

    /**
     * 签名概览：有效/作废/未校验/验签失败的计数与各对象类型的签名覆盖率，并附时间来源与证书信任级别说明。
     * 用于签名中心页面顶部的概览卡片区。
     */
    SignatureSummaryVO summary();

    /**
     * 单条验签：签名值能否验通、当前内容与签名时摘要是否一致，两个结论各自独立，并把核查结果回写留痕。
     * 用于「签名详情与验签」抽屉上的验签按钮。
     */
    SignVerifyVO verify(Long signId);

    /**
     * 按业务对象批量验签：一次核完该对象名下全部签名，含已作废的。
     * 用于门诊药房处方审核的「处方详情」抽屉里核对处方签名。
     */
    List<SignVerifyVO> verifyByBiz(Integer bizType, Long bizId);
}
