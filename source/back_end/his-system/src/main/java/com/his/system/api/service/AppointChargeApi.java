package com.his.system.api.service;

import com.his.system.api.vo.BillBriefVO;
import com.his.system.api.vo.CancelCommandVO;
import com.his.system.api.vo.RegistChargeCommandVO;

import java.util.Collection;
import java.util.Map;

/**
 * 挂号侧对收费能力的引用点（SPI）。
 */
public interface AppointChargeApi {

    /**
     * 批量查询账单简要信息（挂号看板装配缴费状态用，禁止在循环里单条查）。
     *
     * @param billIds 账单ID集合
     * @return billId -> 简要信息
     */
    Map<Long, BillBriefVO> mapBillsByIds(Collection<Long> billIds);

    /**
     * 查询单张账单简要信息
     *
     * @return 不存在时返回 null
     */
    BillBriefVO getBill(Long billId);

    /**
     * 挂号费结算：记账（挂号费 + 诊查费）并立即出账。
     *
     * @return 已落库的账单简要信息；<b>免收时返回 null</b>（没有应收，也就没有账单）
     */
    BillBriefVO createRegistCharge(RegistChargeCommandVO command);

    /**
     * 挂号作废时联动处理账单 —— <b>退号不退钱是本系统里最容易漏掉的一环</b>。
     *
     * <p>口径：整单撤销 = 收进来的钱按原收款流水逐路退回 + 本账单的记账行全额红冲 + 账单作废，
     * 三件事必须在<b>同一个事务</b>里。只置挂号状态不处理钱，会留下「已退号 + 账单仍是已支付」
     * 的对账缺口；反过来只退钱不冲应收，收费台上就永远挂着这张收不到的账。
     *
     * @return true=本次确实撤销；false=没有账单或账单早已关闭（幂等跳过）
     */
    boolean cancelRegistCharge(CancelCommandVO command);
}
