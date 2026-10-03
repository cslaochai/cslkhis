package com.his.medicaltech.service.impl;

import com.his.medicaltech.service.BloodService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.dto.BloodDTO;
import com.his.medicaltech.entity.BizBloodCrossmatch;
import com.his.medicaltech.entity.BizBloodInventory;
import com.his.medicaltech.entity.BizBloodStockLog;
import com.his.medicaltech.enums.BloodInventoryStatusEnum;
import com.his.medicaltech.enums.BloodSourceTypeEnum;
import com.his.medicaltech.enums.BloodStockLogBizTypeEnum;
import com.his.medicaltech.enums.BloodTypeEnum;
import com.his.medicaltech.enums.CrossmatchMethodEnum;
import com.his.medicaltech.enums.CrossmatchOrderStatusEnum;
import com.his.medicaltech.enums.CrossmatchResultEnum;
import com.his.medicaltech.enums.RhTypeEnum;
import com.his.medicaltech.mapper.BizBloodCrossmatchMapper;
import com.his.medicaltech.mapper.BizBloodInventoryMapper;
import com.his.medicaltech.mapper.BizBloodStockLogMapper;
import com.his.medicaltech.vo.BloodVO;
import com.his.medicaltech.support.SubDictText;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 血库储血台账服务
 *
 * <p>状态机（血袋）：1 在库 → 2 已预留 → 3 已发血；1/2 → 4 已报废 / 5 已退回。
 *
 * <p>硬规则：
 * <ol>
 *   <li>bag_no 全局唯一，重复入库直接拒绝；</li>
 *   <li>每一次状态变化都写血库出入库流水（只增不改）；</li>
 *   <li>发血的前提是配血复核通过（相合）且血袋已预留——没有配血依据的发血等于
 *       把没做交叉配血的血液输给病人，服务端直接拒绝；</li>
 *   <li>配血复核人不得与配血人同一人；</li>
 *   <li>报废 / 退回必须写原因（台账审计查的就是这两个动作）。</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class BloodServiceImpl implements BloodService {

    private static final String DICT_BLOOD_TYPE = "his_blood_type";
    private static final String DICT_RH = "his_blood_rh";
    private static final String DICT_COMPONENT = "his_blood_component";
    private static final String DICT_INV_STATUS = "his_blood_inventory_status";
    private static final String DICT_SOURCE = "his_blood_source_type";
    private static final String DICT_CM_METHOD = "his_crossmatch_method";
    private static final String DICT_CM_RESULT = "his_crossmatch_result";
    private static final String DICT_CM_STATUS = "his_crossmatch_status";
    private static final String DICT_LOG_TYPE = "his_blood_log_type";

    private final BizBloodInventoryMapper invMapper;
    private final BizBloodCrossmatchMapper cmMapper;
    private final BizBloodStockLogMapper logMapper;
    private final SubDictText dictText;

    // 库存台账

    @Transactional(rollbackFor = Exception.class)
    public BizBloodInventory inbound(BloodDTO.Inbound dto) {
        String bagNo = dto.getBagNo().trim();
        if (invMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
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
        b.setInboundBy(currentName());
        b.setInboundTime(LocalDateTime.now().withNano(0));
        invMapper.insert(b);
        writeLog(bagNo, BloodStockLogBizTypeEnum.INBOUND.getCode(), null, BloodInventoryStatusEnum.IN_STOCK.getCode(), null, null);
        return b;
    }

    public PageResult<BloodVO.InventoryVO> inventoryPage(BloodDTO.InventoryQuery q) {
        LambdaQueryWrapper<BizBloodInventory> w = new LambdaQueryWrapper<>();
        w.eq(StringUtils.hasText(q.getBagNo()), BizBloodInventory::getBagNo, tr(q.getBagNo()))
                .eq(q.getBloodType() != null, BizBloodInventory::getBloodType, q.getBloodType())
                .eq(q.getRhType() != null, BizBloodInventory::getRhType, q.getRhType())
                .eq(q.getComponentType() != null, BizBloodInventory::getComponentType, q.getComponentType())
                .eq(q.getStatus() != null, BizBloodInventory::getStatus, q.getStatus())
                .like(StringUtils.hasText(q.getStorageLoc()), BizBloodInventory::getStorageLoc, tr(q.getStorageLoc()));
        if (q.getExpireWithinDays() != null) {
            w.isNotNull(BizBloodInventory::getExpireDate)
                    .le(BizBloodInventory::getExpireDate, LocalDate.now().plusDays(q.getExpireWithinDays()));
        }
        w.orderByAsc(BizBloodInventory::getExpireDate)
                .orderByDesc(BizBloodInventory::getId);
        Page<BizBloodInventory> page = invMapper.selectPage(
                new Page<>(q.getPageNum() == null ? 1 : q.getPageNum(), q.getPageSize() == null ? 20 : q.getPageSize()), w);
        List<BloodVO.InventoryVO> vos = new ArrayList<>();
        for (BizBloodInventory b : page.getRecords()) {
            vos.add(toInvVo(b));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    private BloodVO.CrossmatchVO toCmVo(BizBloodCrossmatch c) {
        BloodVO.CrossmatchVO vo = new BloodVO.CrossmatchVO();
        BeanUtils.copyProperties(c, vo);
        vo.setPatientBloodTypeText(dictText.text(DICT_BLOOD_TYPE, c.getPatientBloodType()));
        vo.setPatientRhTypeText(dictText.text(DICT_RH, c.getPatientRhType()));
        vo.setBagBloodTypeText(dictText.text(DICT_BLOOD_TYPE, c.getBagBloodType()));
        vo.setComponentTypeText(dictText.text(DICT_COMPONENT, c.getComponentType()));
        vo.setMethodText(dictText.text(DICT_CM_METHOD, c.getMethod()));
        vo.setResultText(dictText.text(DICT_CM_RESULT, c.getResult()));
        vo.setStatusText(dictText.text(DICT_CM_STATUS, c.getStatus()));
        return vo;
    }

    public BloodVO.InventoryVO toInvVo(BizBloodInventory b) {
        BloodVO.InventoryVO vo = new BloodVO.InventoryVO();
        BeanUtils.copyProperties(b, vo);
        vo.setBloodTypeText(dictText.text(DICT_BLOOD_TYPE, b.getBloodType()));
        vo.setRhTypeText(dictText.text(DICT_RH, b.getRhType()));
        vo.setComponentTypeText(dictText.text(DICT_COMPONENT, b.getComponentType()));
        vo.setStatusText(dictText.text(DICT_INV_STATUS, b.getStatus()));
        vo.setSourceTypeText(dictText.text(DICT_SOURCE, b.getSourceType()));
        vo.setAboVerifyText(b.getAboVerify() != null && b.getAboVerify() == YesOrNoEnum.YES.getCode() ? "已复核" : "未复核");
        if (b.getExpireDate() != null) {
            vo.setExpireDays((int) ChronoUnit.DAYS.between(LocalDate.now(), b.getExpireDate()));
        }
        return vo;
    }

    public BloodVO.StatsVO inventoryStats() {
        BloodVO.StatsVO vo = new BloodVO.StatsVO();
        vo.setInStock(invMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .eq(BizBloodInventory::getStatus, BloodInventoryStatusEnum.IN_STOCK.getCode())));
        vo.setReserved(invMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .eq(BizBloodInventory::getStatus, BloodInventoryStatusEnum.RESERVED.getCode())));
        vo.setIssued(invMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .eq(BizBloodInventory::getStatus, BloodInventoryStatusEnum.ISSUED.getCode())));
        vo.setExpireSoon(invMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .in(BizBloodInventory::getStatus, BloodInventoryStatusEnum.IN_STOCK.getCode(), BloodInventoryStatusEnum.RESERVED.getCode())
                .isNotNull(BizBloodInventory::getExpireDate)
                .le(BizBloodInventory::getExpireDate, LocalDate.now().plusDays(7))));
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        vo.setTodayIn(invMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .ge(BizBloodInventory::getInboundTime, dayStart)));
        vo.setTodayOut(invMapper.selectCount(new LambdaQueryWrapper<BizBloodInventory>()
                .ge(BizBloodInventory::getOutboundTime, dayStart)));
        vo.setPendingMatch(cmMapper.selectCount(new LambdaQueryWrapper<BizBloodCrossmatch>()
                .eq(BizBloodCrossmatch::getStatus, CrossmatchOrderStatusEnum.PENDING.getCode())));

        List<BloodVO.TypeCount> byType = new ArrayList<>();
        for (BloodTypeEnum type : BloodTypeEnum.values()) {
            BloodVO.TypeCount c = new BloodVO.TypeCount();
            c.setBloodType(type.getCode());
            c.setBloodTypeText(dictText.text(DICT_BLOOD_TYPE, type.getCode()));
            List<BizBloodInventory> bags = invMapper.selectList(new LambdaQueryWrapper<BizBloodInventory>()
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
            throw new BusinessException("仅「在库」血袋可预留（当前：" + dictText.text(DICT_INV_STATUS, b.getStatus()) + "）");
        }
        b.setStatus(BloodInventoryStatusEnum.RESERVED.getCode());
        invMapper.updateById(b);
        writeLog(b.getBagNo(), BloodStockLogBizTypeEnum.RESERVE.getCode(), BloodInventoryStatusEnum.IN_STOCK.getCode(), BloodInventoryStatusEnum.RESERVED.getCode(), dto.getApplyNo(), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelReserve(BloodDTO.BagAction dto) {
        BizBloodInventory b = requireBag(dto.getBagId());
        if (b.getStatus() != BloodInventoryStatusEnum.RESERVED.getCode()) {
            throw new BusinessException("仅「已预留」血袋可取消预留（当前：" + dictText.text(DICT_INV_STATUS, b.getStatus()) + "）");
        }
        b.setStatus(BloodInventoryStatusEnum.IN_STOCK.getCode());
        invMapper.updateById(b);
        writeLog(b.getBagNo(), BloodStockLogBizTypeEnum.CANCEL_RESERVE.getCode(), BloodInventoryStatusEnum.RESERVED.getCode(), BloodInventoryStatusEnum.IN_STOCK.getCode(), null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void issue(BloodDTO.BagAction dto) {
        if (!StringUtils.hasText(dto.getApplyNo())) {
            throw new BusinessException("发血必须关联用血申请单号");
        }
        BizBloodInventory b = requireBag(dto.getBagId());
        if (b.getStatus() != BloodInventoryStatusEnum.RESERVED.getCode()) {
            throw new BusinessException("发血前血袋必须「已预留」（预留由配血复核相合产生）——当前："
                    + dictText.text(DICT_INV_STATUS, b.getStatus()));
        }
        b.setStatus(BloodInventoryStatusEnum.ISSUED.getCode());
        b.setApplyNo(dto.getApplyNo().trim());
        b.setOutboundBy(currentName());
        b.setOutboundTime(LocalDateTime.now().withNano(0));
        invMapper.updateById(b);
        writeLog(b.getBagNo(), BloodStockLogBizTypeEnum.ISSUE.getCode(), BloodInventoryStatusEnum.RESERVED.getCode(), BloodInventoryStatusEnum.ISSUED.getCode(), b.getApplyNo(), null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void scrap(BloodDTO.BagAction dto) {
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("报废必须填写原因");
        }
        BizBloodInventory b = requireBag(dto.getBagId());
        if (b.getStatus() != BloodInventoryStatusEnum.IN_STOCK.getCode() && b.getStatus() != BloodInventoryStatusEnum.RESERVED.getCode()) {
            throw new BusinessException("仅「在库 / 已预留」血袋可报废（当前："
                    + dictText.text(DICT_INV_STATUS, b.getStatus()) + "）");
        }
        int from = b.getStatus();
        b.setStatus(BloodInventoryStatusEnum.SCRAPPED.getCode());
        b.setOutboundBy(currentName());
        b.setOutboundTime(LocalDateTime.now().withNano(0));
        invMapper.updateById(b);
        writeLog(b.getBagNo(), BloodStockLogBizTypeEnum.SCRAP.getCode(), from, BloodInventoryStatusEnum.SCRAPPED.getCode(), null, dto.getReason());
    }

    @Transactional(rollbackFor = Exception.class)
    public void returnBag(BloodDTO.BagAction dto) {
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("退回必须填写原因");
        }
        BizBloodInventory b = requireBag(dto.getBagId());
        if (b.getStatus() != BloodInventoryStatusEnum.IN_STOCK.getCode() && b.getStatus() != BloodInventoryStatusEnum.RESERVED.getCode()) {
            throw new BusinessException("仅「在库 / 已预留」血袋可退回（当前："
                    + dictText.text(DICT_INV_STATUS, b.getStatus()) + "）");
        }
        int from = b.getStatus();
        b.setStatus(BloodInventoryStatusEnum.RETURNED.getCode());
        b.setOutboundBy(currentName());
        b.setOutboundTime(LocalDateTime.now().withNano(0));
        invMapper.updateById(b);
        writeLog(b.getBagNo(), BloodStockLogBizTypeEnum.RETURN.getCode(), from, BloodInventoryStatusEnum.RETURNED.getCode(), null, dto.getReason());
    }

    // 交叉配血

    public PageResult<BloodVO.CrossmatchVO> crossmatchPage(BloodDTO.CrossmatchQuery q) {
        LambdaQueryWrapper<BizBloodCrossmatch> w = new LambdaQueryWrapper<>();
        w.eq(StringUtils.hasText(q.getMatchNo()), BizBloodCrossmatch::getMatchNo, tr(q.getMatchNo()))
                .eq(StringUtils.hasText(q.getBagNo()), BizBloodCrossmatch::getBagNo, tr(q.getBagNo()))
                .like(StringUtils.hasText(q.getPatientName()), BizBloodCrossmatch::getPatientName, tr(q.getPatientName()))
                .eq(q.getStatus() != null, BizBloodCrossmatch::getStatus, q.getStatus())
                .eq(q.getResult() != null, BizBloodCrossmatch::getResult, q.getResult())
                .orderByDesc(BizBloodCrossmatch::getId);
        Page<BizBloodCrossmatch> page = cmMapper.selectPage(
                new Page<>(q.getPageNum() == null ? 1 : q.getPageNum(), q.getPageSize() == null ? 20 : q.getPageSize()), w);
        List<BloodVO.CrossmatchVO> vos = new ArrayList<>();
        for (BizBloodCrossmatch c : page.getRecords()) {
            vos.add(toCmVo(c));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Transactional(rollbackFor = Exception.class)
    public BloodVO.CrossmatchVO crossmatchCreate(BloodDTO.CrossmatchCreate dto) {
        BizBloodInventory bag = invMapper.selectOne(new LambdaQueryWrapper<BizBloodInventory>()
                .eq(BizBloodInventory::getBagNo, dto.getBagNo().trim()).last("LIMIT 1"));
        if (bag == null) {
            throw new BusinessException("血袋不存在：" + dto.getBagNo());
        }
        if (bag.getStatus() != BloodInventoryStatusEnum.IN_STOCK.getCode() && bag.getStatus() != BloodInventoryStatusEnum.RESERVED.getCode()) {
            throw new BusinessException("血袋当前状态不可配血（" + dictText.text(DICT_INV_STATUS, bag.getStatus()) + "）");
        }
        long active = cmMapper.selectCount(new LambdaQueryWrapper<BizBloodCrossmatch>()
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
        cmMapper.insert(c);
        return toCmVo(c);
    }

    @Transactional(rollbackFor = Exception.class)
    public void crossmatchExecute(BloodDTO.CrossmatchExecute dto) {
        BizBloodCrossmatch c = requireCm(dto.getMatchId());
        if (c.getStatus() != CrossmatchOrderStatusEnum.PENDING.getCode()) {
            throw new BusinessException("仅「待配血」可执行配血（当前：" + dictText.text(DICT_CM_STATUS, c.getStatus()) + "）");
        }
        c.setMethod(dto.getMethod() == null ? CrossmatchMethodEnum.POLYBRENE.getCode() : dto.getMethod());
        c.setResult(dto.getResult());
        c.setConclusion(buildConclusion(c, dto.getConclusion()));
        c.setStatus(CrossmatchOrderStatusEnum.MATCHED.getCode());
        c.setOperator(currentName());
        c.setMatchTime(LocalDateTime.now().withNano(0));
        cmMapper.updateById(c);
    }

    @Transactional(rollbackFor = Exception.class)
    public void crossmatchVerify(BloodDTO.CrossmatchVerify dto) {
        BizBloodCrossmatch c = requireCm(dto.getMatchId());
        if (c.getStatus() != CrossmatchOrderStatusEnum.MATCHED.getCode()) {
            throw new BusinessException("仅「已配血」可复核（当前：" + dictText.text(DICT_CM_STATUS, c.getStatus()) + "）");
        }
        String who = currentName();
        if (who != null && who.equals(c.getOperator())) {
            throw new BusinessException("复核人不得是配血人本人（" + who + "）——配血结果必须双人确认");
        }
        c.setStatus(CrossmatchOrderStatusEnum.VERIFIED.getCode());
        c.setVerifier(who);
        c.setVerifyTime(LocalDateTime.now().withNano(0));
        cmMapper.updateById(c);

        // 相合 → 预留血袋（发血的前置条件）
        if (c.getResult() != null && c.getResult() == CrossmatchResultEnum.MATCHED.getCode()) {
            BizBloodInventory bag = invMapper.selectOne(new LambdaQueryWrapper<BizBloodInventory>()
                    .eq(BizBloodInventory::getBagNo, c.getBagNo()).last("LIMIT 1"));
            if (bag != null && bag.getStatus() == BloodInventoryStatusEnum.IN_STOCK.getCode()) {
                bag.setStatus(BloodInventoryStatusEnum.RESERVED.getCode());
                bag.setApplyNo(c.getApplyNo());
                invMapper.updateById(bag);
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
        cmMapper.updateById(c);
    }

    // 流水

    public PageResult<BloodVO.StockLogVO> logPage(BloodDTO.LogQuery q) {
        LambdaQueryWrapper<BizBloodStockLog> w = new LambdaQueryWrapper<>();
        w.eq(StringUtils.hasText(q.getBagNo()), BizBloodStockLog::getBagNo, tr(q.getBagNo()))
                .eq(q.getBizType() != null, BizBloodStockLog::getBizType, q.getBizType())
                .eq(StringUtils.hasText(q.getApplyNo()), BizBloodStockLog::getApplyNo, tr(q.getApplyNo()))
                .orderByDesc(BizBloodStockLog::getOperateTime)
                .orderByDesc(BizBloodStockLog::getId);
        Page<BizBloodStockLog> page = logMapper.selectPage(
                new Page<>(q.getPageNum() == null ? 1 : q.getPageNum(), q.getPageSize() == null ? 20 : q.getPageSize()), w);
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
        BizBloodStockLog l = new BizBloodStockLog();
        l.setBagNo(bagNo);
        l.setBizType(bizType);
        l.setFromStatus(from);
        l.setToStatus(to);
        l.setApplyNo(applyNo);
        l.setReason(clip(reason));
        l.setOperator(currentName());
        l.setOperateTime(LocalDateTime.now().withNano(0));
        logMapper.insert(l);
    }

    /** 配血结论：服务端生成 ABO/Rh 核对说明，操作人补充内容拼在后面 */
    private String buildConclusion(BizBloodCrossmatch c, String extra) {
        String patientT = dictText.text(DICT_BLOOD_TYPE, c.getPatientBloodType());
        String bagT = dictText.text(DICT_BLOOD_TYPE, c.getBagBloodType());
        String patientRh = dictText.text(DICT_RH, c.getPatientRhType());
        String bagRh = dictText.text(DICT_RH, c.getBagRhType());
        boolean sameAbo = c.getPatientBloodType() != null && c.getPatientBloodType().equals(c.getBagBloodType());
        boolean sameRh = c.getPatientRhType() == null || c.getBagRhType() == null || c.getPatientRhType().equals(c.getBagRhType());
        StringBuilder sb = new StringBuilder();
        sb.append("血型核对：患者 ").append(patientT).append("/").append(patientRh)
                .append("，血袋 ").append(bagT).append("/").append(bagRh)
                .append(sameAbo && sameRh ? "（同型）" : "（非同型，须核对输血指征）");
        if (StringUtils.hasText(extra)) {
            sb.append("；").append(extra);
        }
        String s = sb.toString();
        return s.length() > 480 ? s.substring(0, 480) : s;
    }

    private String logTypeText(Integer t) {
        if (t == null) {
            return "未知(n)";
        }
        String label = BloodStockLogBizTypeEnum.labelOf(t);
        return label == null ? "未知(" + t + ")" : label;
    }

    private BizBloodInventory requireBag(Long id) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (id == null) {
            throw new BusinessException("血袋ID不能为空");
        }
        BizBloodInventory b = invMapper.selectById(id);
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
        BizBloodCrossmatch c = cmMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("配血单不存在：" + id);
        }
        return c;
    }

    private String nextMatchNo() {
        String day = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        long base = cmMapper.selectCount(new LambdaQueryWrapper<BizBloodCrossmatch>()
                .ge(BizBloodCrossmatch::getCreateTime, LocalDate.now().atStartOfDay())) + 1;
        for (int i = 0; i < 20; i++) {
            String no = "PX" + day + String.format("%03d", base + i);
            if (cmMapper.selectCount(new LambdaQueryWrapper<BizBloodCrossmatch>()
                    .eq(BizBloodCrossmatch::getMatchNo, no)) == 0) {
                return no;
            }
        }
        return "PX" + day + System.currentTimeMillis() % 100000;
    }

    private String currentName() {
        String n = UserUtils.getCurrentEmployeeName();
        return n != null ? n : "未知操作人";
    }

    private String clip(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 480 ? s.substring(0, 480) : s;
    }

    /** null 安全 trim：查询条件的 value 参数是急切求值的，直接 x.trim() 会在 x 为 null 时 NPE */
    private String tr(String s) {
        return s == null ? null : s.trim();
    }
}
