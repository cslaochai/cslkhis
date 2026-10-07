package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.dto.FundAccountQueryPageDTO;
import com.his.charge.dto.FundTxnQueryPageDTO;
import com.his.charge.entity.BizFundAccount;
import com.his.charge.entity.BizFundAccountTxn;
import com.his.charge.mapper.BizFundAccountMapper;
import com.his.charge.mapper.BizFundAccountTxnMapper;
import com.his.charge.service.FundAccountService;
import com.his.charge.vo.FundAccountListVO;
import com.his.charge.vo.FundAccountTxnListVO;
import com.his.common.base.PageResult;
import com.his.common.enums.AccountOwnerTypeEnum;
import com.his.common.enums.AccountTxnTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 资金账户实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundAccountServiceImpl extends ServiceImpl<BizFundAccountMapper, BizFundAccount>
        implements FundAccountService {

    private static final int AMOUNT_SCALE = 2;
    private static final int ACCOUNT_STATUS_NORMAL = 1;
    private static final int TXN_STATUS_SUCCESS = 1;

    private static final int W_PATIENT_NO = 32;
    private static final int W_PATIENT_NAME = 50;
    private static final int W_TXN_NO = 32;
    private static final int W_CHANNEL_TXN_NO = 64;
    private static final int W_OPERATOR_NAME = 50;
    private static final int W_REMARK = 500;

    private final BizFundAccountTxnMapper bizFundAccountTxnMapper;
    private final RedisSequenceService redisSequenceService;

    private static List<FundAccountTxnListVO> toTxnVOList(List<BizFundAccountTxn> rows) {
        List<FundAccountTxnListVO> vos = new ArrayList<>(rows.size());
        for (BizFundAccountTxn row : rows) {
            FundAccountTxnListVO vo = new FundAccountTxnListVO();
            BeanUtils.copyProperties(row, vo);
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public BizFundAccount getOrCreate(Integer ownerType, Long ownerId, Long patientId,
                                      String patientNo, String patientName) {
        AccountOwnerTypeEnum type = AccountOwnerTypeEnum.fromCode(ownerType);
        if (type == null) {
            throw new BusinessException("账户主体类型不合法（1-患者 2-住院就诊次）");
        }
        if (ownerId == null || patientId == null) {
            throw new BusinessException("缺少账户主体");
        }
        BizFundAccount account = baseMapper.selectByOwner(ownerType, ownerId);
        if (account != null) {
            return account;
        }
        account = new BizFundAccount();
        account.setOwnerType(ownerType);
        account.setOwnerId(ownerId);
        account.setPatientId(patientId);
        account.setPatientNo(TextUtil.cut(patientNo, W_PATIENT_NO));
        account.setPatientName(TextUtil.cut(patientName, W_PATIENT_NAME));
        account.setBalance(BigDecimal.ZERO);
        account.setTotalRecharge(BigDecimal.ZERO);
        account.setTotalConsume(BigDecimal.ZERO);
        account.setAccountStatus(ACCOUNT_STATUS_NORMAL);
        try {
            this.save(account);
        } catch (DuplicateKeyException e) {
            // uk_account_owner 撞车=两个人同时给同一个人开户，用先建成的那个
            BizFundAccount exist = baseMapper.selectByOwner(ownerType, ownerId);
            if (exist == null) {
                throw new BusinessException("资金账户创建失败，请重试");
            }
            return exist;
        }
        log.info("[开户] {} 账户 owner={}/{} 患者 {}", type.getDesc(), ownerType, ownerId, patientName);
        return account;
    }

    @Override
    public BigDecimal balance(Integer ownerType, Long ownerId) {
        BizFundAccount account = baseMapper.selectByOwner(ownerType, ownerId);
        return account == null ? BigDecimal.ZERO : NumUtil.orZero(account.getBalance());
    }

    @Override
    public BigDecimal admissionBalance(Long admissionId) {
        if (admissionId == null) {
            return BigDecimal.ZERO;
        }
        return NumUtil.orZero(bizFundAccountTxnMapper.sumAdmissionBalance(admissionId));
    }

    @Override
    public PageResult<FundAccountListVO> accountListPage(FundAccountQueryPageDTO query) {
        LambdaQueryWrapper<BizFundAccount> wrapper = new LambdaQueryWrapper<BizFundAccount>()
                .eq(query.getOwnerType() != null, BizFundAccount::getOwnerType, query.getOwnerType())
                .eq(query.getAccountStatus() != null, BizFundAccount::getAccountStatus, query.getAccountStatus())
                .eq(query.getPatientId() != null, BizFundAccount::getPatientId, query.getPatientId())
                .and(TextUtil.hasText(query.getKeyword()), w -> w
                        .like(BizFundAccount::getPatientName, query.getKeyword())
                        .or().like(BizFundAccount::getPatientNo, query.getKeyword()))
                // create_time 大面积重复（同一批开户），必须补 id 二级键，否则分页重复/漏行
                .orderByDesc(BizFundAccount::getLastTxnTime)
                .orderByDesc(BizFundAccount::getId);
        Page<BizFundAccount> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<FundAccountListVO> records = new ArrayList<>(page.getRecords().size());
        for (BizFundAccount account : page.getRecords()) {
            FundAccountListVO vo = new FundAccountListVO();
            BeanUtils.copyProperties(account, vo);
            records.add(vo);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public PageResult<FundAccountTxnListVO> txnListPage(FundTxnQueryPageDTO query) {
        LambdaQueryWrapper<BizFundAccountTxn> wrapper = new LambdaQueryWrapper<BizFundAccountTxn>()
                .eq(query.getAccountId() != null, BizFundAccountTxn::getAccountId, query.getAccountId())
                .eq(query.getPatientId() != null, BizFundAccountTxn::getPatientId, query.getPatientId())
                .eq(query.getTxnType() != null, BizFundAccountTxn::getTxnType, query.getTxnType())
                .eq(query.getTxnStatus() != null, BizFundAccountTxn::getTxnStatus, query.getTxnStatus())
                .orderByDesc(BizFundAccountTxn::getTxnTime)
                .orderByDesc(BizFundAccountTxn::getId);
        Page<BizFundAccountTxn> page = bizFundAccountTxnMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), toTxnVOList(page.getRecords()));
    }

    @Override
    public List<FundAccountTxnListVO> listTxnByAccount(Long accountId) {
        return toTxnVOList(bizFundAccountTxnMapper.selectList(new LambdaQueryWrapper<BizFundAccountTxn>()
                .eq(BizFundAccountTxn::getAccountId, accountId)
                .orderByDesc(BizFundAccountTxn::getTxnTime)
                .orderByDesc(BizFundAccountTxn::getId)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizFundAccountTxn apply(FundTxnSpec spec) {
        if (spec == null) {
            throw new BusinessException("缺少账户流水");
        }
        AccountTxnTypeEnum type = AccountTxnTypeEnum.fromCode(spec.txnType());
        if (type == null) {
            throw new BusinessException("账户流水类型不合法");
        }
        BigDecimal signed = signedAmount(type, spec.amount());
        if (signed.signum() == 0) {
            throw new BusinessException("账户变动金额不能为 0");
        }

        // 乐观锁重试：最多 3 次
        int maxRetries = 3;
        BizFundAccount account = null;
        BigDecimal after = null;
        for (int i = 0; i < maxRetries; i++) {
            account = lockAccount(spec);
            if (account.getAccountStatus() != null && account.getAccountStatus() != ACCOUNT_STATUS_NORMAL) {
                throw new BusinessException("该账户已冻结，不收不抵");
            }
            after = NumUtil.orZero(account.getBalance()).add(signed).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
            if (after.signum() < 0) {
                // 余额是医院欠患者的钱，扣成负数等于凭空多收一笔，绝不能放行
                throw new BusinessException((AccountOwnerTypeEnum.ADMISSION.getCode().equals(account.getOwnerType())
                        ? "住院预交金" : "账户余额") + "不足，当前 " + NumUtil.orZero(account.getBalance()).toPlainString()
                        + "，本次需要 " + signed.abs().toPlainString());
            }

            // 乐观锁更新：UPDATE ... SET balance = balance + ?, version = version + 1 WHERE id = ? AND version = ?
            int updated = baseMapper.updateBalanceOptimistic(
                    account.getId(), signed, account.getVersion(), LocalDateTime.now());
            if (updated > 0) {
                // 成功，跳出重试循环
                break;
            }
            // 版本冲突，重试
            log.warn("[账户流水] 乐观锁冲突，第 {} 次重试 accountId={}", i + 1, account.getId());
            if (i == maxRetries - 1) {
                throw new BusinessException("账户并发冲突，请重试");
            }
        }

        BizFundAccountTxn txn = new BizFundAccountTxn();
        txn.setTxnNo(TextUtil.cut(redisSequenceService.generateFundTxnNo(), W_TXN_NO));
        txn.setAccountId(account.getId());
        txn.setPatientId(account.getPatientId());
        txn.setOwnerType(account.getOwnerType());
        txn.setOwnerId(account.getOwnerId());
        txn.setTxnType(type.getCode());
        txn.setAmount(signed);
        txn.setBalanceAfter(after);
        txn.setAdmissionId(spec.admissionId());
        txn.setBillId(spec.billId());
        txn.setPaymentTxnId(spec.paymentTxnId());
        txn.setPayMethod(spec.payMethod());
        txn.setChannelTxnNo(TextUtil.cut(spec.channelTxnNo(), W_CHANNEL_TXN_NO));
        txn.setOperatorId(UserUtils.getCurrentUser().getEmployeeId());
        txn.setOperatorName(TextUtil.cut(UserUtils.getCurrentUser().getRealName(), W_OPERATOR_NAME));
        txn.setTxnTime(LocalDateTime.now());
        txn.setTxnStatus(TXN_STATUS_SUCCESS);
        txn.setRemark(TextUtil.cut(spec.reason(), W_REMARK));
        bizFundAccountTxnMapper.insert(txn);

        log.info("[账户流水] {} 账户 {} {} 变动 ¥{} 后余额 ¥{}", txn.getTxnNo(), account.getId(),
                type.getDesc(), signed.toPlainString(), after.toPlainString());
        return txn;
    }

    /**
     * 行锁取账户：读余额、判够不够、写流水、更新缓存必须全在锁内，
     * 否则两个人同时刷同一个人的余额，两边都判够、扣完就是负数。
     */
    private BizFundAccount lockAccount(FundTxnSpec spec) {
        getOrCreate(spec.ownerType(), spec.ownerId(), spec.patientId(), spec.patientNo(), spec.patientName());
        BizFundAccount locked = baseMapper.selectByOwnerForUpdate(spec.ownerType(), spec.ownerId());
        if (locked == null) {
            throw new BusinessException("资金账户不存在");
        }
        return locked;
    }

    /**
     * 符号由类型决定（扣用类取负、入账类取正）；只有「6-手工调整」允许调用方自带正负，
     * 因为调整本来就双向。
     */
    private BigDecimal signedAmount(AccountTxnTypeEnum type, BigDecimal amount) {
        if (amount == null) {
            throw new BusinessException("缺少变动金额");
        }
        if (type == AccountTxnTypeEnum.MANUAL_ADJUST) {
            return amount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
        if (amount.signum() <= 0) {
            throw new BusinessException("变动金额必须大于 0，方向由流水类型决定");
        }
        BigDecimal abs = amount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        return type.debit() ? abs.negate() : abs;
    }
}
