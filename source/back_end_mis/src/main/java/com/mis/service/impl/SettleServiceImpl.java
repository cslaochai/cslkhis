package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageVO;
import com.mis.dto.BillQueryDTO;
import com.mis.dto.BizPageParam;
import com.mis.engine.SettleEngine;
import com.mis.entity.MssBill;
import com.mis.entity.MssFeeItem;
import com.mis.entity.MssPendingSettle;
import com.mis.mapper.MssBillMapper;
import com.mis.mapper.MssFeeItemMapper;
import com.mis.mapper.MssPendingSettleMapper;
import com.mis.service.SettleService;
import com.mis.vo.BillVO;
import com.mis.vo.DecVO;
import com.mis.vo.PendingSettleVO;
import com.mis.vo.SettleResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettleServiceImpl implements SettleService {

    private final MssPendingSettleMapper pendingMapper;
    private final MssBillMapper billMapper;
    private final MssFeeItemMapper feeItemMapper;
    private final SettleEngine settleEngine;

    @Override
    public PageVO<PendingSettleVO> pendingPage(int bizType, BizPageParam query) {
        Page<MssPendingSettle> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<MssPendingSettle> w = new LambdaQueryWrapper<>();
        w.eq(MssPendingSettle::getBizType, bizType)
                .eq(MssPendingSettle::getStatus, 1);
        if (hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            w.and(q -> q.like(MssPendingSettle::getPatientName, kw)
                    .or().like(MssPendingSettle::getBizNo, kw));
        }
        w.orderByAsc(MssPendingSettle::getId);
        Page<MssPendingSettle> res = pendingMapper.selectPage(page, w);
        List<PendingSettleVO> list = res.getRecords().stream().map(this::toPendingVO).toList();
        return PageVO.of(list, res.getTotal());
    }

    @Override
    public DecVO preview(String bizNo) {
        MssPendingSettle p = loadPending(bizNo);
        List<MssFeeItem> items = itemsOf(1, p.getId());
        return settleEngine.compute(items, p.getInsuranceType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SettleResultVO confirm(int bizType, String bizNo) {
        MssPendingSettle p = pendingMapper.selectOne(new LambdaQueryWrapper<MssPendingSettle>()
                .eq(MssPendingSettle::getBizNo, bizNo)
                .eq(MssPendingSettle::getBizType, bizType)
                .eq(MssPendingSettle::getStatus, 1)
                .last("limit 1"));
        if (p == null) {
            return SettleResultVO.fail("业务已结算或不存在");
        }
        List<MssFeeItem> srcItems = itemsOf(1, p.getId());
        DecVO d = settleEngine.compute(srcItems, p.getInsuranceType());

        String settleType = bizType == 2 ? "住院" : "门诊";
        MssBill bill = new MssBill();
        bill.setPendingId(p.getId());
        bill.setBizNo(p.getBizNo());
        bill.setAdmitDays(p.getAdmitDays());
        bill.setPatientName(p.getPatientName());
        bill.setIdCard(p.getIdCard());
        bill.setIdCardMask(p.getIdCardMask());
        bill.setInsuranceType(p.getInsuranceType());
        bill.setSettleType(settleType);
        bill.setDept(p.getDept());
        bill.setDoctor(p.getDoctor());
        bill.setSettleTime(LocalDateTime.now());
        bill.setStatus("已结算");
        bill.setTotalAmount(d.getTotal());
        bill.setInsurancePay(d.getInsurancePay());
        bill.setPersonalAccount(d.getPersonalAccount());
        bill.setCashPay(d.getCashPay());
        bill.setSelfPay(d.getSelfPay());
        bill.setDeductible(d.getDeductible());
        bill.setBasePool(d.getBasePool());
        billMapper.insert(bill);

        String prefix = bizType == 2 ? "ZY" : "YL";
        bill.setBillNo(prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%05d", bill.getId()));
        billMapper.updateById(bill);

        for (MssFeeItem src : srcItems) {
            MssFeeItem dst = new MssFeeItem();
            BeanUtils.copyProperties(src, dst);
            dst.setId(null);
            dst.setOwnerType(2);
            dst.setOwnerId(bill.getId());
            dst.setReversed(false);
            dst.setCreateTime(null);
            feeItemMapper.insert(dst);
        }

        p.setStatus(2);
        pendingMapper.updateById(p);

        return SettleResultVO.ok(toBillVO(bill, true));
    }

    @Override
    public PageVO<BillVO> billPage(BillQueryDTO q) {
        Page<MssBill> page = new Page<>(q.getPage(), q.getSize());
        LambdaQueryWrapper<MssBill> w = new LambdaQueryWrapper<>();
        if (hasText(q.getBillNo())) {
            w.like(MssBill::getBillNo, q.getBillNo().trim());
        }
        if (hasText(q.getPatientName())) {
            w.like(MssBill::getPatientName, q.getPatientName().trim());
        }
        if (hasText(q.getInsuranceType())) {
            w.eq(MssBill::getInsuranceType, q.getInsuranceType());
        }
        if (hasText(q.getSettleType())) {
            w.eq(MssBill::getSettleType, q.getSettleType());
        }
        if (hasText(q.getStatus())) {
            w.eq(MssBill::getStatus, q.getStatus());
        }
        w.orderByDesc(MssBill::getSettleTime);
        Page<MssBill> res = billMapper.selectPage(page, w);
        List<BillVO> list = res.getRecords().stream().map(b -> toBillVO(b, false)).toList();
        return PageVO.of(list, res.getTotal());
    }

    @Override
    public BillVO billDetail(Long id) {
        MssBill bill = billMapper.selectById(id);
        if (bill == null) {
            return null;
        }
        return toBillVO(bill, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SettleResultVO reverse(Long id) {
        MssBill bill = billMapper.selectById(id);
        if (bill == null) {
            return SettleResultVO.fail("结算单不存在");
        }
        if ("已退费".equals(bill.getStatus()) || "异常".equals(bill.getStatus())) {
            return SettleResultVO.fail("当前状态不可退费");
        }
        bill.setStatus("已退费");
        billMapper.updateById(bill);
        List<MssFeeItem> items = itemsOf(2, bill.getId());
        for (MssFeeItem it : items) {
            it.setReversed(true);
            feeItemMapper.updateById(it);
        }
        return SettleResultVO.ok(null);
    }

    // ------------------------------------------------------------------

    private MssPendingSettle loadPending(String bizNo) {
        return pendingMapper.selectOne(new LambdaQueryWrapper<MssPendingSettle>()
                .eq(MssPendingSettle::getBizNo, bizNo)
                .eq(MssPendingSettle::getStatus, 1)
                .last("limit 1"));
    }

    private List<MssFeeItem> itemsOf(int ownerType, Long ownerId) {
        return feeItemMapper.selectList(new LambdaQueryWrapper<MssFeeItem>()
                .eq(MssFeeItem::getOwnerType, ownerType)
                .eq(MssFeeItem::getOwnerId, ownerId)
                .orderByAsc(MssFeeItem::getId));
    }

    private PendingSettleVO toPendingVO(MssPendingSettle p) {
        PendingSettleVO vo = new PendingSettleVO();
        BeanUtils.copyProperties(p, vo);
        vo.setItems(itemsOf(1, p.getId()));
        return vo;
    }

    private BillVO toBillVO(MssBill b, boolean withItems) {
        BillVO vo = new BillVO();
        BeanUtils.copyProperties(b, vo);
        if (withItems) {
            vo.setItems(itemsOf(2, b.getId()));
        }
        return vo;
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
