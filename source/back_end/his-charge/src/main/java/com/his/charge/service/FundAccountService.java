package com.his.charge.service;

import com.his.charge.dto.FundAccountQueryPageDTO;
import com.his.charge.dto.FundTxnQueryPageDTO;
import com.his.charge.entity.BizFundAccount;
import com.his.charge.entity.BizFundAccountTxn;
import com.his.charge.vo.FundAccountListVO;
import com.his.charge.vo.FundAccountTxnListVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 资金账户服务（L3）：门诊余额与住院预交金统一建模，差别只在 {@code owner_type}。
 *
 * <p><b>本层是"钱存在院内"这件事的唯一账本</b>：余额不是患者口袋里的现金，
 * 是医院欠患者的可退款项，所以它的进出必须逐笔留痕（资金账户流水），
 * 余额永远 {@code SUM(amount)} 得出来，资金账户.balance 只是行锁保护下的缓存。
 */
public interface FundAccountService extends IService<BizFundAccount> {

    /**
     * 取账户，没有就开（按 owner 幂等，撞唯一键不会开第二个）
     */
    BizFundAccount getOrCreate(Integer ownerType, Long ownerId, Long patientId, String patientNo, String patientName);

    /**
     * 账户余额（按主体查，不要求账户已存在；没开过户就是 0）
     */
    BigDecimal balance(Integer ownerType, Long ownerId);

    /**
     * 某次住院的预交金余额（欠费管控按入院算，不按人算）
     */
    BigDecimal admissionBalance(Long admissionId);

    /**
     * 账户台账分页（门诊余额 / 住院预交金）
     */
    PageResult<FundAccountListVO> accountListPage(FundAccountQueryPageDTO query);

    /**
     * 账户流水分页（按账户或按患者跨账户）
     */
    PageResult<FundAccountTxnListVO> txnListPage(FundTxnQueryPageDTO query);

    /**
     * 某账户的流水（账户详情弹框一次给全）
     */
    List<FundAccountTxnListVO> listTxnByAccount(Long accountId);

    /**
     * 记一笔账户流水并同步余额缓存。扣用类余额不足直接拒绝，绝不透支。
     *
     * @return 本笔账户流水（含流水号，余额支付时它要作为渠道号写进支付流水）
     */
    BizFundAccountTxn apply(FundTxnSpec spec);

    /**
     * 一笔账户变动。
     *
     * @param txnType      流水类型（字典 {@code his_account_txn_type}，决定金额符号）
     * @param amount       变动绝对额（手工调整允许自带正负）
     * @param billId       余额抵扣/退差入账时关联的账单
     * @param paymentTxnId 与该笔账户流水配对的支付流水ID（余额支付场景，两者必须同时存在）
     */
    record FundTxnSpec(Integer ownerType, Long ownerId, Long patientId, String patientNo, String patientName,
                       Integer txnType, BigDecimal amount, Long admissionId, Long billId, Long paymentTxnId,
                       Integer payMethod, String channelTxnNo, String reason) implements Serializable {
    }
}
