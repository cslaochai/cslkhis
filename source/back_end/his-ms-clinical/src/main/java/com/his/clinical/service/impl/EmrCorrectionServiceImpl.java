package com.his.clinical.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.clinical.dto.EmrCorrectionApplyDTO;
import com.his.clinical.dto.EmrCorrectionOpinionDTO;
import com.his.clinical.dto.EmrCorrectionPageQueryDTO;
import com.his.clinical.entity.BizEmrCorrectionRequest;
import com.his.clinical.entity.BizMedicalRecord;
import com.his.clinical.entity.BizMedicalRecordLog;
import com.his.clinical.mapper.BizEmrCorrectionRequestMapper;
import com.his.clinical.mapper.BizMedicalRecordLogMapper;
import com.his.clinical.mapper.BizMedicalRecordMapper;
import com.his.clinical.service.EmrCorrectionService;
import com.his.clinical.vo.EmrCorrectionVO;
import com.his.clinical.vo.MessagePayloadVO;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.BizTypeEnum;
import com.his.common.enums.EmrCorrectionStatusEnum;
import com.his.common.enums.EmrCorrectionTypeEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.utils.UserUtils;
import com.his.spi.service.DeptScopeService;
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
import java.util.List;
import java.util.Objects;

/**
 * 病历更正申请服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmrCorrectionServiceImpl extends ServiceImpl<BizEmrCorrectionRequestMapper, BizEmrCorrectionRequest>
        implements EmrCorrectionService {

    private final BizMedicalRecordMapper bizMedicalRecordMapper;
    private final BizMedicalRecordLogMapper bizMedicalRecordLogMapper;
    private final DeptScopeService deptScopeService;
    private final RedisSequenceService redisSequenceService;
    private final SysMessageService sysMessageService;
    private final SystemFacadeService systemFacadeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmrCorrectionVO apply(EmrCorrectionApplyDTO dto) {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        BizMedicalRecord record = bizMedicalRecordMapper.selectById(dto.getRecordId());
        if (record == null || (record.getDelFlag() != null && record.getDelFlag() == 1)) {
            throw new BusinessException("病历不存在或已删除");
        }
        Integer status = record.getRecordStatus();
        if (Objects.equals(status, RecordStatusEnum.DRAFT.getCode())) {
            throw new BusinessException("病历仍是草稿，可直接续写，无需发起更正申请");
        }
        if (Objects.equals(status, RecordStatusEnum.VOIDED.getCode())) {
            throw new BusinessException("病历已作废，不能发起更正");
        }
        deptScopeService.assertDeptAccessible(record.getDeptId());

        BizEmrCorrectionRequest active = baseMapper.selectActiveByRecordId(record.getId());
        if (active != null) {
            throw new BusinessException("该病历已有在途更正申请（"
                    + EmrCorrectionStatusEnum.getLabel(active.getStatus()) + "），请勿重复发起");
        }

        BizEmrCorrectionRequest request = new BizEmrCorrectionRequest();
        request.setRequestNo(generateRequestNo());
        request.setRecordId(record.getId());
        request.setRecordNo(record.getRecordNo());
        request.setPatientId(record.getPatientId());
        request.setPatientName(record.getPatientName());
        request.setDeptId(record.getDeptId());
        request.setDeptName(record.getDeptName());
        request.setOriginalDoctorId(record.getDoctorId());
        request.setOriginalDoctorName(record.getDoctorName());
        request.setApplicantId(currentUser.getEmployeeId());
        request.setApplicantName(currentUser.getRealName());
        request.setCorrectionType(dto.getCorrectionType());
        request.setReason(dto.getReason());
        request.setPlannedContent(dto.getPlannedContent());
        request.setStatus(EmrCorrectionStatusEnum.DEPT_PENDING.getCode());
        request.setRemark(dto.getRemark());
        request.setCreateTime(LocalDateTime.now());
        this.save(request);

        notifyPermissionHolders("emr:correction:deptApprove", record, request.getRequestNo(),
                "病历更正申请待科室审核：" + record.getRecordNo(),
                String.format("患者 %s（%s）的病历由 %s 发起更正申请：%s，请科室审核。",
                        record.getPatientName(), record.getDeptName(), currentUser.getRealName(), dto.getReason()));
        return decorate(baseMapper.selectCorrectionById(request.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deptApprove(EmrCorrectionOpinionDTO dto) {
        BizEmrCorrectionRequest request = lockAndCheck(dto.getId());
        if (!Objects.equals(request.getStatus(), EmrCorrectionStatusEnum.DEPT_PENDING.getCode())) {
            throw new BusinessException("当前状态（" + EmrCorrectionStatusEnum.getLabelOrUnknown(request.getStatus())
                    + "）不是待科室审核，不能审核");
        }
        CurrentUser currentUser = UserUtils.getCurrentUser();
        request.setDeptApproverId(currentUser.getEmployeeId());
        request.setDeptApproverName(currentUser.getRealName());
        request.setDeptApproveTime(LocalDateTime.now());
        request.setDeptComment(dto.getComment());

        if (Boolean.TRUE.equals(dto.getApproved())) {
            request.setStatus(EmrCorrectionStatusEnum.MEDICAL_PENDING.getCode());
            this.updateById(request);
            notifyPermissionHolders("emr:correction:medicalApprove", loadRecord(request), request.getRequestNo(),
                    "病历更正申请待医务审批：" + request.getRecordNo(),
                    String.format("患者 %s 的病历更正申请已通过科室审核，待医务审批。", request.getPatientName()));
        } else {
            request.setStatus(EmrCorrectionStatusEnum.REJECTED.getCode());
            this.updateById(request);
            notifyApplicant(request, "病历更正申请被科室驳回：" + request.getRecordNo(),
                    "科室审核驳回意见：" + nullToDash(dto.getComment()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void medicalApprove(EmrCorrectionOpinionDTO dto) {
        BizEmrCorrectionRequest request = lockAndCheck(dto.getId());
        if (!Objects.equals(request.getStatus(), EmrCorrectionStatusEnum.MEDICAL_PENDING.getCode())) {
            throw new BusinessException("当前状态（" + EmrCorrectionStatusEnum.getLabelOrUnknown(request.getStatus())
                    + "）不是待医务审批，不能审批");
        }
        CurrentUser currentUser = UserUtils.getCurrentUser();
        request.setMedicalApproverId(currentUser.getEmployeeId());
        request.setMedicalApproverName(currentUser.getRealName());
        request.setMedicalApproveTime(LocalDateTime.now());
        request.setMedicalComment(dto.getComment());

        if (Boolean.TRUE.equals(dto.getApproved())) {
            request.setStatus(EmrCorrectionStatusEnum.APPROVED.getCode());
            this.updateById(request);
            notifyApplicant(request, "病历更正申请已批准：" + request.getRecordNo(),
                    "医务部门已批准更正申请，请在授权范围内回写病历；修改人与时间将全程留痕。");
        } else {
            request.setStatus(EmrCorrectionStatusEnum.REJECTED.getCode());
            this.updateById(request);
            notifyApplicant(request, "病历更正申请被医务部门驳回：" + request.getRecordNo(),
                    "医务审批驳回意见：" + nullToDash(dto.getComment()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long id) {
        BizEmrCorrectionRequest request = lockAndCheck(id);
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (!Objects.equals(request.getApplicantId(), currentUser.getEmployeeId())) {
            throw new BusinessException("仅申请人可撤回该申请");
        }
        if (!Objects.equals(request.getStatus(), EmrCorrectionStatusEnum.DEPT_PENDING.getCode())
                && !Objects.equals(request.getStatus(), EmrCorrectionStatusEnum.MEDICAL_PENDING.getCode())) {
            throw new BusinessException("当前状态不可撤回");
        }
        request.setStatus(EmrCorrectionStatusEnum.REVOKED.getCode());
        this.updateById(request);
    }

    @Override
    public PageResult<EmrCorrectionVO> page(EmrCorrectionPageQueryDTO query) {
        IPage<EmrCorrectionVO> page = baseMapper.selectCorrectionPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query, deptScopeService.scopedDeptIds(null));
        page.getRecords().forEach(this::decorate);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public EmrCorrectionVO getDetail(Long id) {
        EmrCorrectionVO vo = baseMapper.selectCorrectionById(id);
        if (vo == null) {
            throw new BusinessException("更正申请单不存在或已删除");
        }
        deptScopeService.assertDeptAccessible(vo.getDeptId());
        return decorate(vo);
    }

    @Override
    public BizEmrCorrectionRequest findApprovedByRecord(Long recordId) {
        if (recordId == null) {
            return null;
        }
        return baseMapper.selectApprovedByRecordId(recordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markCompleted(Long recordId, Long modifierId, String modifierName) {
        BizEmrCorrectionRequest request = findApprovedByRecord(recordId);
        if (request == null) {
            return;
        }
        request.setStatus(EmrCorrectionStatusEnum.COMPLETED.getCode());
        request.setModifierId(modifierId);
        request.setModifierName(modifierName);
        request.setModifyTime(LocalDateTime.now());
        this.updateById(request);

        BizMedicalRecordLog logRow = new BizMedicalRecordLog();
        logRow.setRecordId(recordId);
        logRow.setRecordNo(request.getRecordNo());
        logRow.setUserId(modifierId);
        logRow.setUserName(modifierName);
        logRow.setOperation("病历更正回写");
        logRow.setFieldName("更正申请单");
        logRow.setOldValue(request.getRequestNo());
        logRow.setNewValue(modifierName);
        bizMedicalRecordLogMapper.insert(logRow);

        try {
            sysMessageService.closeTodoByBiz(BizTypeEnum.EMR_CORRECTION.getType(), recordId, 1);
        } catch (Exception ex) {
            log.warn("[病历更正] 关闭更正待办失败 requestNo={}", request.getRequestNo(), ex);
        }
    }

    // ---------------- 私有 ----------------

    private BizEmrCorrectionRequest lockAndCheck(Long id) {
        BizEmrCorrectionRequest request = this.getById(id);
        if (request == null) {
            throw new BusinessException("更正申请单不存在或已删除");
        }
        deptScopeService.assertDeptAccessible(request.getDeptId());
        return request;
    }

    private BizMedicalRecord loadRecord(BizEmrCorrectionRequest request) {
        return bizMedicalRecordMapper.selectById(request.getRecordId());
    }

    private void notifyPermissionHolders(String permission, BizMedicalRecord record, String requestNo,
                                         String title, String content) {
        if (record == null) {
            return;
        }
        List<EmployeeBriefVO> holders = systemFacadeService.listEmployeesByPermission(permission);
        for (EmployeeBriefVO holder : holders) {
            try {
                MessagePayloadVO payload = new MessagePayloadVO();
                payload.setPatientName(record.getPatientName());
                payload.setRecordNo(record.getRecordNo());
                payload.setOpinion(requestNo);
                sysMessageService.sendSystemMessage(holder.getId(), holder.getEmpName(), title, content,
                        BizTypeEnum.EMR_CORRECTION.getType(), record.getId(), "warning",
                        cn.hutool.json.JSONUtil.toJsonStr(payload), 0);
            } catch (Exception ex) {
                log.warn("[病历更正] 投递审批待办失败 permission={}, holder={}", permission, holder.getId(), ex);
            }
        }
    }

    private void notifyApplicant(BizEmrCorrectionRequest request, String title, String content) {
        if (request.getApplicantId() == null) {
            return;
        }
        try {
            MessagePayloadVO payload = new MessagePayloadVO();
            payload.setPatientName(request.getPatientName());
            payload.setRecordNo(request.getRecordNo());
            payload.setOpinion(request.getRequestNo());
            sysMessageService.sendSystemMessage(request.getApplicantId(), request.getApplicantName(), title, content,
                    BizTypeEnum.EMR_CORRECTION.getType(), request.getRecordId(), "warning",
                    cn.hutool.json.JSONUtil.toJsonStr(payload), 0);
        } catch (Exception ex) {
            log.warn("[病历更正] 通知申请人失败 requestNo={}", request.getRequestNo(), ex);
        }
    }

    private EmrCorrectionVO decorate(EmrCorrectionVO vo) {
        if (vo == null) {
            return null;
        }
        vo.setStatusText(EmrCorrectionStatusEnum.getLabel(vo.getStatus()));
        vo.setCorrectionTypeText(EmrCorrectionTypeEnum.getLabel(vo.getCorrectionType()));
        return vo;
    }

    private String generateRequestNo() {
        return "CR" + LocalDate.now().format(DateFormats.COMPACT_DATE)
                + String.format("%04d", redisSequenceService.next("emr_correction"));
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "无" : value;
    }
}
