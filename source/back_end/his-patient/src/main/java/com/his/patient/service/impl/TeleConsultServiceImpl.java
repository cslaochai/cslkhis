package com.his.patient.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.BizCodeConstants;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.*;
import com.his.patient.entity.BizOnlineConsult;
import com.his.patient.entity.BizPatient;
import com.his.patient.entity.BizTeleConsult;
import com.his.patient.enums.OnlineConsultStatusEnum;
import com.his.patient.enums.TeleConsultStatusEnum;
import com.his.patient.mapper.BizOnlineConsultMapper;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.mapper.BizTeleConsultMapper;
import com.his.patient.service.TeleConsultService;
import com.his.patient.vo.*;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 互联网医院 / 远程会诊服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeleConsultServiceImpl extends ServiceImpl<BizTeleConsultMapper, BizTeleConsult> implements TeleConsultService {

    private final BizTeleConsultMapper bizTeleConsultMapper;
    private final BizOnlineConsultMapper bizOnlineConsultMapper;
    private final BizPatientMapper bizPatientMapper;
    private final RedisSequenceService redisSequenceService;
    private final DeptScopeService deptScopeService;

    // 远程会诊

    private static LocalDateTime parseDateTime(String v) {
        String s = TextUtil.trimToNull(v);
        if (s == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(s.replace(' ', 'T'));
        } catch (Exception e) {
            throw new BusinessException("时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    @Override
    public PageResult<TeleConsultVO> teleListPage(TeleConsultQueryPageDTO dto) {
        Page<TeleConsultVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<Long> deptIds = deptScopeService.scopedDeptIds(dto.getApplyDeptId());
        List<TeleConsultVO> records = bizTeleConsultMapper.selectTelePage(page, TextUtil.trimToNull(dto.getKeyword()),
                dto.getConsultType(), dto.getStatus(), dto.getApplyDeptId(),
                dto.getUrgentOnly(), dto.getOpenOnly(), deptIds);
        records.forEach(this::decorateTele);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    // 线上问诊

    @Override
    public TeleConsultVO teleGetDetailById(Long id) {
        TeleConsultVO vo = requireTeleVo(id);
        deptScopeService.assertDeptAccessible(vo.getApplyDeptId());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeleConsultVO teleUpsert(TeleConsultUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizTeleConsult entity;
        boolean isNew = dto.getId() == null;
        if (isNew) {
            entity = new BizTeleConsult();
            entity.setConsultNo(nextNo(BizCodeConstants.TELE_CONSULT_NO_PREFIX, "TELE_CONSULT"));
            entity.setStatus(TeleConsultStatusEnum.PENDING.getCode());
            entity.setIsUrgent(dto.getIsUrgent() == null ? 0 : dto.getIsUrgent());
        } else {
            entity = requireTeleEntity(dto.getId());
            if (!Objects.equals(entity.getStatus(), TeleConsultStatusEnum.PENDING.getCode())) {
                throw new BusinessException("仅「待安排」的会诊单允许修改（当前：" + teleStatusName(entity.getStatus()) + "）");
            }
        }
        // 申请科室走前端选择（B类）：新单校验提交值；改单未传时沿用旧值同样要校验
        Long applyDeptId = dto.getApplyDeptId() != null ? dto.getApplyDeptId()
                : (isNew ? null : entity.getApplyDeptId());
        if (applyDeptId != null) {
            deptScopeService.assertDeptAccessible(applyDeptId);
        }
        BizPatient patient = requirePatient(dto.getPatientId());
        entity.setPatientId(patient.getId());
        entity.setPatientNo(patient.getPatientNo());
        entity.setPatientName(patient.getPatientName());
        entity.setAdmissionId(dto.getAdmissionId());
        entity.setApplyDeptId(dto.getApplyDeptId());
        entity.setApplyDeptName(dto.getApplyDeptId() == null ? null : bizTeleConsultMapper.selectDeptName(dto.getApplyDeptId()));
        entity.setApplyDoctorId(dto.getApplyDoctorId() == null ? operatorUser.getEmployeeId() : dto.getApplyDoctorId());
        entity.setApplyDoctor(operatorUser.getRealName());
        entity.setConsultType(dto.getConsultType());
        entity.setExpertHospital(TextUtil.cut(dto.getExpertHospital(), 128));
        entity.setExpertDept(TextUtil.cut(dto.getExpertDept(), 128));
        entity.setExpertName(TextUtil.cut(dto.getExpertName(), 64));
        entity.setExpertTitle(TextUtil.cut(dto.getExpertTitle(), 32));
        entity.setPurpose(TextUtil.cut(dto.getPurpose(), 500));
        entity.setDiagnosis(TextUtil.cut(dto.getDiagnosis(), 500));
        entity.setFee(dto.getFee());
        entity.setRemark(TextUtil.cut(dto.getRemark(), 512));
        if (isNew) {
            bizTeleConsultMapper.insert(entity);
        } else {
            bizTeleConsultMapper.updateById(entity);
        }
        return requireTeleVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeleConsultVO teleArrange(TeleArrangeDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizTeleConsult entity = requireTeleEntity(dto.getId());
        deptScopeService.assertDeptAccessible(entity.getApplyDeptId());
        if (!Objects.equals(entity.getStatus(), TeleConsultStatusEnum.PENDING.getCode())) {
            throw new BusinessException("仅「待安排」的会诊单可安排（当前：" + teleStatusName(entity.getStatus()) + "）");
        }
        entity.setStatus(TeleConsultStatusEnum.ARRANGED.getCode());
        entity.setPlanTime(parseDateTime(dto.getPlanTime()));
        entity.setDurationMin(dto.getDurationMin());
        entity.setPlatform(TextUtil.cut(dto.getPlatform(), 64));
        entity.setMeetNo(TextUtil.cut(dto.getMeetNo(), 64));
        if (TextUtil.hasText(dto.getExpertHospital())) {
            entity.setExpertHospital(TextUtil.cut(dto.getExpertHospital(), 128));
        }
        if (TextUtil.hasText(dto.getExpertDept())) {
            entity.setExpertDept(TextUtil.cut(dto.getExpertDept(), 128));
        }
        if (TextUtil.hasText(dto.getExpertName())) {
            entity.setExpertName(TextUtil.cut(dto.getExpertName(), 64));
        }
        if (TextUtil.hasText(dto.getExpertTitle())) {
            entity.setExpertTitle(TextUtil.cut(dto.getExpertTitle(), 32));
        }
        entity.setArrangeBy(operatorUser.getRealName());
        entity.setArrangeTime(TimeUtil.nowSeconds());
        bizTeleConsultMapper.updateById(entity);
        return requireTeleVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeleConsultVO teleComplete(TeleActionDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizTeleConsult entity = requireTeleEntity(dto.getId());
        deptScopeService.assertDeptAccessible(entity.getApplyDeptId());
        if (!Objects.equals(entity.getStatus(), TeleConsultStatusEnum.ARRANGED.getCode())) {
            throw new BusinessException("仅「已安排」的会诊单可出意见完成（当前：" + teleStatusName(entity.getStatus()) + "）");
        }
        String opinion = TextUtil.trimToNull(dto.getContent());
        entity.setStatus(TeleConsultStatusEnum.DONE.getCode());
        entity.setOpinion(TextUtil.cut(opinion, 1000));
        entity.setCompleteBy(operatorUser.getRealName());
        entity.setCompleteTime(TimeUtil.nowSeconds());
        bizTeleConsultMapper.updateById(entity);
        return requireTeleVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeleConsultVO teleCancel(TeleActionDTO dto) {
        BizTeleConsult entity = requireTeleEntity(dto.getId());
        deptScopeService.assertDeptAccessible(entity.getApplyDeptId());
        Integer st = entity.getStatus();
        if (Objects.equals(st, TeleConsultStatusEnum.DONE.getCode()) || Objects.equals(st, TeleConsultStatusEnum.CANCELED.getCode())) {
            throw new BusinessException("已完成/已取消的会诊单不可再取消");
        }
        String reason = TextUtil.trimToNull(dto.getContent());
        entity.setStatus(TeleConsultStatusEnum.CANCELED.getCode());
        entity.setCancelReason(TextUtil.cut(reason, 500));
        bizTeleConsultMapper.updateById(entity);
        return requireTeleVo(entity.getId());
    }

    @Override
    public boolean teleDeleteById(Long id) {
        BizTeleConsult entity = requireTeleEntity(id);
        deptScopeService.assertDeptAccessible(entity.getApplyDeptId());
        if (!Objects.equals(entity.getStatus(), TeleConsultStatusEnum.PENDING.getCode())) {
            throw new BusinessException("仅「待安排」的会诊单可删除");
        }
        return bizTeleConsultMapper.deleteById(id) > 0;
    }

    @Override
    public PageResult<OnlineConsultVO> onlineListPage(OnlineQueryPageDTO dto) {
        Page<OnlineConsultVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<Long> deptIds = deptScopeService.scopedDeptIds(dto.getDeptId());
        List<OnlineConsultVO> records = bizOnlineConsultMapper.selectOnlinePage(page, TextUtil.trimToNull(dto.getKeyword()),
                dto.getConsultType(), dto.getStatus(), dto.getDeptId(), dto.getDoctorId(), dto.getWaitingOnly(), deptIds);
        records.forEach(this::decorateOnline);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public OnlineConsultVO onlineGetDetailById(Long id) {
        OnlineConsultVO vo = requireOnlineVo(id);
        deptScopeService.assertDeptAccessible(vo.getDeptId());
        return vo;
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OnlineConsultVO onlineApply(OnlineApplyDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizPatient patient = requirePatient(dto.getPatientId());
        if (dto.getDeptId() != null) {
            deptScopeService.assertDeptAccessible(dto.getDeptId());
        }
        BizOnlineConsult entity = new BizOnlineConsult();
        entity.setConsultNo(nextNo(BizCodeConstants.ONLINE_CONSULT_NO_PREFIX, "ONLINE_CONSULT"));
        entity.setPatientId(patient.getId());
        entity.setPatientNo(patient.getPatientNo());
        entity.setPatientName(patient.getPatientName());
        entity.setDeptId(dto.getDeptId());
        entity.setDeptName(dto.getDeptId() == null ? null : bizOnlineConsultMapper.selectDeptName(dto.getDeptId()));
        entity.setDoctorId(dto.getDoctorId());
        entity.setDoctorName(dto.getDoctorId() == null ? null : operatorUser.getRealName());
        entity.setConsultType(dto.getConsultType());
        entity.setChiefComplaint(TextUtil.cut(dto.getChiefComplaint(), 1000));
        entity.setStatus(OnlineConsultStatusEnum.WAITING.getCode());
        entity.setNeedVisit(0);
        entity.setFee(dto.getFee());
        entity.setApplyTime(TimeUtil.nowSeconds());
        entity.setRemark(TextUtil.cut(dto.getRemark(), 512));
        bizOnlineConsultMapper.insert(entity);
        return requireOnlineVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OnlineConsultVO onlineAccept(Long id) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizOnlineConsult entity = requireOnlineEntity(id);
        deptScopeService.assertDeptAccessible(entity.getDeptId());
        if (!Objects.equals(entity.getStatus(), OnlineConsultStatusEnum.WAITING.getCode())) {
            throw new BusinessException("仅「待接诊」的问诊单可接诊（当前：" + onlineStatusName(entity.getStatus()) + "）");
        }
        entity.setStatus(OnlineConsultStatusEnum.ACCEPTED.getCode());
        entity.setAcceptBy(operatorUser.getRealName());
        entity.setAcceptTime(TimeUtil.nowSeconds());
        if (entity.getDoctorId() == null) {
            entity.setDoctorId(operatorUser.getEmployeeId());
        }
        if (!TextUtil.hasText(entity.getDoctorName())) {
            entity.setDoctorName(operatorUser.getRealName());
        }
        bizOnlineConsultMapper.updateById(entity);
        return requireOnlineVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OnlineConsultVO onlineReply(OnlineReplyDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizOnlineConsult entity = requireOnlineEntity(dto.getId());
        deptScopeService.assertDeptAccessible(entity.getDeptId());
        if (!Objects.equals(entity.getStatus(), OnlineConsultStatusEnum.ACCEPTED.getCode())) {
            throw new BusinessException("仅「接诊中」的问诊单可回复（当前：" + onlineStatusName(entity.getStatus()) + "）");
        }
        entity.setStatus(OnlineConsultStatusEnum.DONE.getCode());
        entity.setReply(TextUtil.cut(dto.getReply(), 1000));
        entity.setAdvice(TextUtil.cut(dto.getAdvice(), 500));
        entity.setNeedVisit(dto.getNeedVisit() == null ? 0 : dto.getNeedVisit());
        entity.setFinishBy(operatorUser.getRealName());
        entity.setFinishTime(TimeUtil.nowSeconds());
        bizOnlineConsultMapper.updateById(entity);
        return requireOnlineVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OnlineConsultVO onlineReject(TeleActionDTO dto) {
        BizOnlineConsult entity = requireOnlineEntity(dto.getId());
        deptScopeService.assertDeptAccessible(entity.getDeptId());
        Integer st = entity.getStatus();
        if (Objects.equals(st, OnlineConsultStatusEnum.DONE.getCode()) || Objects.equals(st, OnlineConsultStatusEnum.REJECTED.getCode())) {
            throw new BusinessException("已完成/已退诊的问诊单不可再退诊");
        }
        String reason = TextUtil.trimToNull(dto.getContent());
        entity.setStatus(OnlineConsultStatusEnum.REJECTED.getCode());
        entity.setRejectReason(TextUtil.cut(reason, 500));
        bizOnlineConsultMapper.updateById(entity);
        return requireOnlineVo(entity.getId());
    }

    @Override
    public boolean onlineDeleteById(Long id) {
        BizOnlineConsult entity = requireOnlineEntity(id);
        deptScopeService.assertDeptAccessible(entity.getDeptId());
        if (!Objects.equals(entity.getStatus(), OnlineConsultStatusEnum.WAITING.getCode())) {
            throw new BusinessException("仅「待接诊」的问诊单可删除");
        }
        return bizOnlineConsultMapper.deleteById(id) > 0;
    }

    @Override
    public TeleConsultStatVO stat() {
        List<Long> deptIds = deptScopeService.scopedDeptIds(null);
        TeleConsultStatVO vo = new TeleConsultStatVO();
        long tp = 0, ta = 0, td = 0, tc = 0;
        for (TeleConsultStatusCountVO row : bizTeleConsultMapper.countByStatus(deptIds)) {
            long c = row.getCnt() == null ? 0L : row.getCnt();
            if (row.getStatus() == null) {
                continue;
            }
            switch (row.getStatus()) {
                case 1 -> tp = c;
                case 2 -> ta = c;
                case 3 -> td = c;
                case 4 -> tc = c;
                default -> {
                }
            }
        }
        vo.setTelePending(tp);
        vo.setTeleArranged(ta);
        vo.setTeleDone(td);
        vo.setTeleCanceled(tc);
        vo.setTeleTotal(tp + ta + td + tc);

        long ow = 0, oa = 0, od = 0, orj = 0;
        for (OnlineConsultStatusCountVO row : bizOnlineConsultMapper.countByStatus(deptIds)) {
            long c = row.getCnt() == null ? 0L : row.getCnt();
            if (row.getStatus() == null) {
                continue;
            }
            switch (row.getStatus()) {
                case 1 -> ow = c;
                case 2 -> oa = c;
                case 3 -> od = c;
                case 4 -> orj = c;
                default -> {
                }
            }
        }
        vo.setOnlineWaiting(ow);
        vo.setOnlineAccepted(oa);
        vo.setOnlineDone(od);
        vo.setOnlineRejected(orj);
        vo.setOnlineTotal(ow + oa + od + orj);
        return vo;
    }

    private void decorateTele(TeleConsultVO vo) {
        Integer st = vo.getStatus();
        boolean pending = Objects.equals(st, TeleConsultStatusEnum.PENDING.getCode());
        boolean terminal = Objects.equals(st, TeleConsultStatusEnum.DONE.getCode())
                || Objects.equals(st, TeleConsultStatusEnum.CANCELED.getCode());
        vo.setCanEdit(pending);
        vo.setCanArrange(pending);
        vo.setCanComplete(Objects.equals(st, TeleConsultStatusEnum.ARRANGED.getCode()));
        vo.setCanCancel(!terminal);
        vo.setCanDelete(pending);
    }

    private void decorateOnline(OnlineConsultVO vo) {
        Integer st = vo.getStatus();
        boolean waiting = Objects.equals(st, OnlineConsultStatusEnum.WAITING.getCode());
        boolean accepted = Objects.equals(st, OnlineConsultStatusEnum.ACCEPTED.getCode());
        vo.setCanAccept(waiting);
        vo.setCanReply(accepted);
        vo.setCanReject(waiting || accepted);
        vo.setCanDelete(waiting);
    }

    private BizPatient requirePatient(Long patientId) {
        BizPatient patient = bizPatientMapper.selectById(patientId);
        if (patient == null || !Objects.equals(patient.getDelFlag(), 0)) {
            throw new BusinessException("患者不存在或已删除");
        }
        return patient;
    }

    private TeleConsultVO requireTeleVo(Long id) {
        TeleConsultVO vo = bizTeleConsultMapper.selectTeleById(id);
        if (vo == null) {
            throw new BusinessException("远程会诊单不存在或已删除");
        }
        decorateTele(vo);
        return vo;
    }

    private BizTeleConsult requireTeleEntity(Long id) {
        BizTeleConsult entity = bizTeleConsultMapper.selectById(id);
        if (entity == null || !Objects.equals(entity.getDelFlag(), 0)) {
            throw new BusinessException("远程会诊单不存在或已删除");
        }
        return entity;
    }

    private OnlineConsultVO requireOnlineVo(Long id) {
        OnlineConsultVO vo = bizOnlineConsultMapper.selectOnlineById(id);
        if (vo == null) {
            throw new BusinessException("问诊单不存在或已删除");
        }
        decorateOnline(vo);
        return vo;
    }

    private BizOnlineConsult requireOnlineEntity(Long id) {
        BizOnlineConsult entity = bizOnlineConsultMapper.selectById(id);
        if (entity == null || !Objects.equals(entity.getDelFlag(), 0)) {
            throw new BusinessException("问诊单不存在或已删除");
        }
        return entity;
    }

    private String teleStatusName(Integer status) {
        return switch (status == null ? 0 : status) {
            case 1 -> "待安排";
            case 2 -> "已安排";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    private String onlineStatusName(Integer status) {
        return switch (status == null ? 0 : status) {
            case 1 -> "待接诊";
            case 2 -> "接诊中";
            case 3 -> "已完成";
            case 4 -> "已退诊";
            default -> "未知";
        };
    }

    private String nextNo(String prefix, String module) {
        return prefix + LocalDate.now().format(DateFormats.COMPACT_DATE) + String.format("%04d", redisSequenceService.next(module));
    }
}
