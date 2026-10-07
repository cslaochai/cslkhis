package com.his.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.common.util.TimeUtil;
import com.his.operation.dto.*;
import com.his.operation.entity.BizAnesthesiaPacu;
import com.his.operation.entity.BizAnesthesiaRecord;
import com.his.operation.entity.BizOperationApply;
import com.his.operation.enums.AnesthesiaChargeStatusEnum;
import com.his.operation.enums.AnesthesiaRecordStatusEnum;
import com.his.operation.enums.OperationAnesthesiaMethodEnum;
import com.his.operation.enums.PacuStatusEnum;
import com.his.operation.mapper.BizAnesthesiaPacuMapper;
import com.his.operation.mapper.BizAnesthesiaRecordMapper;
import com.his.operation.mapper.BizOperationApplyMapper;
import com.his.operation.service.PacuService;
import com.his.operation.support.AnesthesiaCalcs;
import com.his.operation.support.OperationChargeBiller;
import com.his.operation.vo.OperationChargeSummaryVO;
import com.his.operation.vo.PacuRecordVO;
import com.his.system.service.DictCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * PACU 麻醉后监测治疗服务实现（G15 第三环）。
 *
 * <p>本类固化了这些<b>至少踩过一次或一定会被追问</b>的点：
 *
 * <ol>
 *   <li><b>Aldrete 总分服务端逐项相加</b>，不接收前端传来的总分：
 *       可以自己填总分的评分，出室标准就是摆设。</li>
 *   <li><b>出室标准 Aldrete ≥ 9</b>；不达标出室必须写明原因，且去向不能是"回病房" ——
 *       事故复盘里最常说的一句话就是"当时评分没到就走了"。</li>
 *   <li><b>一次麻醉一段 PACU</b>：UNIQUE(record_id) 之外再做计数，为了给出人话错误。</li>
 *   <li><b>只有已提交/已审核的麻醉记录才允许入 PACU</b>：没有麻醉记录却有一段复苏停留，
 *       等于凭空出现一节监护。</li>
 *   <li><b>出室即联动计费</b>（按停留整小时）；失败不影响业务推进，但会标记并写明原因。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PacuServiceImpl extends ServiceImpl<BizAnesthesiaPacuMapper, BizAnesthesiaPacu> implements PacuService {

    private final BizAnesthesiaPacuMapper bizAnesthesiaPacuMapper;
    private final BizAnesthesiaRecordMapper bizAnesthesiaRecordMapper;
    private final BizOperationApplyMapper bizOperationApplyMapper;
    private final OperationChargeBiller operationChargeBiller;
    private final DictCacheService dictCacheService;

    @Override
    public IPage<PacuRecordVO> listPage(PacuQueryPageDTO query) {
        if (query == null) {
            query = new PacuQueryPageDTO();
        }
        IPage<PacuRecordVO> page = bizAnesthesiaPacuMapper.selectPacuPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        page.getRecords().forEach(this::decorate);
        return page;
    }

    @Override
    public PacuRecordVO getDetailById(Long pacuId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (pacuId == null) {
            throw new BusinessException("PACU 记录ID不能为空");
        }
        PacuRecordVO vo = bizAnesthesiaPacuMapper.selectVOById(pacuId);
        if (vo == null) {
            throw new BusinessException("PACU 复苏记录不存在");
        }
        decorate(vo);
        return vo;
    }

    @Override
    public PacuRecordVO getByRecord(Long recordId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (recordId == null) {
            throw new BusinessException("麻醉记录单ID不能为空");
        }
        PacuRecordVO vo = bizAnesthesiaPacuMapper.selectVOByRecord(recordId);
        if (vo != null) {
            decorate(vo);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String enter(PacuEnterDTO dto) {
        BizAnesthesiaRecord record = bizAnesthesiaRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("麻醉记录单不存在");
        }
        if (Integer.valueOf(AnesthesiaRecordStatusEnum.DRAFT.getCode()).equals(record.getRecordStatus())) {
            throw new BusinessException("麻醉记录单 " + record.getRecordNo()
                    + " 还在「记录中」，先把麻醉记录提交，再登记入 PACU");
        }
        long exists = bizAnesthesiaPacuMapper.selectCount(
                new LambdaQueryWrapper<BizAnesthesiaPacu>().eq(BizAnesthesiaPacu::getRecordId, record.getId()));
        if (exists > 0) {
            throw new BusinessException("该麻醉记录已有 PACU 复苏单，不能重复入室");
        }
        BizOperationApply apply = bizOperationApplyMapper.selectById(record.getApplyId());
        if (apply == null) {
            throw new BusinessException("关联的手术申请单不存在，无法登记入 PACU");
        }

        BizAnesthesiaPacu entity = new BizAnesthesiaPacu();
        entity.setPacuNo(nextPacuNo());
        entity.setRecordId(record.getId());
        entity.setRecordNo(record.getRecordNo());
        entity.setApplyId(apply.getId());
        entity.setAdmissionId(apply.getAdmissionId());
        entity.setPatientId(apply.getPatientId());
        entity.setPatientName(apply.getPatientName());
        entity.setGender(apply.getGender());
        entity.setAge(apply.getAge());
        entity.setEnterTime(TimeUtil.nowSeconds());
        entity.setNurseId(dto.getNurseId());
        entity.setNurseName(employeeNameOf(dto.getNurseId()));
        entity.setAnesthetistId(dto.getAnesthetistId() != null ? dto.getAnesthetistId() : record.getAnesthetistId());
        entity.setAnesthetistName(employeeNameOf(entity.getAnesthetistId()));
        entity.setStatus(PacuStatusEnum.IN.getCode());
        entity.setChargeStatus(AnesthesiaChargeStatusEnum.PENDING.getCode());
        entity.setComplicationFlag(0);
        if (StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        bizAnesthesiaPacuMapper.insert(entity);
        log.info("入PACU pacuNo={} recordNo={} 患者={} 复苏护士={}",
                entity.getPacuNo(), record.getRecordNo(), apply.getPatientName(), entity.getNurseName());
        return entity.getPacuNo();
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void score(PacuScoreDTO dto) {
        BizAnesthesiaPacu entity = mustInRoom(dto == null ? null : dto.getPacuId());
        // B-条件必填：标记发生并发症时才要求经过与处理，跨字段条件，DTO 注解无法表达，保留
        if (Integer.valueOf(1).equals(dto.getComplicationFlag()) && !StringUtils.hasText(dto.getComplicationNote())) {
            throw new BusinessException("已标记发生并发症，必须填写经过与处理");
        }
        Integer total;
        try {
            total = AnesthesiaCalcs.aldreteTotal(dto.getScoreActivity(), dto.getScoreRespiration(),
                    dto.getScoreCirculation(), dto.getScoreConsciousness(), dto.getScoreSpo2());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        entity.setScoreActivity(dto.getScoreActivity());
        entity.setScoreRespiration(dto.getScoreRespiration());
        entity.setScoreCirculation(dto.getScoreCirculation());
        entity.setScoreConsciousness(dto.getScoreConsciousness());
        entity.setScoreSpo2(dto.getScoreSpo2());
        entity.setAldreteTotal(total);
        entity.setAwareness(dto.getAwareness());
        entity.setOxygenTherapy(dto.getOxygenTherapy());
        entity.setAnalgesia(dto.getAnalgesia());
        entity.setComplicationFlag(dto.getComplicationFlag() == null ? 0 : dto.getComplicationFlag());
        entity.setComplicationNote(dto.getComplicationNote());
        entity.setLeaveCriteriaMet(total != null && total >= AnesthesiaCalcs.ALDRETE_DISCHARGE_MIN ? 1 : 0);
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
        bizAnesthesiaPacuMapper.updateById(entity);
        log.info("PACU 评分 pacuNo={} Aldrete={}（活动{} 呼吸{} 循环{} 意识{} 氧合{}）",
                entity.getPacuNo(), total, entity.getScoreActivity(), entity.getScoreRespiration(),
                entity.getScoreCirculation(), entity.getScoreConsciousness(), entity.getScoreSpo2());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperationChargeSummaryVO leave(PacuLeaveDTO dto) {
        BizAnesthesiaPacu entity = mustInRoom(dto == null ? null : dto.getPacuId());
        if (entity.getAldreteTotal() == null) {
            throw new BusinessException("尚未完成 Aldrete 评分，不能出室");
        }
        boolean criteriaMet = entity.getAldreteTotal() >= AnesthesiaCalcs.ALDRETE_DISCHARGE_MIN;
        if (!criteriaMet) {
            // B-条件必填：Aldrete 未达出室标准时才要求写明出室原因，依赖运行时评分，DTO 注解无法表达，保留
            if (!StringUtils.hasText(dto.getNote())) {
                throw new BusinessException("Aldrete 评分 " + entity.getAldreteTotal() + " 未达出室标准 "
                        + AnesthesiaCalcs.ALDRETE_DISCHARGE_MIN + " 分，出室必须写明原因");
            }
            if (Integer.valueOf(1).equals(dto.getDisposition())) {
                throw new BusinessException("Aldrete 评分 " + entity.getAldreteTotal()
                        + " 未达出室标准，不能直接「回病房」—— 请转 ICU 或继续留观");
            }
        }
        if (Integer.valueOf(1).equals(entity.getComplicationFlag())
                && Integer.valueOf(1).equals(dto.getDisposition())) {
            throw new BusinessException("复苏期间发生并发症，出室去向不能是「回病房」");
        }

        entity.setLeaveTime(TimeUtil.toSeconds(dto.getLeaveTime() == null ? TimeUtil.nowSeconds() : dto.getLeaveTime()));
        entity.setDisposition(dto.getDisposition());
        entity.setLeaveCriteriaMet(criteriaMet ? 1 : 0);
        entity.setStatus(PacuStatusEnum.OUT.getCode());
        if (StringUtils.hasText(dto.getNote())) {
            entity.setRemark(StringUtils.hasText(entity.getRemark())
                    ? entity.getRemark() + "；出室说明：" + dto.getNote() : "出室说明：" + dto.getNote());
        } else if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }

        // ★ 出室即联动计费
        OperationChargeSummaryVO summary;
        try {
            summary = operationChargeBiller.billPacu(entity);
        } catch (Exception e) {
            log.error("PACU {} 计费异常：{}", entity.getPacuNo(), e.getMessage(), e);
            summary = new OperationChargeSummaryVO();
            summary.getMessages().add("计费过程异常：" + e.getMessage());
            summary.setFailedItems(1);
        }
        applyChargeResult(entity, summary);
        bizAnesthesiaPacuMapper.updateById(entity);
        log.info("出PACU pacuNo={} Aldrete={} 去向={} 计费=成功{}项/失败{}项 金额={}",
                entity.getPacuNo(), entity.getAldreteTotal(),
                dictCacheService.getDicDataLabel("biz_operation_pacuDispositionEnum", dto.getDisposition()),
                summary.getSuccessItems(), summary.getFailedItems(), summary.getAmount());
        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperationChargeSummaryVO charge(AnesthesiaActionDTO dto) {
        BizAnesthesiaPacu entity = mustGet(dto == null ? null : dto.getId());
        OperationChargeSummaryVO summary;
        try {
            summary = operationChargeBiller.billPacu(entity);
        } catch (Exception e) {
            log.error("PACU {} 重新计费异常：{}", entity.getPacuNo(), e.getMessage(), e);
            summary = new OperationChargeSummaryVO();
            summary.getMessages().add("计费过程异常：" + e.getMessage());
            summary.setFailedItems(1);
        }
        applyChargeResult(entity, summary);
        bizAnesthesiaPacuMapper.updateById(entity);
        return summary;
    }

    @Override
    public long countInRoom() {
        return bizAnesthesiaPacuMapper.selectCount(new LambdaQueryWrapper<BizAnesthesiaPacu>()
                .eq(BizAnesthesiaPacu::getStatus, PacuStatusEnum.IN.getCode()));
    }

    private void applyChargeResult(BizAnesthesiaPacu entity, OperationChargeSummaryVO summary) {
        if (summary == null || summary.getTotalItems() == 0) {
            entity.setChargeStatus(AnesthesiaChargeStatusEnum.PENDING.getCode());
            entity.setChargeFailReason("本次没有可计费项目");
            return;
        }
        entity.setChargedAmount(NumUtil.orZero(entity.getChargedAmount()).add(summary.getAmount()));
        if (summary.hasFailure()) {
            entity.setChargeStatus(AnesthesiaChargeStatusEnum.FAILED.getCode());
            entity.setChargeFailReason(AnesthesiaCalcs.clipReason(String.join("；", summary.getMessages())));
        } else {
            entity.setChargeStatus(AnesthesiaChargeStatusEnum.DONE.getCode());
            entity.setFeeNo(summary.getFeeNo());
            entity.setChargeFailReason(null);
        }
    }

    // 展示态

    private BizAnesthesiaPacu mustGet(Long pacuId) {
        // C-非 DTO 入参：私有 helper 校验方法参数，被多入口复用，Bean Validation 不覆盖，保留
        if (pacuId == null) {
            throw new BusinessException("PACU 记录ID不能为空");
        }
        BizAnesthesiaPacu entity = bizAnesthesiaPacuMapper.selectById(pacuId);
        if (entity == null) {
            throw new BusinessException("PACU 复苏记录不存在");
        }
        return entity;
    }

    private BizAnesthesiaPacu mustInRoom(Long pacuId) {
        BizAnesthesiaPacu entity = mustGet(pacuId);
        if (!Objects.equals(PacuStatusEnum.IN.getCode(), entity.getStatus())) {
            throw new BusinessException("PACU 复苏单 " + entity.getPacuNo() + " 当前状态为「"
                    + PacuStatusEnum.labelOrUnknown(entity.getStatus()) + "」，只有「在室观察」可以评分/出室");
        }
        return entity;
    }

    private String employeeNameOf(Long empId) {
        if (empId == null) {
            return null;
        }
        String name = bizOperationApplyMapper.selectEmployeeName(empId);
        return StringUtils.hasText(name) ? name : "未知员工(ID=" + empId + ")";
    }

    private String nextPacuNo() {
        String prefix = "FS" + LocalDate.now().format(DateFormats.COMPACT_DATE);
        long seq = bizAnesthesiaPacuMapper.countByNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private void decorate(PacuRecordVO vo) {
        vo.setStatusText(PacuStatusEnum.getText(vo.getStatus()));
        vo.setAwarenessText(dictCacheService.getDicDataLabel("biz_operation_awarenessLevelEnum", vo.getAwareness()));
        vo.setDispositionText(dictCacheService.getDicDataLabel("biz_operation_pacuDispositionEnum", vo.getDisposition()));
        vo.setChargeStatusText(AnesthesiaChargeStatusEnum.getText(vo.getChargeStatus()));
        vo.setAnesthesiaTypeText(OperationAnesthesiaMethodEnum.getText(vo.getAnesthesiaType()));

        boolean inRoom = Objects.equals(PacuStatusEnum.IN.getCode(), vo.getStatus());
        Long stay = TimeUtil.elapsedMinutes(vo.getEnterTime(), vo.getLeaveTime() == null ? LocalDateTime.now() : vo.getLeaveTime());
        vo.setStayMinutes(stay);
        vo.setStayDurationText(AnesthesiaCalcs.durationText(stay));
        vo.setBillHours(AnesthesiaCalcs.billHours(stay));

        vo.setCanScore(inRoom);
        vo.setCanLeave(inRoom && vo.getAldreteTotal() != null);
        vo.setCanCharge(!inRoom);
        vo.setCriteriaMet(Integer.valueOf(1).equals(vo.getLeaveCriteriaMet()));
        vo.setWarningText(warningOf(vo, inRoom));
    }

    private String warningOf(PacuRecordVO vo, boolean inRoom) {
        if (inRoom && vo.getAldreteTotal() == null) {
            return "尚未完成 Aldrete 评分";
        }
        if (vo.getAldreteTotal() != null && vo.getAldreteTotal() < AnesthesiaCalcs.ALDRETE_DISCHARGE_MIN) {
            return "Aldrete " + vo.getAldreteTotal() + " 分，未达出室标准 " + AnesthesiaCalcs.ALDRETE_DISCHARGE_MIN + " 分";
        }
        if (Integer.valueOf(AnesthesiaChargeStatusEnum.FAILED.getCode()).equals(vo.getChargeStatus())) {
            return "计费异常：" + vo.getChargeFailReason();
        }
        if (!inRoom && Integer.valueOf(AnesthesiaChargeStatusEnum.PENDING.getCode()).equals(vo.getChargeStatus())) {
            return "已出室但尚未计费";
        }
        return null;
    }
}