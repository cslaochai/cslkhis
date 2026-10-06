package com.his.emr.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.PrescriptionStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.support.TcmGramUnits;
import com.his.emr.entity.BizDrugDispensing;
import com.his.emr.entity.BizPrescription;
import com.his.emr.enums.DispensingStatusEnum;
import com.his.emr.mapper.BizDrugDispensingMapper;
import com.his.emr.mapper.BizPrescriptionMapper;
import com.his.emr.service.DrugDispensingService;
import com.his.emr.service.NarcoticControlService;
import com.his.emr.service.TcmDecoctService;
import com.his.emr.vo.BizDrugDispensingVO;
import com.his.emr.vo.DrugDispensingCountVO;
import com.his.emr.vo.NarcoticViolationVO;
import com.his.pharmacy.dto.StockDeductResultDTO;
import com.his.pharmacy.service.PharmacyService;
import com.his.system.entity.SysDrug;
import com.his.system.mapper.SysDrugMapper;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 药品发药服务实现
 * 闭环口径：
 * 1) 审方闸门：处方 prescription_status 必须已审核（3/4/6 允许继续发剩余明细；1/2/5 拒绝）；
 * 2) **麻精限量闸门**：整张处方一次判定，任一管制明细超法定处方天数 → 整单不发（NarcoticControlService）；
 * 3) **麻精双人复核闸门**：麻醉药品、第一类精神药品必须指定复核药师，且不得与发药人同人；
 * 4) 扣库存：PharmacyService.deductStockFefo 先过期先出、跨批次、逐批落药品库存流水，
 * 前后合计写回药品发药记录.stock_before/stock_after；
 * <b>中药饮片行先换算再扣</b>（quantity 是克、批次是 kg/袋，见 {@code TcmGramUnits}，sql/139）；
 * 5) **麻精写专册**：发药成功后按 FEFO 实际扣减批次登记，批号从流水回查、不取前端传值；
 * 6) 处方联动：该处方全部待发明细发完 → 处方置 4 + dispense_time/dispense_by；任一明细退药 → 处方置 6 + refund_*；
 * 饮片方且标了代煎的，在置 4 的同一事务里生成代煎台账（中药代煎单，幂等）；
 * 7) 一切状态迁移校验失败抛 BusinessException，绝不静默成功。
 *
 * <p>闸门顺序是「先校验、后扣库存」：反过来的话每次校验失败都要回滚一批流水，
 * 流水表里全是幻影记录，事后没人分得清哪些是真实动过的。
 */
@Service
@RequiredArgsConstructor
public class DrugDispensingServiceImpl extends ServiceImpl<BizDrugDispensingMapper, BizDrugDispensing> implements DrugDispensingService {

    private final BizPrescriptionMapper prescriptionMapper;
    private final PharmacyService pharmacyService;
    private final NarcoticControlService narcoticControlService;
    private final SysDrugMapper drugMapper;
    private final TcmDecoctService tcmDecoctService;

    /**
     * 本行该扣/退多少<b>库存档案单位</b>（sql/139 第三条口径）。
     *
     * <p>饮片方发药记录上的 quantity 是<b>克</b>（每剂克数 × 剂数，由收费侧从处方明细原样抄来），
     * 而批次库存按档案单位记（散装 kg、包装袋）。差着 1000 倍，直接拿 quantity 去扣 =
     * 发 105g 黄芪抹掉 105kg 账。换算一律走 {@link TcmGramUnits}，且扣减<b>向上</b>取整。
     *
     * <p>西药/中成药（换算率为空）原样返回，行为与改造前完全一致。
     */
    private BigDecimal stockUnitsOf(BizDrugDispensing dispensing) {
        BigDecimal quantity = dispensing.getQuantity();
        if (quantity == null || dispensing.getDrugId() == null) {
            return quantity;
        }
        SysDrug drug = drugMapper.selectById(dispensing.getDrugId());
        BigDecimal gramPerUnit = drug == null ? null : drug.getGramPerUnit();
        if (!TcmGramUnits.gramDosed(gramPerUnit)) {
            return quantity;
        }
        return TcmGramUnits.toStockUnits(quantity, gramPerUnit);
    }

    @Override
    public PageResult<BizDrugDispensingVO> selectDispensingPage(Long patientId, String patientName, String prescriptionNo,
                                                                Integer dispensingStatus, int pageNum, int pageSize) {
        Page<BizDrugDispensing> page = baseMapper.selectDispensingPage(
                new Page<>(pageNum, pageSize), patientId, patientName, prescriptionNo, dispensingStatus);
        List<BizDrugDispensingVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        enrichSpecialFlag(voList);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dispense(Long dispensingId, Long pharmacistId, String pharmacistName,
                            Long checkerId, String overLimitReason) {
        BizDrugDispensing dispensing = requirePending(dispensingId);
        // ① 麻精限量闸门（按处方整体判一次）
        assertPrescriptionQuota(dispensing.getPrescriptionId(), overLimitReason);
        // ② 双人复核闸门（复核人姓名服务端反查）
        String checkerName = narcoticControlService.resolveAndAssertChecker(
                dispensing.getDrugId(), pharmacistId, checkerId);
        doDispenseOne(dispensing, pharmacistId, pharmacistName);
        // ③ 写专册（必须在扣减之后 —— 批号要从 FEFO 流水里回查）
        narcoticControlService.registerOnDispense(dispensing, checkerId, checkerName, overLimitReason);
        syncPrescriptionDispensed(dispensing.getPrescriptionId(), pharmacistId, pharmacistName);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dispenseByPrescription(Long prescriptionId, Long pharmacistId, String pharmacistName,
                                          Long checkerId, String overLimitReason) {
        // C 类保留：入参是 Long（Controller 解 DTO 后拆开直传），Bean Validation 不经过这一层
        if (prescriptionId == null) {
            throw new BusinessException("处方ID不能为空");
        }
        // ① 麻精限量闸门：整单一次判定，任一管制明细超限整单不发
        assertPrescriptionQuota(prescriptionId, overLimitReason);
        List<BizDrugDispensing> pendingList = this.lambdaQuery()
                .eq(BizDrugDispensing::getPrescriptionId, prescriptionId)
                .eq(BizDrugDispensing::getDispensingStatus, DispensingStatusEnum.PENDING.getCode())
                .orderByAsc(BizDrugDispensing::getId)
                .list();
        if (pendingList.isEmpty()) {
            throw new BusinessException("该处方没有待发药明细");
        }
        for (BizDrugDispensing dispensing : pendingList) {
            // ② 双人复核：只对麻醉药品/第一类精神药品生效，同一单里普通药品不受影响
            String checkerName = narcoticControlService.resolveAndAssertChecker(
                    dispensing.getDrugId(), pharmacistId, checkerId);
            doDispenseOne(dispensing, pharmacistId, pharmacistName);
            // ③ 这行的复核人只在该行是麻精时才非空，普通药品行 checkerName 为 null 属正常
            narcoticControlService.registerOnDispense(dispensing, checkerId, checkerName, overLimitReason);
        }
        // 整单发完，必然全部发出
        markPrescriptionDispensed(prescriptionId, pharmacistId, pharmacistName);
        return true;
    }

    /**
     * 麻精限量闸门：有 BLOCK 级违规即拒，并把每一条结论原样抛给调用方。
     * <p>
     * 只报"处方校验失败"会让医生反复试；把「哪个药、哪档限量、超了几日」直接说出来，
     * 他才知道改哪里。WARN 级（二类精神超 7 日且已注明理由）放行，理由由专册留存。
     */
    private void assertPrescriptionQuota(Long prescriptionId, String overLimitReason) {
        if (prescriptionId == null) {
            return;
        }
        List<NarcoticViolationVO> violations = narcoticControlService.checkPrescription(prescriptionId, overLimitReason);
        if (violations.isEmpty()) {
            return;
        }
        List<NarcoticViolationVO> blocks = violations.stream()
                .filter(v -> "BLOCK".equals(v.getLevel()))
                .collect(Collectors.toList());
        if (blocks.isEmpty()) {
            return;
        }
        throw new BusinessException("麻精处方限量校验未通过：" + blocks.stream()
                .map(NarcoticViolationVO::getMessage).collect(Collectors.joining("；")));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnDrug(Long dispensingId, String reason) {
        BizDrugDispensing dispensing = this.getById(dispensingId);
        if (dispensing == null) {
            throw new BusinessException("发药记录不存在");
        }
        if (dispensing.getDispensingStatus() == null || dispensing.getDispensingStatus() != DispensingStatusEnum.DISPENSED.getCode()) {
            throw new BusinessException("当前状态不允许退药");
        }
        String operatorName = UserUtils.getCurrentEmployeeName();

        // 退药回库（落流水 type=3）：回库量必须与当初的扣库量同一个口径（档案单位），
        // 否则饮片退一次药，账上就多出一堆不存在的克
        pharmacyService.restoreStock(dispensing.getDrugId(), stockUnitsOf(dispensing),
                "dispenseReturn", dispensing.getId(), dispensing.getDispensingNo(), operatorName);

        dispensing.setDispensingStatus(DispensingStatusEnum.RETURNED.getCode());
        dispensing.setRemark(reason);
        dispensing.setUpdateBy(operatorName);
        boolean updated = this.updateById(dispensing);
        if (!updated) {
            throw new BusinessException("退药更新失败");
        }

        // 处方联动：任一明细退药 → 处方置 6 + refund_*
        BizPrescription rx = prescriptionMapper.selectById(dispensing.getPrescriptionId());
        if (rx != null) {
            rx.setPrescriptionStatus(PrescriptionStatusEnum.RETURNED.getCode());
            rx.setRefundTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
            rx.setRefundBy(operatorName);
            rx.setRefundReason(reason);
            prescriptionMapper.updateById(rx);
        }
        // sql/139：药退回了架上，还没开煎的代煎单必须跟着停掉 ——
        // 否则煎药室照着台账煎出一袋没人取的汤液（建单时机选在发药后，退药就是它的逆动作）
        tcmDecoctService.cancelOnReturn(dispensing.getPrescriptionId());
        return true;
    }

    @Override
    public BizDrugDispensingVO getDispensingDetail(Long dispensingId) {
        BizDrugDispensingVO vo = toVO(this.getById(dispensingId));
        if (vo != null) {
            enrichSpecialFlag(java.util.Collections.singletonList(vo));
        }
        return vo;
    }

    @Override
    public DrugDispensingCountVO getStatusCount() {
        DrugDispensingCountVO counts = new DrugDispensingCountVO();
        counts.setPending(this.lambdaQuery().eq(BizDrugDispensing::getDispensingStatus, DispensingStatusEnum.PENDING.getCode()).count());
        counts.setDispensed(this.lambdaQuery().eq(BizDrugDispensing::getDispensingStatus, DispensingStatusEnum.DISPENSED.getCode()).count());
        counts.setReturned(this.lambdaQuery().eq(BizDrugDispensing::getDispensingStatus, DispensingStatusEnum.RETURNED.getCode()).count());
        return counts;
    }

    /**
     * 单明细发药：审方闸门 + FEFO 扣库存 + 明细置 2
     */
    private void doDispenseOne(BizDrugDispensing dispensing, Long pharmacistId, String pharmacistName) {
        if (dispensing.getDispensingStatus() == null || dispensing.getDispensingStatus() != DispensingStatusEnum.PENDING.getCode()) {
            throw new BusinessException("当前状态不允许发药");
        }
        // 审方闸门：处方未审核（1草稿/2已提交）或已取消（5）禁止发药
        BizPrescription rx = prescriptionMapper.selectById(dispensing.getPrescriptionId());
        if (rx == null) {
            throw new BusinessException("关联处方不存在");
        }
        int rxStatus = rx.getPrescriptionStatus() == null ? PrescriptionStatusEnum.DRAFT.getCode() : rx.getPrescriptionStatus();
        if (rxStatus == PrescriptionStatusEnum.DRAFT.getCode() || rxStatus == PrescriptionStatusEnum.SUBMITTED.getCode()) {
            throw new BusinessException("处方未审核，禁止发药（处方号：" + rx.getPrescriptionNo() + "）");
        }
        if (rxStatus == PrescriptionStatusEnum.RETURNED_AUDIT.getCode()) {
            // L7 审方退回：药师已退回待医生改方重提，绝不能当"未审"漏进发药
            throw new BusinessException("处方已被审方退回，禁止发药（处方号：" + rx.getPrescriptionNo() + "）");
        }
        if (rxStatus == PrescriptionStatusEnum.CANCELLED.getCode()) {
            throw new BusinessException("处方已取消，禁止发药（处方号：" + rx.getPrescriptionNo() + "）");
        }

        // FEFO 扣库存（库存不足整单失败，流水在 his-pharmacy 侧落库）
        // 扣的是档案单位：饮片行 = 总克数 ÷ gram_per_unit 向上取整，其余与原数量一致
        BigDecimal stockQuantity = stockUnitsOf(dispensing);
        StockDeductResultDTO deduct = pharmacyService.deductStockFefo(dispensing.getDrugId(), stockQuantity,
                "dispensing", dispensing.getId(), dispensing.getDispensingNo(),
                pharmacistName != null ? pharmacistName : UserUtils.getCurrentEmployeeName());
        dispensing.setStockBefore(deduct.getQuantityBefore());
        dispensing.setStockAfter(deduct.getQuantityAfter());

        dispensing.setDispensingStatus(DispensingStatusEnum.DISPENSED.getCode());
        dispensing.setPharmacistId(pharmacistId);
        dispensing.setPharmacistName(pharmacistName);
        dispensing.setDispensingTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        boolean updated = this.updateById(dispensing);
        if (!updated) {
            throw new BusinessException("发药更新失败");
        }
    }

    /**
     * 该处方名下全部待发明细发完 → 处方置 4 + dispense_time/dispense_by
     */
    private void syncPrescriptionDispensed(Long prescriptionId, Long pharmacistId, String pharmacistName) {
        long pendingCount = this.lambdaQuery()
                .eq(BizDrugDispensing::getPrescriptionId, prescriptionId)
                .eq(BizDrugDispensing::getDispensingStatus, DispensingStatusEnum.PENDING.getCode())
                .count();
        if (pendingCount == 0) {
            markPrescriptionDispensed(prescriptionId, pharmacistId, pharmacistName);
        }
    }

    private void markPrescriptionDispensed(Long prescriptionId, Long pharmacistId, String pharmacistName) {
        BizPrescription rx = prescriptionMapper.selectById(prescriptionId);
        if (rx == null) {
            return;
        }
        rx.setPrescriptionStatus(PrescriptionStatusEnum.DISPENSED.getCode());
        rx.setDispenseTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        rx.setDispenseBy(pharmacistName);
        prescriptionMapper.updateById(rx);
        // sql/139：药已经全部调剂出去，才谈得上代煎 —— 在这里（而不是医生开方时）建单，
        // 才不会出现在途单据「单子在、药被退了」。建单幂等，非代煎方返回 null。
        tcmDecoctService.createOnDispensed(prescriptionId);
    }

    /**
     * 取待发药记录并校验存在性
     */
    private BizDrugDispensing requirePending(Long dispensingId) {
        BizDrugDispensing dispensing = this.getById(dispensingId);
        if (dispensing == null) {
            throw new BusinessException("发药记录不存在");
        }
        return dispensing;
    }

    private BizDrugDispensingVO toVO(BizDrugDispensing entity) {
        if (entity == null) {
            return null;
        }
        BizDrugDispensingVO vo = new BizDrugDispensingVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * 按 drugId 批量补 {@code specialFlag}（一次库往返，不是逐行查）。
     *
     * <p>查不到的药品**保持 null**，不补 0 —— 0 是"普通药品"，补 0 等于把"分类没维护"
     * 洗成"不受管制"，前端就不会提示双人复核、后端也不进专册。宁可让前端显示"未知"。
     */
    private void enrichSpecialFlag(List<BizDrugDispensingVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        List<Long> drugIds = voList.stream().map(BizDrugDispensingVO::getDrugId)
                .filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
        if (drugIds.isEmpty()) {
            return;
        }
        java.util.Map<Long, Integer> flagMap = narcoticControlService.specialFlagMap(drugIds);
        for (BizDrugDispensingVO vo : voList) {
            if (vo.getDrugId() != null) {
                vo.setSpecialFlag(flagMap.get(vo.getDrugId()));
            }
        }
    }
}
