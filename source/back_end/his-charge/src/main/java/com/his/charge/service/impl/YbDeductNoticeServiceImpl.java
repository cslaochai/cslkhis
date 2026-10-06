package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.charge.api.PatientGateway;
import com.his.charge.dto.*;
import com.his.charge.entity.BizInsuranceSettlement;
import com.his.charge.entity.BizYbDeductLog;
import com.his.charge.entity.BizYbDeductNotice;
import com.his.charge.entity.BizYbInspection;
import com.his.charge.enums.YbDeductStatusEnum;
import com.his.charge.mapper.BizYbDeductLogMapper;
import com.his.charge.mapper.BizYbDeductNoticeMapper;
import com.his.charge.mapper.BizYbInspectionMapper;
import com.his.charge.service.InsuranceSettlementService;
import com.his.charge.service.YbDeductNoticeService;
import com.his.charge.vo.*;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 扣款通知服务实现（状态机见 YbDeductStatusEnum；每个动作都写一条留痕）。
 *
 * <p>口径要点：
 * 超期只是展示态（期限早于当天且还没结案），不落列、不起定时任务，避免「日期已过、状态还没刷」；
 * 缴回金额必须等于扣款金额，差额走财务另走院内承担流程，不允许在这里抹平；
 * 共担金额之和 ≤ 扣款金额（差额视为院方承担），不为凑数硬塞给个人。
 */
@Service
@RequiredArgsConstructor
public class YbDeductNoticeServiceImpl implements YbDeductNoticeService {

    /**
     * 留痕动作（字典 his_yb_deduct_action）
     */
    private static final int ACTION_CREATE = 1;
    private static final int ACTION_APPEAL = 2;
    private static final int ACTION_APPEAL_RESULT = 3;
    private static final int ACTION_CONFIRM = 4;
    private static final int ACTION_PAYBACK = 5;
    private static final int ACTION_CANCEL = 6;

    /**
     * 损失承担方式（字典 his_yb_loss_bear）
     */
    private static final int BEAR_HOSPITAL = 1;
    private static final int BEAR_DEPT = 2;
    private static final int BEAR_EMP = 3;
    private static final int BEAR_BOTH = 4;

    private final BizYbDeductNoticeMapper noticeMapper;
    private final BizYbDeductLogMapper logMapper;
    private final BizYbInspectionMapper inspectionMapper;
    private final InsuranceSettlementService settlementService;
    private final PatientGateway patientGateway;
    private final RedisSequenceService sequenceService;

    @Override
    public PageResult<DeductNoticeListVO> listPage(DeductNoticeQueryPageDTO queryDTO) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<BizYbDeductNotice> wrapper = new LambdaQueryWrapper<BizYbDeductNotice>()
                .eq(queryDTO.getDeductStatus() != null, BizYbDeductNotice::getDeductStatus, queryDTO.getDeductStatus())
                .eq(queryDTO.getSourceType() != null, BizYbDeductNotice::getSourceType, queryDTO.getSourceType())
                .eq(queryDTO.getViolationType() != null, BizYbDeductNotice::getViolationType, queryDTO.getViolationType())
                .eq(queryDTO.getInspectionId() != null, BizYbDeductNotice::getInspectionId, queryDTO.getInspectionId())
                .and(isText(queryDTO.getKeyword()), w -> w
                        .like(BizYbDeductNotice::getDeductNo, queryDTO.getKeyword())
                        .or().like(BizYbDeductNotice::getPatientName, queryDTO.getKeyword())
                        .or().like(BizYbDeductNotice::getDeptName, queryDTO.getKeyword())
                        .or().like(BizYbDeductNotice::getInspectionNo, queryDTO.getKeyword())
                        .or().like(BizYbDeductNotice::getViolationDesc, queryDTO.getKeyword()))
                .orderByAsc(BizYbDeductNotice::getDeductStatus)
                .orderByAsc(BizYbDeductNotice::getHandleDeadline)
                .orderByDesc(BizYbDeductNotice::getId);
        if (Boolean.TRUE.equals(queryDTO.getOnlyOverdue())) {
            wrapper.in(BizYbDeductNotice::getDeductStatus, YbDeductStatusEnum.PENDING_CONFIRM.getCode(), YbDeductStatusEnum.APPEALING.getCode())
                    .lt(BizYbDeductNotice::getHandleDeadline, today);
        }
        IPage<BizYbDeductNotice> page = noticeMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<DeductNoticeListVO> voList = page.getRecords().stream()
                .map(entity -> toVO(entity, today)).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public DeductNoticeDetailVO getDetailById(Long id) {
        BizYbDeductNotice entity = require(id);
        DeductNoticeDetailVO vo = new DeductNoticeDetailVO();
        BeanUtils.copyProperties(entity, vo);
        LocalDate today = LocalDate.now();
        vo.setOverdue(isOverdue(entity, today));
        vo.setDeadlineDays(deadlineDays(entity, today));
        vo.setLogs(logMapper.selectList(new LambdaQueryWrapper<BizYbDeductLog>()
                        .eq(BizYbDeductLog::getNoticeId, entity.getId())
                        .orderByAsc(BizYbDeductLog::getOperateTime, BizYbDeductLog::getId))
                .stream().map(this::toLogVO).toList());
        return vo;
    }

    @Override
    public DeductSummaryVO summary() {
        DeductSummaryVO vo = noticeMapper.summary();
        return vo == null ? new DeductSummaryVO() : vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeductNoticeListVO upsert(DeductNoticeUpsertDTO dto) {
        if (dto.getDeductAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("扣款金额必须大于 0");
        }
        if (dto.getHandleDeadline().isBefore(dto.getNoticeDate())) {
            throw new BusinessException("处理期限不能早于通知日期");
        }
        BizYbDeductNotice entity;
        boolean creating = dto.getId() == null;
        if (creating) {
            entity = new BizYbDeductNotice();
            entity.setDeductNo(sequenceService.generateYbDeductNo());
            entity.setDeductStatus(YbDeductStatusEnum.PENDING_CONFIRM.getCode());
        } else {
            entity = require(dto.getId());
            if (!YbDeductStatusEnum.PENDING_CONFIRM.matches(entity.getDeductStatus())) {
                throw new BusinessException("仅「待确认」的扣款通知可修改，已进入申诉/确认/缴回流程的单据请走对应动作");
            }
        }
        entity.setSourceType(dto.getSourceType());
        entity.setInspectionId(dto.getInspectionId());
        entity.setInspectionNo(resolveInspectionNo(dto.getInspectionId(), dto.getSourceType()));
        entity.setSettlementId(dto.getSettlementId());
        entity.setSettlementNo(resolveSettlementNo(dto.getSettlementId()));
        entity.setEncounterType(dto.getEncounterType());
        entity.setEncounterId(dto.getEncounterId());
        entity.setPatientId(dto.getPatientId());
        // 患者姓名/编号以库里的事实为准：前端传的是选择器快照，一旦漏传或传错，扣款单上就是空白
        PatientBriefVO patient = dto.getPatientId() == null ? null : patientGateway.findPatient(dto.getPatientId());
        if (dto.getPatientId() != null && patient == null) {
            throw new BusinessException("患者不存在或已删除");
        }
        entity.setPatientName(cut(patient == null ? dto.getPatientName() : patient.getPatientName(), 50));
        entity.setPatientNo(cut(patient == null ? dto.getPatientNo() : patient.getPatientNo(), 32));
        entity.setDeptId(dto.getDeptId());
        entity.setDeptName(cut(dto.getDeptName(), 100));
        entity.setDoctorName(cut(dto.getDoctorName(), 50));
        entity.setViolationType(dto.getViolationType());
        entity.setViolationDesc(cut(dto.getViolationDesc(), 500));
        entity.setDeductAmount(dto.getDeductAmount());
        entity.setNoticeDate(dto.getNoticeDate());
        entity.setHandleDeadline(dto.getHandleDeadline());
        entity.setRemark(cut(dto.getRemark(), 500));
        entity.setUpdateBy(UserUtils.getCurrentEmployeeName());
        if (creating) {
            noticeMapper.insert(entity);
            writeLog(entity.getId(), ACTION_CREATE, "扣款通知录入：" + entity.getDeductNo()
                    + "，金额 " + entity.getDeductAmount() + " 元", null);
        } else {
            noticeMapper.updateById(entity);
        }
        return toVO(entity, LocalDate.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appeal(DeductAppealDTO dto) {
        BizYbDeductNotice entity = require(dto.getId());
        if (!YbDeductStatusEnum.PENDING_CONFIRM.matches(entity.getDeductStatus())) {
            throw new BusinessException("仅「待确认」的扣款通知可发起申诉");
        }
        String operator = UserUtils.getCurrentEmployeeName();
        entity.setAppealReason(cut(dto.getAppealReason(), 500));
        entity.setAppealMaterial(cut(dto.getAppealMaterial(), 500));
        entity.setAppealBy(operator);
        entity.setAppealTime(LocalDateTime.now());
        entity.setDeductStatus(YbDeductStatusEnum.APPEALING.getCode());
        entity.setUpdateBy(operator);
        noticeMapper.updateById(entity);
        writeLog(entity.getId(), ACTION_APPEAL, "提交申诉：" + entity.getAppealReason(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appealResult(DeductAppealResultDTO dto) {
        BizYbDeductNotice entity = require(dto.getId());
        if (!YbDeductStatusEnum.APPEALING.matches(entity.getDeductStatus())) {
            throw new BusinessException("仅「申诉中」的扣款通知可录入申诉结果");
        }
        Integer result = dto.getAppealResult();
        if (result == null || (result != 1 && result != 2)) {
            throw new BusinessException("申诉结果只能是 1-成功 或 2-驳回");
        }
        String operator = UserUtils.getCurrentEmployeeName();
        entity.setAppealResult(result);
        entity.setAppealResultRemark(cut(dto.getAppealResultRemark(), 500));
        entity.setAppealResultBy(operator);
        entity.setAppealResultTime(LocalDateTime.now());
        entity.setDeductStatus(result == 1 ? YbDeductStatusEnum.APPEAL_SUCCESS.getCode() : YbDeductStatusEnum.WAIT_PAY.getCode());
        entity.setUpdateBy(operator);
        noticeMapper.updateById(entity);
        writeLog(entity.getId(), ACTION_APPEAL_RESULT,
                (result == 1 ? "医保局回复：申诉成功，扣款撤销" : "医保局回复：申诉驳回，维持扣款待缴")
                        + (isText(entity.getAppealResultRemark()) ? "（" + entity.getAppealResultRemark() + "）" : ""),
                null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(DeductConfirmDTO dto) {
        BizYbDeductNotice entity = require(dto.getId());
        int status = entity.getDeductStatus();
        if (status != YbDeductStatusEnum.PENDING_CONFIRM.getCode() && status != YbDeductStatusEnum.WAIT_PAY.getCode()) {
            throw new BusinessException("仅「待确认」或「维持扣款待缴」的扣款通知可确认追责");
        }
        BigDecimal deduct = entity.getDeductAmount();
        BigDecimal deptAmount = money(dto.getBearDeptAmount());
        BigDecimal empAmount = money(dto.getBearEmpAmount());
        switch (dto.getLossBearType()) {
            case BEAR_HOSPITAL -> {
                deptAmount = BigDecimal.ZERO;
                empAmount = BigDecimal.ZERO;
            }
            case BEAR_DEPT -> deptAmount = deptAmount.compareTo(BigDecimal.ZERO) > 0 ? deptAmount : deduct;
            case BEAR_EMP -> empAmount = empAmount.compareTo(BigDecimal.ZERO) > 0 ? empAmount : deduct;
            case BEAR_BOTH -> {
                if (deptAmount.compareTo(BigDecimal.ZERO) <= 0 && empAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("科室+个人共担时必须至少填一侧的分摊金额");
                }
                if (deptAmount.add(empAmount).compareTo(deduct) > 0) {
                    throw new BusinessException("科室分摊 " + deptAmount + " + 个人分摊 " + empAmount
                            + " 已超过扣款金额 " + deduct + "，分摊不能超出实扣");
                }
            }
            default -> throw new BusinessException("损失承担方式不合法（1-院方 2-科室 3-个人 4-科室+个人共担）");
        }
        String operator = UserUtils.getCurrentEmployeeName();
        entity.setLiableDeptId(dto.getLiableDeptId());
        entity.setLiableDeptName(cut(dto.getLiableDeptName(), 100));
        entity.setLiableEmpName(cut(dto.getLiableEmpName(), 64));
        entity.setLossBearType(dto.getLossBearType());
        entity.setBearDeptAmount(deptAmount);
        entity.setBearEmpAmount(empAmount);
        entity.setConfirmBy(operator);
        entity.setConfirmTime(LocalDateTime.now());
        entity.setDeductStatus(YbDeductStatusEnum.WAIT_PAY.getCode());
        entity.setUpdateBy(operator);
        noticeMapper.updateById(entity);
        writeLog(entity.getId(), ACTION_CONFIRM, "确认扣款并追责：承担方式 " + entity.getLossBearType()
                + "，科室 " + deptAmount + " 元 / 个人 " + empAmount + " 元", deduct);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payback(DeductPaybackDTO dto) {
        BizYbDeductNotice entity = require(dto.getId());
        if (!YbDeductStatusEnum.WAIT_PAY.matches(entity.getDeductStatus())) {
            throw new BusinessException("仅「维持扣款待缴」的扣款通知可录入缴回");
        }
        if (!isText(entity.getConfirmBy())) {
            throw new BusinessException("请先完成「确认扣款并追责」再录入缴回，否则钱退了但没人对这笔扣款负责");
        }
        if (dto.getPaidAmount().compareTo(entity.getDeductAmount()) != 0) {
            throw new BusinessException("缴回金额必须等于扣款金额 " + entity.getDeductAmount()
                    + " 元；差额请由院内承担流程另行处理，不要在这里抹平");
        }
        String operator = UserUtils.getCurrentEmployeeName();
        entity.setPaidAmount(dto.getPaidAmount());
        entity.setPaybackDate(dto.getPaybackDate());
        entity.setPaybackVoucher(cut(dto.getPaybackVoucher(), 100));
        entity.setPaybackBy(operator);
        entity.setPaybackTime(LocalDateTime.now());
        entity.setDeductStatus(YbDeductStatusEnum.PAID_BACK.getCode());
        entity.setUpdateBy(operator);
        noticeMapper.updateById(entity);
        writeLog(entity.getId(), ACTION_PAYBACK, "财务缴回医保基金：" + entity.getPaidAmount()
                + " 元，凭证 " + entity.getPaybackVoucher(), entity.getPaidAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(YbCancelDTO dto) {
        BizYbDeductNotice entity = require(dto.getId());
        if (!YbDeductStatusEnum.PENDING_CONFIRM.matches(entity.getDeductStatus())) {
            throw new BusinessException("仅「待确认」的扣款通知可作废；已进入申诉或缴回流程的请走对应动作");
        }
        String operator = UserUtils.getCurrentEmployeeName();
        entity.setCancelReason(cut(dto.getReason(), 500));
        entity.setCancelBy(operator);
        entity.setCancelTime(LocalDateTime.now());
        entity.setDeductStatus(YbDeductStatusEnum.CANCELLED.getCode());
        entity.setUpdateBy(operator);
        noticeMapper.updateById(entity);
        writeLog(entity.getId(), ACTION_CANCEL, "作废：" + entity.getCancelReason(), null);
    }

    private BizYbDeductNotice require(Long id) {
        BizYbDeductNotice entity = id == null ? null : noticeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("扣款通知不存在或已删除");
        }
        return entity;
    }

    /**
     * sourceType=1 表示飞检现场发现，必须挂得上批次，否则「问题从哪来」这条线就断了
     */
    private String resolveInspectionNo(Long inspectionId, Integer sourceType) {
        if (inspectionId == null) {
            if (Integer.valueOf(1).equals(sourceType)) {
                throw new BusinessException("来源为「飞检现场发现」时必须关联飞检批次");
            }
            return null;
        }
        BizYbInspection inspection = inspectionMapper.selectById(inspectionId);
        if (inspection == null) {
            throw new BusinessException("关联的飞检批次不存在或已删除");
        }
        return inspection.getInspectNo();
    }

    private String resolveSettlementNo(Long settlementId) {
        if (settlementId == null) {
            return null;
        }
        BizInsuranceSettlement settlement = settlementService.getSettlementDetail(settlementId);
        if (settlement == null) {
            throw new BusinessException("关联的医保结算清单不存在");
        }
        return settlement.getSettlementNo();
    }

    private void writeLog(Long noticeId, int action, String detail, BigDecimal amount) {
        BizYbDeductLog log = new BizYbDeductLog();
        log.setNoticeId(noticeId);
        log.setAction(action);
        log.setDetail(cut(detail, 1000));
        log.setAmount(amount);
        log.setOperator(UserUtils.getCurrentEmployeeName());
        log.setOperateTime(LocalDateTime.now());
        logMapper.insert(log);
    }

    private DeductNoticeListVO toVO(BizYbDeductNotice entity, LocalDate today) {
        DeductNoticeListVO vo = new DeductNoticeListVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setOverdue(isOverdue(entity, today));
        vo.setDeadlineDays(deadlineDays(entity, today));
        return vo;
    }

    private boolean isOverdue(BizYbDeductNotice entity, LocalDate today) {
        boolean open = entity.getDeductStatus() == YbDeductStatusEnum.PENDING_CONFIRM.getCode()
                || entity.getDeductStatus() == YbDeductStatusEnum.APPEALING.getCode();
        return open && entity.getHandleDeadline() != null && entity.getHandleDeadline().isBefore(today);
    }

    private Integer deadlineDays(BizYbDeductNotice entity, LocalDate today) {
        if (entity.getHandleDeadline() == null) {
            return null;
        }
        return (int) ChronoUnit.DAYS.between(today, entity.getHandleDeadline());
    }

    private DeductLogVO toLogVO(BizYbDeductLog entity) {
        DeductLogVO vo = new DeductLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isText(String text) {
        return text != null && !text.isBlank();
    }

    private String cut(String text, int max) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
