package com.his.pharmacy.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.enums.DispensingStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.pharmacy.entity.BizDrugDispensing;
import com.his.pharmacy.mapper.BizDrugDispensingMapper;
import com.his.spi.dto.PendingDispensingCreateDTO;
import com.his.spi.service.DispensingFacadeService;
import com.his.spi.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 待发药记录端口实现（事实宿主：biz_drug_dispensing）。
 *
 * <p>缴费推进生成待发药、退费取消待发药、退费前未退药闸门；
 * 已发药记录一律不动，回库是药师实物验收的退药动作。
 */
@Service
@RequiredArgsConstructor
public class DispensingFacadeServiceImpl implements DispensingFacadeService {

    /**
     * 备注列宽（写库前截断，避免 Data too long）
     */
    private static final int W_REMARK = 500;

    private final BizDrugDispensingMapper dispensingMapper;
    private final RedisSequenceService redisSequenceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPendingDispensing(PendingDispensingCreateDTO dto) {
        // 幂等锚点：同一条处方明细已有记录（任意状态）即跳过，不叠加
        Long existing = dispensingMapper.selectCount(new LambdaQueryWrapper<BizDrugDispensing>()
                .eq(BizDrugDispensing::getPrescriptionDetailId, dto.getPrescriptionDetailId()));
        if (existing != null && existing > 0) {
            return;
        }
        BizDrugDispensing row = new BizDrugDispensing();
        row.setDispensingNo(redisSequenceService.generateDispensingNo());
        row.setPrescriptionId(dto.getPrescriptionId());
        row.setPrescriptionNo(dto.getPrescriptionNo());
        row.setPrescriptionDetailId(dto.getPrescriptionDetailId());
        row.setPatientId(dto.getPatientId());
        row.setPatientNo(dto.getPatientNo());
        row.setPatientName(dto.getPatientName());
        row.setDrugId(dto.getDrugId());
        row.setDrugCode(dto.getDrugCode());
        row.setDrugName(dto.getDrugName());
        row.setSpecification(dto.getSpecification());
        row.setUnit(dto.getUnit());
        row.setQuantity(dto.getQuantity());
        row.setPrice(dto.getPrice());
        row.setAmount(dto.getAmount());
        row.setDispensingStatus(DispensingStatusEnum.PENDING.getCode());
        dispensingMapper.insert(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPendingDispensingByDetailId(Long prescriptionDetailId, String reason) {
        if (prescriptionDetailId == null) {
            return;
        }
        // 仅取消待发药(PENDING)，已发药/已退药记录不动
        dispensingMapper.update(null, new LambdaUpdateWrapper<BizDrugDispensing>()
                .eq(BizDrugDispensing::getPrescriptionDetailId, prescriptionDetailId)
                .eq(BizDrugDispensing::getDispensingStatus, DispensingStatusEnum.PENDING.getCode())
                .set(BizDrugDispensing::getDispensingStatus, DispensingStatusEnum.CANCELLED.getCode())
                .set(BizDrugDispensing::getRemark, TextUtil.cut("退费取消：" + reason, W_REMARK)));
    }

    @Override
    public void assertNoDrugPendingReturn(List<Long> prescriptionDetailIds, String scene) {
        if (prescriptionDetailIds == null || prescriptionDetailIds.isEmpty()) {
            return;
        }
        Long locked = dispensingMapper.selectCount(new LambdaQueryWrapper<BizDrugDispensing>()
                .in(BizDrugDispensing::getPrescriptionDetailId, prescriptionDetailIds)
                .eq(BizDrugDispensing::getDispensingStatus, DispensingStatusEnum.DISPENSED.getCode()));
        if (locked != null && locked > 0) {
            throw new BusinessException((TextUtil.hasText(scene) ? scene : "退费")
                    + "前请先办理退药：存在已发药但未退药的药品记录");
        }
    }
}
