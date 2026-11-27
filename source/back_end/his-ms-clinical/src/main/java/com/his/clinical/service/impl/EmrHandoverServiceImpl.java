package com.his.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.clinical.dto.EmrHandoverAssignDTO;
import com.his.clinical.dto.EmrHandoverPageQueryDTO;
import com.his.clinical.entity.BizEmrDraftHandover;
import com.his.clinical.entity.BizMedicalRecord;
import com.his.clinical.entity.BizMedicalRecordLog;
import com.his.clinical.mapper.BizEmrDraftHandoverMapper;
import com.his.clinical.mapper.BizMedicalRecordLogMapper;
import com.his.clinical.mapper.BizMedicalRecordMapper;
import com.his.clinical.service.EmrHandoverService;
import com.his.clinical.vo.EmrHandoverVO;
import com.his.clinical.vo.MessagePayloadVO;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.BizTypeEnum;
import com.his.common.enums.EmrHandoverReasonEnum;
import com.his.common.enums.EmrHandoverStatusEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.utils.UserUtils;
import com.his.spi.service.RedisSequenceService;
import com.his.spi.service.SysMessageService;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.EmployeeBriefVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 病历草稿移交代补服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmrHandoverServiceImpl extends ServiceImpl<BizEmrDraftHandoverMapper, BizEmrDraftHandover>
        implements EmrHandoverService {

    private final BizMedicalRecordMapper bizMedicalRecordMapper;
    private final BizMedicalRecordLogMapper bizMedicalRecordLogMapper;
    private final SystemFacadeService systemFacadeService;
    private final RedisSequenceService redisSequenceService;
    private final SysMessageService sysMessageService;

    @Override
    public List<EmrHandoverVO> listMyReceiving() {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        List<EmrHandoverVO> voList = baseMapper.selectMyReceiving(
                currentUser.getEmployeeId(), EmrHandoverStatusEnum.PENDING.getCode());
        for (EmrHandoverVO vo : voList) {
            vo.setReasonText(EmrHandoverReasonEnum.getLabel(vo.getReason()));
            vo.setStatusText(EmrHandoverStatusEnum.getLabel(vo.getStatus()));
        }
        return voList;
    }

    @Override
    public PageResult<EmrHandoverVO> pageManaged(EmrHandoverPageQueryDTO queryDTO) {
        Page<EmrHandoverVO> page = baseMapper.selectHandoverPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                queryDTO.getStatus(), queryDTO.getReceiverId(), queryDTO.getKeyword());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public List<Long> listPendingReceivingRecordIds(Long employeeId) {
        if (employeeId == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<BizEmrDraftHandover> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizEmrDraftHandover::getReceiverId, employeeId)
                .eq(BizEmrDraftHandover::getStatus, EmrHandoverStatusEnum.PENDING.getCode())
                .select(BizEmrDraftHandover::getRecordId);
        return this.list(wrapper).stream().map(BizEmrDraftHandover::getRecordId).toList();
    }

    @Override
    public List<BizEmrDraftHandover> listPendingByRecordIds(List<Long> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<BizEmrDraftHandover> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BizEmrDraftHandover::getRecordId, recordIds)
                .eq(BizEmrDraftHandover::getStatus, EmrHandoverStatusEnum.PENDING.getCode());
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(EmrHandoverAssignDTO dto) {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        EmployeeBriefVO receiver = systemFacadeService.findEmployee(dto.getReceiverId());
        if (receiver == null || !Objects.equals(receiver.getStatus(), 1)) {
            throw new BusinessException("承接人不存在或已停用，请重新选择");
        }

        List<BizMedicalRecord> records = bizMedicalRecordMapper.selectBatchIds(dto.getRecordIds());
        if (records.size() != dto.getRecordIds().size()) {
            throw new BusinessException("存在不存在的病历，请刷新后重试");
        }
        for (BizMedicalRecord record : records) {
            if (!Objects.equals(record.getRecordStatus(), RecordStatusEnum.DRAFT.getCode())) {
                throw new BusinessException("病历 " + record.getRecordNo() + " 不是草稿，不能移交");
            }
        }

        // 同一病历已有待代补移交单：不重复挂承接人
        LambdaQueryWrapper<BizEmrDraftHandover> openWrapper = new LambdaQueryWrapper<>();
        openWrapper.in(BizEmrDraftHandover::getRecordId, dto.getRecordIds())
                .eq(BizEmrDraftHandover::getStatus, EmrHandoverStatusEnum.PENDING.getCode());
        List<BizEmrDraftHandover> openRows = this.list(openWrapper);
        if (!openRows.isEmpty()) {
            String nos = openRows.stream().map(BizEmrDraftHandover::getRecordNo)
                    .reduce((a, b) -> a + "、" + b).orElse("");
            throw new BusinessException("病历 " + nos + " 已有待代补移交单，请勿重复移交");
        }

        LocalDateTime now = LocalDateTime.now();
        for (BizMedicalRecord record : records) {
            BizEmrDraftHandover handover = new BizEmrDraftHandover();
            handover.setHandoverNo(generateHandoverNo());
            handover.setRecordId(record.getId());
            handover.setRecordNo(record.getRecordNo());
            handover.setPatientId(record.getPatientId());
            handover.setPatientName(record.getPatientName());
            handover.setDeptId(record.getDeptId());
            handover.setDeptName(record.getDeptName());
            handover.setOriginalDoctorId(record.getDoctorId());
            handover.setOriginalDoctorName(record.getDoctorName());
            handover.setReceiverId(receiver.getId());
            handover.setReceiverName(receiver.getEmpName());
            handover.setReason(dto.getReason());
            handover.setStatus(EmrHandoverStatusEnum.PENDING.getCode());
            handover.setOperatorId(currentUser.getEmployeeId());
            handover.setOperatorName(currentUser.getRealName());
            handover.setRemark(dto.getRemark());
            handover.setCreateTime(now);
            this.save(handover);

            // 承接人待办：代补提交后由结诊收尾闭环
            String content = String.format(
                    "%s 接诊患者 %s 的门诊病历由 %s 移交您代补（原因：%s），请尽快补写并完成提交；病历经治医生仍为原接诊医生。",
                    record.getDeptName() == null ? "本科室" : record.getDeptName(),
                    record.getPatientName() == null ? "未知" : record.getPatientName(),
                    record.getDoctorName() == null ? "原接诊医生" : record.getDoctorName(),
                    EmrHandoverReasonEnum.getLabel(dto.getReason(), "其他"));
            MessagePayloadVO payload = new MessagePayloadVO();
            payload.setPatientName(record.getPatientName());
            payload.setRecordNo(record.getRecordNo());
            payload.setHandlerName(currentUser.getRealName());
            sysMessageService.sendSystemMessage(receiver.getId(), receiver.getEmpName(),
                    "病历草稿移交代补：" + record.getRecordNo(), content,
                    BizTypeEnum.EMR_DRAFT_HANDOVER.getType(), record.getId(),
                    "warning", cn.hutool.json.JSONUtil.toJsonStr(payload), 0);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long id) {
        BizEmrDraftHandover handover = this.getById(id);
        if (handover == null) {
            throw new BusinessException("移交单不存在或已删除");
        }
        if (!Objects.equals(handover.getStatus(), EmrHandoverStatusEnum.PENDING.getCode())) {
            throw new BusinessException("仅待代补状态的移交单可撤销");
        }
        handover.setStatus(EmrHandoverStatusEnum.REVOKED.getCode());
        this.updateById(handover);
        try {
            sysMessageService.closeTodoByBiz(BizTypeEnum.EMR_DRAFT_HANDOVER.getType(), handover.getRecordId(), 2);
        } catch (Exception ex) {
            log.warn("[病历移交] 关闭移交代补待办失败 handoverNo={}", handover.getHandoverNo(), ex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markCompleted(Long recordId, Long operatorId, String operatorName) {
        BizEmrDraftHandover handover = findPendingByRecord(recordId);
        if (handover == null) {
            return;
        }
        handover.setStatus(EmrHandoverStatusEnum.COMPLETED.getCode());
        handover.setCompleteTime(LocalDateTime.now());
        handover.setCompleteById(operatorId);
        handover.setCompleteByName(operatorName);
        this.updateById(handover);

        // 代补留痕：经治医生不变，操作人记为代补人
        BizMedicalRecordLog logRow = new BizMedicalRecordLog();
        logRow.setRecordId(recordId);
        logRow.setRecordNo(handover.getRecordNo());
        logRow.setOperation("代补提交");
        logRow.setFieldName("移交代补");
        logRow.setOldValue(handover.getOriginalDoctorName());
        logRow.setNewValue(operatorName);
        bizMedicalRecordLogMapper.insert(logRow);

        try {
            sysMessageService.closeTodoByBiz(BizTypeEnum.EMR_DRAFT_HANDOVER.getType(), recordId, 1);
        } catch (Exception ex) {
            log.warn("[病历移交] 关闭移交代补待办失败 recordId={}", recordId, ex);
        }
    }

    @Override
    public BizEmrDraftHandover findPendingByRecord(Long recordId) {
        if (recordId == null) {
            return null;
        }
        LambdaQueryWrapper<BizEmrDraftHandover> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizEmrDraftHandover::getRecordId, recordId)
                .eq(BizEmrDraftHandover::getStatus, EmrHandoverStatusEnum.PENDING.getCode())
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    private String generateHandoverNo() {
        return "HD" + LocalDate.now().format(DateFormats.COMPACT_DATE)
                + String.format("%04d", redisSequenceService.next("emr_handover"));
    }

}
