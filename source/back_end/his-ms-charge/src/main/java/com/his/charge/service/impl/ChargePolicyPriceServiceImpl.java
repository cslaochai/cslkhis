package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.charge.dto.PolicyPriceChangeDTO;
import com.his.charge.dto.PolicyPriceStatusDTO;
import com.his.charge.entity.SysChargePolicy;
import com.his.charge.entity.SysChargePolicyPrice;
import com.his.charge.entity.SysPriceChangeHistory;
import com.his.charge.mapper.SysChargePolicyMapper;
import com.his.charge.mapper.SysChargePolicyPriceMapper;
import com.his.charge.mapper.SysPriceChangeHistoryMapper;
import com.his.charge.service.ChargePolicyPriceService;
import com.his.charge.vo.PolicyPriceAdminVO;
import com.his.common.entity.CurrentUser;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.common.utils.UserUtils;
import com.his.spi.vo.PolicyPriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 收费政策价服务实现。
 *
 * <p>调价历史口径与五类价表一致（sys_price_change_history），itemType 固定 POLICY，
 * itemCode 存「政策编码/档位码」，itemName 存「政策名·档位名」，金额变化记全额。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargePolicyPriceServiceImpl implements ChargePolicyPriceService {

    /** 调价历史里的政策价类型标识（区别于 DRUG/CONSUMABLE/INSPECTION/LABORATORY/TREATMENT） */
    public static final String HISTORY_ITEM_TYPE = "POLICY";

    private final SysChargePolicyMapper sysChargePolicyMapper;
    private final SysChargePolicyPriceMapper sysChargePolicyPriceMapper;
    private final SysPriceChangeHistoryMapper sysPriceChangeHistoryMapper;

    @Override
    public PolicyPriceVO policyPrice(String policyCode, Integer tierCode) {
        if (!TextUtil.hasText(policyCode) || tierCode == null) {
            return null;
        }
        SysChargePolicy policy = sysChargePolicyMapper.selectOne(
                new LambdaQueryWrapper<SysChargePolicy>().eq(SysChargePolicy::getPolicyCode, policyCode));
        if (policy == null || !isEnabled(policy.getStatus())) {
            return null;
        }
        SysChargePolicyPrice price = sysChargePolicyPriceMapper.selectOne(
                new LambdaQueryWrapper<SysChargePolicyPrice>()
                        .eq(SysChargePolicyPrice::getPolicyCode, policyCode)
                        .eq(SysChargePolicyPrice::getTierCode, tierCode));
        if (price == null || !isEnabled(price.getStatus())) {
            return null;
        }
        PolicyPriceVO vo = new PolicyPriceVO();
        vo.setPolicyCode(policy.getPolicyCode());
        vo.setPolicyName(policy.getPolicyName());
        vo.setItemCode(policy.getItemCode());
        vo.setItemName(policy.getItemName());
        vo.setUnit(policy.getUnit());
        vo.setCatalogType(policy.getCatalogType());
        vo.setTierCode(price.getTierCode());
        vo.setTierLabel(price.getTierLabel());
        vo.setYbCode(price.getYbCode());
        vo.setAmount(price.getAmount());
        vo.setSurcharge(price.getSurcharge());
        return vo;
    }

    @Override
    public List<PolicyPriceAdminVO> listAdmin() {
        List<SysChargePolicy> policies = sysChargePolicyMapper.selectList(null);
        List<SysChargePolicyPrice> prices = sysChargePolicyPriceMapper.selectList(null);
        Map<String, SysChargePolicy> policyMap = policies.stream()
                .collect(Collectors.toMap(SysChargePolicy::getPolicyCode, Function.identity(), (a, b) -> a));
        List<PolicyPriceAdminVO> rows = new ArrayList<>(prices.size());
        for (SysChargePolicyPrice price : prices) {
            SysChargePolicy policy = policyMap.get(price.getPolicyCode());
            if (policy == null) {
                // 价目行挂着孤儿政策：不吞掉，管理页要能看见并处理
                log.warn("[政策价] 价目行 {} 挂着不存在的政策 {}，请检查数据", price.getId(), price.getPolicyCode());
                continue;
            }
            PolicyPriceAdminVO vo = new PolicyPriceAdminVO();
            vo.setPolicyId(policy.getId());
            vo.setPolicyCode(policy.getPolicyCode());
            vo.setPolicyName(policy.getPolicyName());
            vo.setItemCode(policy.getItemCode());
            vo.setItemName(policy.getItemName());
            vo.setUnit(policy.getUnit());
            vo.setCatalogType(policy.getCatalogType());
            vo.setYbNote(policy.getYbNote());
            vo.setPolicyStatus(policy.getStatus());
            vo.setPolicyRemark(policy.getRemark());
            vo.setPriceId(price.getId());
            vo.setTierCode(price.getTierCode());
            vo.setTierLabel(price.getTierLabel());
            vo.setYbCode(price.getYbCode());
            vo.setAmount(price.getAmount());
            vo.setSurcharge(price.getSurcharge());
            vo.setStatus(price.getStatus());
            vo.setRemark(price.getRemark());
            rows.add(vo);
        }
        rows.sort(Comparator.comparing(PolicyPriceAdminVO::getPolicyCode)
                .thenComparing(PolicyPriceAdminVO::getTierCode));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PolicyPriceAdminVO changePrice(PolicyPriceChangeDTO changeDTO) {
        SysChargePolicyPrice price = sysChargePolicyPriceMapper.selectById(changeDTO.getPriceId());
        if (price == null) {
            throw new BusinessException("价目行不存在，无法调价");
        }
        if (price.getAmount().compareTo(changeDTO.getAmount()) == 0
                && (changeDTO.getSurcharge() == null
                || Objects.equals(price.getSurcharge(), changeDTO.getSurcharge()))) {
            throw new BusinessException("新价格与当前价格相同，无需调价");
        }
        SysChargePolicy policy = sysChargePolicyMapper.selectOne(
                new LambdaQueryWrapper<SysChargePolicy>().eq(SysChargePolicy::getPolicyCode, price.getPolicyCode()));
        if (policy == null) {
            throw new BusinessException("价目行挂着不存在的政策：" + price.getPolicyCode());
        }

        int rows = sysChargePolicyPriceMapper.updatePrice(changeDTO.getPriceId(),
                changeDTO.getAmount(), changeDTO.getSurcharge());
        if (rows == 0) {
            throw new BusinessException("调价失败：未匹配到可更新的记录");
        }

        // 调价留痕：与五类价表同表，itemType=POLICY
        SysPriceChangeHistory history = new SysPriceChangeHistory();
        history.setItemType(HISTORY_ITEM_TYPE);
        history.setItemId(price.getId());
        history.setItemCode(price.getPolicyCode() + "/" + price.getTierCode());
        history.setItemName(policy.getPolicyName() + "·" + price.getTierLabel() + "档");
        history.setOldPrice(price.getAmount());
        history.setNewPrice(changeDTO.getAmount());
        history.setChangeReason(changeDTO.getReason());
        fillOperator(history);
        // DATETIME(0)：截断到秒，避免四舍五入
        history.setChangeTime(TimeUtil.nowSeconds());
        sysPriceChangeHistoryMapper.insert(history);

        log.info("[政策价] 调价成功 policy={}/{} {} -> {}（加收 {}），原因={}",
                price.getPolicyCode(), price.getTierCode(), price.getAmount(),
                changeDTO.getAmount(),
                changeDTO.getSurcharge() == null ? "未动" : changeDTO.getSurcharge(),
                changeDTO.getReason());
        return listAdmin().stream()
                .filter(r -> Objects.equals(r.getPriceId(), price.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("调价后回查失败"));
    }

    @Override
    public void changeStatus(PolicyPriceStatusDTO statusDTO) {
        SysChargePolicyPrice price = sysChargePolicyPriceMapper.selectById(statusDTO.getPriceId());
        if (price == null) {
            throw new BusinessException("价目行不存在");
        }
        if (Objects.equals(price.getStatus(), statusDTO.getStatus())) {
            throw new BusinessException(statusDTO.getStatus() == 1 ? "该档位已是启用状态" : "该档位已是停用状态");
        }
        price.setStatus(statusDTO.getStatus());
        int rows = sysChargePolicyPriceMapper.updateById(price);
        if (rows == 0) {
            throw new BusinessException("状态更新失败：未匹配到可更新的记录");
        }
        log.info("[政策价] 档位启停 policy={}/{} status={}",
                price.getPolicyCode(), price.getTierCode(), statusDTO.getStatus());
    }

    private boolean isEnabled(Integer status) {
        return status != null && status == 1;
    }

    /**
     * 操作人口径与 PriceServiceImpl.fillOperator 一致：一律员工ID，姓名取 realName。
     */
    private void fillOperator(SysPriceChangeHistory history) {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (Objects.isNull(currentUser)) {
            return;
        }
        history.setOperatorId(currentUser.getEmployeeId());
        history.setOperatorName(TextUtil.hasText(currentUser.getRealName()) ? currentUser.getRealName() : null);
    }
}
