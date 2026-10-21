package com.his.emr.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.PrescriptionStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.emr.entity.BizDrugDispensing;
import com.his.emr.entity.BizPrescription;
import com.his.common.enums.DispensingStatusEnum;
import com.his.common.enums.NarcoticViolationLevelEnum;
import com.his.emr.mapper.BizDrugDispensingMapper;
import com.his.emr.mapper.BizPrescriptionMapper;
import com.his.emr.service.DrugDispensingService;
import com.his.emr.service.NarcoticControlService;
import com.his.emr.service.TcmDecoctService;
import com.his.emr.vo.BizDrugDispensingVO;
import com.his.emr.vo.DrugDispensingCountVO;
import com.his.emr.vo.NarcoticViolationVO;
import com.his.common.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 药品发药服务实现
 */
@Service
@RequiredArgsConstructor
public class DrugDispensingServiceImpl extends ServiceImpl<BizDrugDispensingMapper, BizDrugDispensing> implements DrugDispensingService {

    private final BizPrescriptionMapper bizPrescriptionMapper;
    private final NarcoticControlService narcoticControlService;
    private final TcmDecoctService tcmDecoctService;

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
    public boolean dispense(Long dispensingId, Long checkerId, String overLimitReason) {
        CurrentUser pharmacist = requireCurrentPharmacist();
        Long pharmacistId = pharmacist.getEmployeeId();
        String pharmacistName = pharmacist.getRealName();
        BizDrugDispensing dispensing = requirePending(dispensingId);
        // ① 麻精限量闸门（按处方整体判一次）
        assertPrescriptionQuota(dispensing.getPrescriptionId(), overLimitReason);
        // ② 双人复核闸门（复核人姓名服务端反查，且不得与发药人同人）
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
    public boolean dispenseByPrescription(Long prescriptionId, Long checkerId, String overLimitReason) {
        if (prescriptionId == null) {
            throw new BusinessException("处方ID不能为空");
        }
        CurrentUser pharmacist = requireCurrentPharmacist();
        Long pharmacistId = pharmacist.getEmployeeId();
        String pharmacistName = pharmacist.getRealName();
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
                .filter(v -> NarcoticViolationLevelEnum.BLOCK.getCode().equals(v.getLevel()))
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
        String operatorName = UserUtils.getCurrentUser().getRealName();
        // TODO(spi 扩展)：药房侧库存回补（restoreStock）待药品域库存端口开放后接入，当前模块不再直连 PharmacyService
        // 登记需求见 spi-extension-requests.txt（发药扣减 FEFO / 退药回补库存）

        dispensing.setDispensingStatus(DispensingStatusEnum.RETURNED.getCode());
        dispensing.setRemark(reason);
        dispensing.setUpdateBy(operatorName);
        boolean updated = this.updateById(dispensing);
        if (!updated) {
            throw new BusinessException("退药更新失败");
        }

        // 处方联动：任一明细退药 → 处方置 6 + refund_*
        BizPrescription rx = bizPrescriptionMapper.selectById(dispensing.getPrescriptionId());
        if (rx != null) {
            rx.setPrescriptionStatus(PrescriptionStatusEnum.RETURNED.getCode());
            rx.setRefundTime(TimeUtil.nowSeconds());
            rx.setRefundBy(operatorName);
            rx.setRefundReason(reason);
            bizPrescriptionMapper.updateById(rx);
        }
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
        BizPrescription rx = bizPrescriptionMapper.selectById(dispensing.getPrescriptionId());
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

        // TODO(spi 扩展)：FEFO 扣减药房库存（deductStockFefo，库存不足整单失败）待药品域库存端口开放后接入；
        // 当前模块不再直连 PharmacyService，扣减前后库存列暂不回写。登记需求见 spi-extension-requests.txt。

        dispensing.setDispensingStatus(DispensingStatusEnum.DISPENSED.getCode());
        dispensing.setPharmacistId(pharmacistId);
        dispensing.setPharmacistName(pharmacistName);
        dispensing.setDispensingTime(TimeUtil.nowSeconds());
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
        BizPrescription rx = bizPrescriptionMapper.selectById(prescriptionId);
        if (rx == null) {
            return;
        }
        rx.setPrescriptionStatus(PrescriptionStatusEnum.DISPENSED.getCode());
        rx.setDispenseTime(TimeUtil.nowSeconds());
        rx.setDispenseBy(pharmacistName);
        bizPrescriptionMapper.updateById(rx);
        tcmDecoctService.createOnDispensed(prescriptionId);
    }

    /**
     * 发药人 = 当前登录员工。身份只能来自登录态，前端不可伪造；取不到即报错，不降级、不兜底。
     */
    private CurrentUser requireCurrentPharmacist() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getEmployeeId() == null || !TextUtil.hasText(user.getRealName())) {
            throw new BusinessException("未获取到当前登录药师信息，无法发药");
        }
        return user;
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
