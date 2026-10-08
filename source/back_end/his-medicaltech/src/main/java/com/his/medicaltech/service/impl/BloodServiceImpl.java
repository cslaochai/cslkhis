package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.DictType;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.medicaltech.dto.BloodDTO;
import com.his.medicaltech.entity.BizBloodCrossmatch;
import com.his.medicaltech.entity.BizBloodInventory;
import com.his.medicaltech.entity.BizBloodStockLog;
import com.his.medicaltech.enums.*;
import com.his.medicaltech.mapper.BizBloodCrossmatchMapper;
import com.his.medicaltech.mapper.BizBloodInventoryMapper;
import com.his.medicaltech.mapper.BizBloodStockLogMapper;
import com.his.medicaltech.service.BloodService;
import com.his.medicaltech.vo.BloodVO;
import com.his.system.entity.CurrentUser;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 血库储血台账服务
 */
@Service
@RequiredArgsConstructor
public class BloodServiceImpl extends ServiceImpl<BizBloodInventoryMapper, BizBloodInventory> implements BloodService {


    private final BizBloodInventoryMapper bizBloodInventoryMapper;
    private final BizBloodCrossmatchMapper bizBloodCrossmatchMapper;
    private final BizBloodStockLogMapper bizBloodStockLogMapper;
    private final DictCacheService dictCacheService;

    // 库存台账

    @Transactional(rollbackFor = Exception.class)
    public BizBloodInventory inbound(BloodDTO.Inbound dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        String bagNo = dto.getBagNo().trim();
        if (bizBloodInventoryMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .eq(BizBloodInventory::getBagNo, bagNo)) > 0) {
            throw new BusinessException("血袋号已存在：" + bagNo + "（血袋号全局唯一，重复入库先查台账）");
        }
        BizBloodInventory b = new BizBloodInventory();
        BeanUtils.copyProperties(dto, b);
        b.setBagNo(bagNo);
        b.setId(null);
        if (b.getRhType() == null) {
            b.setRhType(RhTypeEnum.POSITIVE.getCode());
        }
        if (b.getSourceType() == null) {
            b.setSourceType(BloodSourceTypeEnum.STATION.getCode());
        }
        b.setAboVerify(dto.getAboVerify() != null && dto.getAboVerify() == YesOrNoEnum.YES.getCode()
                ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
        b.setStatus(BloodInventoryStatusEnum.IN_STOCK.getCode());
        b.setInboundBy(operatorUser.getRealName());
        b.setInboundTime(LocalDateTime.now().withNano(0));
        bizBloodInventoryMapper.insert(b);
        writeLog(bagNo, BloodStockLogBizTypeEnum.INBOUND.getCode(), null, BloodInventoryStatusEnum.IN_STOCK.getCode(), null, null);
        return b;
    }

    public PageResult<BloodVO.InventoryVO> inventoryPage(BloodDTO.InventoryQuery q) {
        LambdaQueryWrapper<BizBloodInventory> w = new LambdaQueryWrapper<>();
        w.eq(TextUtil.hasText(q.getBagNo()), BizBloodInventory::getBagNo, TextUtil.trim(q.getBagNo()))
                .eq(q.getBloodType() != null, BizBloodInventory::getBloodType, q.getBloodType())
                .eq(q.getRhType() != null, BizBloodInventory::getRhType, q.getRhType())
                .eq(q.getComponentType() != null, BizBloodInventory::getComponentType, q.getComponentType())
                .eq(q.getStatus() != null, BizBloodInventory::getStatus, q.getStatus())
                .like(TextUtil.hasText(q.getStorageLoc()), BizBloodInventory::getStorageLoc, TextUtil.trim(q.getStorageLoc()));
        if (q.getExpireWithinDays() != null) {
            w.isNotNull(BizBloodInventory::getExpireDate)
                    .le(BizBloodInventory::getExpireDate, LocalDate.now().plusDays(q.getExpireWithinDays()));
        }
        w.orderByAsc(BizBloodInventory::getExpireDate)
                .orderByDesc(BizBloodInventory::getId);
        Page<BizBloodInventory> page = bizBloodInventoryMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), w);
        List<BloodVO.InventoryVO> vos = new ArrayList<>();
        for (BizBloodInventory b : page.getRecords()) {
            vos.add(toInvVo(b));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    private BloodVO.CrossmatchVO toCmVo(BizBloodCrossmatch c) {
        BloodVO.CrossmatchVO vo = new BloodVO.CrossmatchVO();
        BeanUtils.copyProperties(c, vo);
        vo.setPatientBloodTypeText(dictCacheService.getDicDataLabel(DictType.BLOOD_TYPE, c.getPatientBloodType()));
        vo.setPatientRhTypeText(dictCacheService.getDicDataLabel(DictType.BLOOD_RH, c.getPatientRhType()));
        vo.setBagBloodTypeText(dictCacheService.getDicDataLabel(DictType.BLOOD_TYPE, c.getBagBloodType()));
        vo.setComponentTypeText(dictCacheService.getDicDataLabel(DictType.BLOOD_COMPONENT, c.getComponentType()));
        vo.setMethodText(dictCacheService.getDicDataLabel(DictType.CROSSMATCH_METHOD, c.getMethod()));
        vo.setResultText(dictCacheService.getDicDataLabel(DictType.CROSSMATCH_RESULT, c.getResult()));
        vo.setStatusText(dictCacheService.getDicDataLabel(DictType.CROSSMATCH_STATUS, c.getStatus()));
        return vo;
    }

    public BloodVO.InventoryVO toInvVo(BizBloodInventory b) {
        BloodVO.InventoryVO vo = new BloodVO.InventoryVO();
        BeanUtils.copyProperties(b, vo);
        vo.setBloodTypeText(dictCacheService.getDicDataLabel(DictType.BLOOD_TYPE, b.getBloodType()));
        vo.setRhTypeText(dictCacheService.getDicDataLabel(DictType.BLOOD_RH, b.getRhType()));
        vo.setComponentTypeText(dictCacheService.getDicDataLabel(DictType.BLOOD_COMPONENT, b.getComponentType()));
        vo.setStatusText(dictCacheService.getDicDataLabel(DictType.BLOOD_INVENTORY_STATUS, b.getStatus()));
        vo.setSourceTypeText(dictCacheService.getDicDataLabel(DictType.BLOOD_SOURCE_TYPE, b.getSourceType()));
        vo.setAboVerifyText(b.getAboVerify() != null && b.getAboVerify() == YesOrNoEnum.YES.getCode() ? "已复核" : "未复核");
        if (b.getExpireDate() != null) {
            vo.setExpireDays((int) ChronoUnit.DAYS.between(LocalDate.now(), b.getExpireDate()));
        }
        return vo;
    }

    public BloodVO.StatsVO inventoryStats() {
        BloodVO.StatsVO vo = new BloodVO.StatsVO();
        vo.setInStock(bizBloodInventoryMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .eq(BizBloodInventory::getStatus, BloodInventoryStatusEnum.IN_STOCK.getCode())));
        vo.setReserved(bizBloodInventoryMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .eq(BizBloodInventory::getStatus, BloodInventoryStatusEnum.RESERVED.getCode())));
        vo.setIssued(bizBloodInventoryMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .eq(BizBloodInventory::getStatus, BloodInventoryStatusEnum.ISSUED.getCode())));
        vo.setExpireSoon(bizBloodInventoryMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .in(BizBloodInventory::getStatus, BloodInventoryStatusEnum.IN_STOCK.getCode(), BloodInventoryStatusEnum.RESERVED.getCode())
                .isNotNull(BizBloodInventory::getExpireDate)
                .le(BizBloodInventory::getExpireDate, LocalDate.now().plusDays(7))));
        LocalDateTime dayStart = TimeUtil.dayStart(LocalDate.now());
        vo.setTodayIn(bizBloodInventoryMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .ge(BizBloodInventory::getInboundTime, dayStart)));
        vo.setTodayOut(bizBloodInventoryMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .ge(BizBloodInventory::getOutboundTime, dayStart)));
        vo.setPendingMatch(bizBloodCrossmatchMapper.selectCount(new LambdaQueryWrapper<BizBloodCrossmatch>()
                .eq(BizBloodCrossmatch::getStatus, CrossmatchOrderStatusEnum.PENDING.getCode())));

        List<BloodVO.TypeCount> byType = new ArrayList<>();
        for (BloodTypeEnum type : BloodTypeEnum.values()) {
            BloodVO.TypeCount c = new BloodVO.TypeCount();
            c.setBloodType(type.getCode());
            c.setBloodTypeText(dictCacheService.getDicDataLabel(DictType.BLOOD_TYPE, type.getCode()));
            List<BizBloodInventory> bags = bizBloodInventoryMapper.selectList(new LambdaQueryWrapper<BizBloodInventory>()
                    .eq(BizBloodInventory::getStatus, BloodInventoryStatusEnum.IN_STOCK.getCode())
                    .eq(BizBloodInventory::getBloodType, type.getCode())
                    .select(BizBloodInventory::getVolume));
            c.setBagCount(bags.size());
            c.setVolumeTotal(bags.stream().mapToInt(b -> b.getVolume() == null ? 0 : b.getVolume()).sum());
            byType.add(c);
        }
        vo.setByBloodType(byType);
        return vo;
    }

    // 袋操作：预留 / 取消预留 / 发血 / 报废 / 退回

    @Transactional(rollbackFor = Exception.class)
    public void reserve(BloodDTO.BagAction dto) {
        BizBloodInventory b = requireBag(dto.getBagId());
        if (b.getStatus() != BloodInventoryStatusEnum.IN_STOCK.getCode()) {
            throw new BusinessException("仅「在库」血袋可预留（当前：" + dictCacheService.getDicDataLabel(DictType.BLOOD_INVENTORY_STATUS, b.getStatus()) + "）");
        }
        b.setStatus(BloodInventoryStatusEnum.RESERVED.getCode());
        bizBloodInventoryMapper.updateById(b);
        writeLog(b.getBagNo(), BloodStockLogBizTypeEnum.RESERVE.getCode(), BloodInventoryStatusEnum.IN_STOCK.getCode(), BloodInventoryStatusEnum.RESERVED.getCode(), dto.getApplyNo(), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelReserve(BloodDTO.BagAction dto) {
        BizBloodInventory b = requireBag(dto.getBagId());
        if (b.getStatus() != BloodInventoryStatusEnum.RESERVED.getCode()) {
            throw new BusinessException("仅「已预留」血袋可取消预留（当前：" + dictCacheService.getDicDataLabel(DictType.BLOOD_INVENTORY_STATUS, b.getStatus()) + "）");
        }
        b.setStatus(BloodInventoryStatusEnum.IN_STOCK.getCode());
        bizBloodInventoryMapper.updateById(b);
        writeLog(b.getBagNo(), BloodStockLogBizTypeEnum.CANCEL_RESERVE.getCode(), BloodInventoryStatusEnum.RESERVED.getCode(), BloodInventoryStatusEnum.IN_STOCK.getCode(), null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void issue(BloodDTO.BagAction dto) {
        if (!TextUtil.hasText(dto.getApplyNo())) {
            throw new BusinessException("发血必须关联用血申请单号");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizBloodInventory b = requireBag(dto.getBagId());
        if (b.getStatus() != BloodInventoryStatusEnum.RESERVED.getCode()) {
            throw new BusinessException("发血前血袋必须「已预留」（预留由配血复核相合产生）——当前："
                    + dictCacheService.getDicDataLabel(DictType.BLOOD_INVENTORY_STATUS, b.getStatus()));
        }
        b.setStatus(BloodInventoryStatusEnum.ISSUED.getCode());
        b.setApplyNo(dto.getApplyNo().trim());
        b.setOutboundBy(operatorUser.getRealName());
        b.setOutboundTime(LocalDateTime.now().withNano(0));
        bizBloodInventoryMapper.updateById(b);
        writeLog(b.getBagNo(), BloodStockLogBizTypeEnum.ISSUE.getCode(), BloodInventoryStatusEnum.RESERVED.getCode(), BloodInventoryStatusEnum.ISSUED.getCode(), b.getApplyNo(), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void scrap(BloodDTO.BagAction dto) {
        if (!TextUtil.hasText(dto.getReason())) {
            throw new BusinessException("报废必须填写原因");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizBloodInventory b = requireBag(dto.getBagId());
        if (b.getStatus() != BloodInventoryStatusEnum.IN_STOCK.getCode() && b.getStatus() != BloodInventoryStatusEnum.RESERVED.getCode()) {
            throw new BusinessException("仅「在库 / 已预留」血袋可报废（当前："
                    + dictCacheService.getDicDataLabel(DictType.BLOOD_INVENTORY_STATUS, b.getStatus()) + "）");
        }
        int from = b.getStatus();
        b.setStatus(BloodInventoryStatusEnum.SCRAPPED.getCode());
        b.setOutboundBy(operatorUser.getRealName());
        b.setOutboundTime(LocalDateTime.now().withNano(0));
        bizBloodInventoryMapper.updateById(b);
        writeLog(b.getBagNo(), BloodStockLogBizTypeEnum.SCRAP.getCode(), from, BloodInventoryStatusEnum.SCRAPPED.getCode(), null, dto.getReason());
    }

    @Transactional(rollbackFor = Exception.class)
    public void returnBag(BloodDTO.BagAction dto) {
        if (!TextUtil.hasText(dto.getReason())) {
            throw new BusinessException("退回必须填写原因");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizBloodInventory b = requireBag(dto.getBagId());
        if (b.getStatus() != BloodInventoryStatusEnum.IN_STOCK.getCode() && b.getStatus() != BloodInventoryStatusEnum.RESERVED.getCode()) {
            throw new BusinessException("仅「在库 / 已预留」血袋可退回（当前："
                    + dictCacheService.getDicDataLabel(DictType.BLOOD_INVENTORY_STATUS, b.getStatus()) + "）");
        }
        int from = b.getStatus();
        b.setStatus(BloodInventoryStatusEnum.RETURNED.getCode());
        b.setOutboundBy(operatorUser.getRealName());
        b.setOutboundTime(LocalDateTime.now().withNano(0));
        bizBloodInventoryMapper.updateById(b);
        writeLog(b.getBagNo(), BloodStockLogBizTypeEnum.RETURN.getCode(), from, BloodInventoryStatusEnum.RETURNED.getCode(), null, dto.getReason());
    }

    // 交叉配血

    public PageResult<BloodVO.CrossmatchVO> crossmatchPage(BloodDTO.CrossmatchQuery q) {
        LambdaQueryWrapper<BizBloodCrossmatch> w = new LambdaQueryWrapper<>();
        w.eq(TextUtil.hasText(q.getMatchNo()), BizBloodCrossmatch::getMatchNo, TextUtil.trim(q.getMatchNo()))
                .eq(TextUtil.hasText(q.getBagNo()), BizBloodCrossmatch::getBagNo, TextUtil.trim(q.getBagNo()))
                .like(TextUtil.hasText(q.getPatientName()), BizBloodCrossmatch::getPatientName, TextUtil.trim(q.getPatientName()))
                .eq(q.getStatus() != null, BizBloodCrossmatch::getStatus, q.getStatus())
                .eq(q.getResult() != null, BizBloodCrossmatch::getResult, q.getResult())
                .orderByDesc(BizBloodCrossmatch::getId);
        Page<BizBloodCrossmatch> page = bizBloodCrossmatchMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), w);
        List<BloodVO.CrossmatchVO> vos = new ArrayList<>();
        for (BizBloodCrossmatch c : page.getRecords()) {
            vos.add(toCmVo(c));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Transactional(rollbackFor = Exception.class)
    public BloodVO.CrossmatchVO crossmatchCreate(BloodDTO.CrossmatchCreate dto) {
        BizBloodInventory bag = bizBloodInventoryMapper.selectOne(new LambdaQueryWrapper<BizBloodInventory>()
                .eq(BizBloodInventory::getBagNo, dto.getBagNo().trim()).last("LIMIT 1"));
        if (bag == null) {
            throw new BusinessException("血袋不存在：" + dto.getBagNo());
        }
        if (bag.getStatus() != BloodInventoryStatusEnum.IN_STOCK.getCode() && bag.getStatus() != BloodInventoryStatusEnum.RESERVED.getCode()) {
            throw new BusinessException("血袋当前状态不可配血（" + dictCacheService.getDicDataLabel(DictType.BLOOD_INVENTORY_STATUS, bag.getStatus()) + "）");
        }
        long active = bizBloodCrossmatchMapper.selectCount(new LambdaQueryWrapper<BizBloodCrossmatch>()
                .eq(BizBloodCrossmatch::getBagNo, bag.getBagNo())
                .in(BizBloodCrossmatch::getStatus, CrossmatchOrderStatusEnum.PENDING.getCode(),
                        CrossmatchOrderStatusEnum.MATCHED.getCode(), CrossmatchOrderStatusEnum.VERIFIED.getCode()));
        if (active > 0) {
            throw new BusinessException("该血袋已有在途配血单（待配血/已配血/已复核），不可重复开单");
        }
        BizBloodCrossmatch c = new BizBloodCrossmatch();
        BeanUtils.copyProperties(dto, c);
        c.setId(null);
        c.setBagNo(bag.getBagNo());
        c.setBagBloodType(bag.getBloodType());
        c.setBagRhType(bag.getRhType());
        c.setComponentType(bag.getComponentType());
        c.setVolume(bag.getVolume());
        c.setStatus(CrossmatchOrderStatusEnum.PENDING.getCode());
        c.setMethod(CrossmatchMethodEnum.POLYBRENE.getCode());
        c.setMatchNo(nextMatchNo());
        bizBloodCrossmatchMapper.insert(c);
        return toCmVo(c);
    }

    @Transactional(rollbackFor = Exception.class)
    public void crossmatchExecute(BloodDTO.CrossmatchExecute dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizBloodCrossmatch c = requireCm(dto.getMatchId());
        if (c.getStatus() != CrossmatchOrderStatusEnum.PENDING.getCode()) {
            throw new BusinessException("仅「待配血」可执行配血（当前：" + dictCacheService.getDicDataLabel(DictType.CROSSMATCH_STATUS, c.getStatus()) + "）");
        }
        c.setMethod(dto.getMethod() == null ? CrossmatchMethodEnum.POLYBRENE.getCode() : dto.getMethod());
        c.setResult(dto.getResult());
        c.setConclusion(buildConclusion(c, dto.getConclusion()));
        c.setStatus(CrossmatchOrderStatusEnum.MATCHED.getCode());
        c.setOperator(operatorUser.getRealName());
        c.setMatchTime(LocalDateTime.now().withNano(0));
        bizBloodCrossmatchMapper.updateById(c);
    }

    @Transactional(rollbackFor = Exception.class)
    public void crossmatchVerify(BloodDTO.CrossmatchVerify dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizBloodCrossmatch c = requireCm(dto.getMatchId());
        if (c.getStatus() != CrossmatchOrderStatusEnum.MATCHED.getCode()) {
            throw new BusinessException("仅「已配血」可复核（当前：" + dictCacheService.getDicDataLabel(DictType.CROSSMATCH_STATUS, c.getStatus()) + "）");
        }
        String who = operatorUser.getRealName();
        if (who != null && who.equals(c.getOperator())) {
            throw new BusinessException("复核人不得是配血人本人（" + who + "）——配血结果必须双人确认");
        }
        c.setStatus(CrossmatchOrderStatusEnum.VERIFIED.getCode());
        c.setVerifier(who);
        c.setVerifyTime(LocalDateTime.now().withNano(0));
        bizBloodCrossmatchMapper.updateById(c);

        // 相合 → 预留血袋（发血的前置条件）
        if (c.getResult() != null && c.getResult() == CrossmatchResultEnum.MATCHED.getCode()) {
            BizBloodInventory bag = bizBloodInventoryMapper.selectOne(new LambdaQueryWrapper<BizBloodInventory>()
                    .eq(BizBloodInventory::getBagNo, c.getBagNo()).last("LIMIT 1"));
            if (bag != null && bag.getStatus() == BloodInventoryStatusEnum.IN_STOCK.getCode()) {
                bag.setStatus(BloodInventoryStatusEnum.RESERVED.getCode());
                bag.setApplyNo(c.getApplyNo());
                bizBloodInventoryMapper.updateById(bag);
                writeLog(bag.getBagNo(), BloodStockLogBizTypeEnum.RESERVE.getCode(), BloodInventoryStatusEnum.IN_STOCK.getCode(), BloodInventoryStatusEnum.RESERVED.getCode(), c.getApplyNo(),
                        "配血复核相合自动预留（" + c.getMatchNo() + "）");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void crossmatchVoid(Long matchId) {
        BizBloodCrossmatch c = requireCm(matchId);
        if (c.getStatus() == CrossmatchOrderStatusEnum.VERIFIED.getCode()) {
            throw new BusinessException("已复核的配血单不可作废（它是发血依据），先报废对应血袋再走台账冲正");
        }
        if (c.getStatus() == CrossmatchOrderStatusEnum.VOIDED.getCode()) {
            throw new BusinessException("该配血单已作废");
        }
        c.setStatus(CrossmatchOrderStatusEnum.VOIDED.getCode());
        bizBloodCrossmatchMapper.updateById(c);
    }

    // 流水

    public PageResult<BloodVO.StockLogVO> logPage(BloodDTO.LogQuery q) {
        LambdaQueryWrapper<BizBloodStockLog> w = new LambdaQueryWrapper<>();
        w.eq(TextUtil.hasText(q.getBagNo()), BizBloodStockLog::getBagNo, TextUtil.trim(q.getBagNo()))
                .eq(q.getBizType() != null, BizBloodStockLog::getBizType, q.getBizType())
                .eq(TextUtil.hasText(q.getApplyNo()), BizBloodStockLog::getApplyNo, TextUtil.trim(q.getApplyNo()))
                .orderByDesc(BizBloodStockLog::getOperateTime)
                .orderByDesc(BizBloodStockLog::getId);
        Page<BizBloodStockLog> page = bizBloodStockLogMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), w);
        List<BloodVO.StockLogVO> vos = new ArrayList<>();
        for (BizBloodStockLog l : page.getRecords()) {
            BloodVO.StockLogVO vo = new BloodVO.StockLogVO();
            BeanUtils.copyProperties(l, vo);
            vo.setBizTypeText(logTypeText(l.getBizType()));
            vos.add(vo);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    // 内部

    private void writeLog(String bagNo, int bizType, Integer from, Integer to, String applyNo, String reason) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizBloodStockLog l = new BizBloodStockLog();
        l.setBagNo(bagNo);
        l.setBizType(bizType);
        l.setFromStatus(from);
        l.setToStatus(to);
        l.setApplyNo(applyNo);
        l.setReason(TextUtil.cut(reason, 480));
        l.setOperator(operatorUser.getRealName());
        l.setOperateTime(LocalDateTime.now().withNano(0));
        bizBloodStockLogMapper.insert(l);
    }

    /**
     * 配血结论：服务端生成 ABO/Rh 核对说明，操作人补充内容拼在后面
     */
    private String buildConclusion(BizBloodCrossmatch c, String extra) {
        String patientT = dictCacheService.getDicDataLabel(DictType.BLOOD_TYPE, c.getPatientBloodType());
        String bagT = dictCacheService.getDicDataLabel(DictType.BLOOD_TYPE, c.getBagBloodType());
        String patientRh = dictCacheService.getDicDataLabel(DictType.BLOOD_RH, c.getPatientRhType());
        String bagRh = dictCacheService.getDicDataLabel(DictType.BLOOD_RH, c.getBagRhType());
        boolean sameAbo = c.getPatientBloodType() != null && c.getPatientBloodType().equals(c.getBagBloodType());
        boolean sameRh = c.getPatientRhType() == null || c.getBagRhType() == null || c.getPatientRhType().equals(c.getBagRhType());
        StringBuilder sb = new StringBuilder();
        sb.append("血型核对：患者 ").append(patientT).append("/").append(patientRh)
                .append("，血袋 ").append(bagT).append("/").append(bagRh)
                .append(sameAbo && sameRh ? "（同型）" : "（非同型，须核对输血指征）");
        if (TextUtil.hasText(extra)) {
            sb.append("；").append(extra);
        }
        String s = sb.toString();
        return s.length() > 480 ? s.substring(0, 480) : s;
    }

    private String logTypeText(Integer t) {
        return BloodStockLogBizTypeEnum.labelOrUnknown(t);
    }

    private BizBloodInventory requireBag(Long id) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (id == null) {
            throw new BusinessException("血袋ID不能为空");
        }
        BizBloodInventory b = bizBloodInventoryMapper.selectById(id);
        if (b == null) {
            throw new BusinessException("血袋不存在：" + id);
        }
        return b;
    }

    private BizBloodCrossmatch requireCm(Long id) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (id == null) {
            throw new BusinessException("配血单ID不能为空");
        }
        BizBloodCrossmatch c = bizBloodCrossmatchMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("配血单不存在：" + id);
        }
        return c;
    }

    private String nextMatchNo() {
        String day = LocalDate.now().format(DateFormats.COMPACT_DATE);
        long base = bizBloodCrossmatchMapper.selectCount(new LambdaQueryWrapper<BizBloodCrossmatch>()
                .ge(BizBloodCrossmatch::getCreateTime, TimeUtil.dayStart(LocalDate.now()))) + 1;
        for (int i = 0; i < 20; i++) {
            String no = "PX" + day + String.format("%03d", base + i);
            if (bizBloodCrossmatchMapper.selectCount(new LambdaQueryWrapper<BizBloodCrossmatch>()
                    .eq(BizBloodCrossmatch::getMatchNo, no)) == 0) {
                return no;
            }
        }
        return "PX" + day + System.currentTimeMillis() % 100000;
    }

}
