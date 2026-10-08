package com.his.charge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.charge.dto.FundAccountQueryPageDTO;
import com.his.charge.dto.FundTxnQueryPageDTO;
import com.his.charge.entity.BizFundAccount;
import com.his.charge.entity.BizFundAccountTxn;
import com.his.charge.vo.FundAccountListVO;
import com.his.charge.vo.FundAccountTxnListVO;
import com.his.common.base.PageResult;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 资金账户服务（L3）：门诊余额与住院预交金统一建模，差别只在 owner_type。
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
 */
    record FundTxnSpec(Integer ownerType, Long ownerId, Long patientId, String patientNo, String patientName,
                       Integer txnType, BigDecimal amount, Long admissionId, Long billId, Long paymentTxnId,
                       Integer payMethod, String channelTxnNo, String reason) implements Serializable {
    }
}
