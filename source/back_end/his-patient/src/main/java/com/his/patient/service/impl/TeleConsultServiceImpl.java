package com.his.patient.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.Constants;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.exception.BusinessException;
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
import com.his.patient.vo.OnlineConsultVO;
import com.his.patient.vo.TeleConsultStatVO;
import com.his.patient.vo.TeleConsultVO;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 互联网医院 / 远程会诊服务实现。
 *
 * <p>口径：
 * <ol>
 *   <li>远程会诊：1待安排 → 2已安排 → 3已完成（意见必填，终态）；非终态 → 4已取消（原因必填，终态）。
 *       <b>已安排才允许出意见</b> —— 没安排过的会诊不能出意见。</li>
 *   <li>线上问诊：1待接诊 → 2接诊中 → 3已完成（回复必填，终态）；未结束 → 4已退诊（原因必填，终态）。
 *       回复即结束，不留「接诊中但没回复」的悬挂单。</li>
 *   <li>患者快照服务端重查（患者基本信息同模块直读），不信任前端传的姓名编号。</li>
 *   <li>费用 fee 只是价目快照，<b>不在此处计费</b>（计费走 charge 域，避免双计）。</li>
 *   <li>操作人一律服务端取当前登录人；按钮可用性 can* 服务端派生。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeleConsultServiceImpl implements TeleConsultService {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BizTeleConsultMapper teleMapper;
    private final BizOnlineConsultMapper onlineMapper;
    private final BizPatientMapper patientMapper;
    private final RedisSequenceService sequenceService;

    // 远程会诊

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private static String currentName() {
        String name = UserUtils.getCurrentEmployeeName();
        return name == null ? "系统" : name;
    }

    private static long toLong(Object v) {
        if (v == null) {
            return 0L;
        }
        return new BigDecimal(String.valueOf(v)).longValue();
    }

    private static LocalDateTime parseDateTime(String v) {
        String s = trimToNull(v);
        if (s == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(s.replace(' ', 'T'));
        } catch (Exception e) {
            throw new BusinessException("时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    private static String cut(String v, int max) {
        if (v == null) {
            return null;
        }
        String s = v.trim();
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static String trimToNull(String v) {
        return StringUtils.hasText(v) ? v.trim() : null;
    }

    @Override
    public PageResult<TeleConsultVO> teleListPage(TeleConsultQueryPageDTO dto) {
        Page<TeleConsultVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<TeleConsultVO> records = teleMapper.selectTelePage(page, trimToNull(dto.getKeyword()),
                dto.getConsultType(), dto.getStatus(), dto.getApplyDeptId(),
                dto.getUrgentOnly(), dto.getOpenOnly());
        records.forEach(this::decorateTele);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    // 线上问诊

    @Override
    public TeleConsultVO teleGetDetailById(Long id) {
        return requireTeleVo(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeleConsultVO teleUpsert(TeleConsultUpsertDTO dto) {
        BizTeleConsult entity;
        boolean isNew = dto.getId() == null;
        if (isNew) {
            entity = new BizTeleConsult();
            entity.setConsultNo(nextNo(Constants.TELE_CONSULT_NO_PREFIX, "TELE_CONSULT"));
            entity.setStatus(TeleConsultStatusEnum.PENDING.getCode());
            entity.setIsUrgent(dto.getIsUrgent() == null ? 0 : dto.getIsUrgent());
        } else {
            entity = requireTeleEntity(dto.getId());
            if (!Objects.equals(entity.getStatus(), TeleConsultStatusEnum.PENDING.getCode())) {
                throw new BusinessException("仅「待安排」的会诊单允许修改（当前：" + teleStatusName(entity.getStatus()) + "）");
            }
        }
        BizPatient patient = requirePatient(dto.getPatientId());
        entity.setPatientId(patient.getId());
        entity.setPatientNo(patient.getPatientNo());
        entity.setPatientName(patient.getPatientName());
        entity.setAdmissionId(dto.getAdmissionId());
        entity.setApplyDeptId(dto.getApplyDeptId());
        entity.setApplyDeptName(dto.getApplyDeptId() == null ? null : teleMapper.selectDeptName(dto.getApplyDeptId()));
        entity.setApplyDoctorId(dto.getApplyDoctorId() == null ? UserUtils.getCurrentEmployeeId() : dto.getApplyDoctorId());
        entity.setApplyDoctor(currentName());
        entity.setConsultType(dto.getConsultType());
        entity.setExpertHospital(cut(dto.getExpertHospital(), 128));
        entity.setExpertDept(cut(dto.getExpertDept(), 128));
        entity.setExpertName(cut(dto.getExpertName(), 64));
        entity.setExpertTitle(cut(dto.getExpertTitle(), 32));
        entity.setPurpose(cut(dto.getPurpose(), 500));
        entity.setDiagnosis(cut(dto.getDiagnosis(), 500));
        entity.setFee(dto.getFee());
        entity.setRemark(cut(dto.getRemark(), 512));
        if (isNew) {
            teleMapper.insert(entity);
        } else {
            teleMapper.updateById(entity);
        }
        return requireTeleVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeleConsultVO teleArrange(TeleArrangeDTO dto) {
        BizTeleConsult entity = requireTeleEntity(dto.getId());
        if (!Objects.equals(entity.getStatus(), TeleConsultStatusEnum.PENDING.getCode())) {
            throw new BusinessException("仅「待安排」的会诊单可安排（当前：" + teleStatusName(entity.getStatus()) + "）");
        }
        entity.setStatus(TeleConsultStatusEnum.ARRANGED.getCode());
        entity.setPlanTime(parseDateTime(dto.getPlanTime()));
        entity.setDurationMin(dto.getDurationMin());
        entity.setPlatform(cut(dto.getPlatform(), 64));
        entity.setMeetNo(cut(dto.getMeetNo(), 64));
        if (StringUtils.hasText(dto.getExpertHospital())) {
            entity.setExpertHospital(cut(dto.getExpertHospital(), 128));
        }
        if (StringUtils.hasText(dto.getExpertDept())) {
            entity.setExpertDept(cut(dto.getExpertDept(), 128));
        }
        if (StringUtils.hasText(dto.getExpertName())) {
            entity.setExpertName(cut(dto.getExpertName(), 64));
        }
        if (StringUtils.hasText(dto.getExpertTitle())) {
            entity.setExpertTitle(cut(dto.getExpertTitle(), 32));
        }
        entity.setArrangeBy(currentName());
        entity.setArrangeTime(now());
        teleMapper.updateById(entity);
        return requireTeleVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeleConsultVO teleComplete(TeleActionDTO dto) {
        BizTeleConsult entity = requireTeleEntity(dto.getId());
        if (!Objects.equals(entity.getStatus(), TeleConsultStatusEnum.ARRANGED.getCode())) {
            throw new BusinessException("仅「已安排」的会诊单可出意见完成（当前：" + teleStatusName(entity.getStatus()) + "）");
        }
        String opinion = trimToNull(dto.getContent());
        entity.setStatus(TeleConsultStatusEnum.DONE.getCode());
        entity.setOpinion(cut(opinion, 1000));
        entity.setCompleteBy(currentName());
        entity.setCompleteTime(now());
        teleMapper.updateById(entity);
        return requireTeleVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeleConsultVO teleCancel(TeleActionDTO dto) {
        BizTeleConsult entity = requireTeleEntity(dto.getId());
        Integer st = entity.getStatus();
        if (Objects.equals(st, TeleConsultStatusEnum.DONE.getCode()) || Objects.equals(st, TeleConsultStatusEnum.CANCELED.getCode())) {
            throw new BusinessException("已完成/已取消的会诊单不可再取消");
        }
        String reason = trimToNull(dto.getContent());
        entity.setStatus(TeleConsultStatusEnum.CANCELED.getCode());
        entity.setCancelReason(cut(reason, 500));
        teleMapper.updateById(entity);
        return requireTeleVo(entity.getId());
    }

    @Override
    public boolean teleDeleteById(Long id) {
        BizTeleConsult entity = requireTeleEntity(id);
        if (!Objects.equals(entity.getStatus(), TeleConsultStatusEnum.PENDING.getCode())) {
            throw new BusinessException("仅「待安排」的会诊单可删除");
        }
        return teleMapper.deleteById(id) > 0;
    }

    @Override
    public PageResult<OnlineConsultVO> onlineListPage(OnlineQueryPageDTO dto) {
        Page<OnlineConsultVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<OnlineConsultVO> records = onlineMapper.selectOnlinePage(page, trimToNull(dto.getKeyword()),
                dto.getConsultType(), dto.getStatus(), dto.getDeptId(), dto.getDoctorId(), dto.getWaitingOnly());
        records.forEach(this::decorateOnline);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public OnlineConsultVO onlineGetDetailById(Long id) {
        return requireOnlineVo(id);
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OnlineConsultVO onlineApply(OnlineApplyDTO dto) {
        BizPatient patient = requirePatient(dto.getPatientId());
        BizOnlineConsult entity = new BizOnlineConsult();
        entity.setConsultNo(nextNo(Constants.ONLINE_CONSULT_NO_PREFIX, "ONLINE_CONSULT"));
        entity.setPatientId(patient.getId());
        entity.setPatientNo(patient.getPatientNo());
        entity.setPatientName(patient.getPatientName());
        entity.setDeptId(dto.getDeptId());
        entity.setDeptName(dto.getDeptId() == null ? null : onlineMapper.selectDeptName(dto.getDeptId()));
        entity.setDoctorId(dto.getDoctorId());
        entity.setDoctorName(dto.getDoctorId() == null ? null : currentName());
        entity.setConsultType(dto.getConsultType());
        entity.setChiefComplaint(cut(dto.getChiefComplaint(), 1000));
        entity.setStatus(OnlineConsultStatusEnum.WAITING.getCode());
        entity.setNeedVisit(0);
        entity.setFee(dto.getFee());
        entity.setApplyTime(now());
        entity.setRemark(cut(dto.getRemark(), 512));
        onlineMapper.insert(entity);
        return requireOnlineVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OnlineConsultVO onlineAccept(Long id) {
        BizOnlineConsult entity = requireOnlineEntity(id);
        if (!Objects.equals(entity.getStatus(), OnlineConsultStatusEnum.WAITING.getCode())) {
            throw new BusinessException("仅「待接诊」的问诊单可接诊（当前：" + onlineStatusName(entity.getStatus()) + "）");
        }
        entity.setStatus(OnlineConsultStatusEnum.ACCEPTED.getCode());
        entity.setAcceptBy(currentName());
        entity.setAcceptTime(now());
        if (entity.getDoctorId() == null) {
            entity.setDoctorId(UserUtils.getCurrentEmployeeId());
        }
        if (!StringUtils.hasText(entity.getDoctorName())) {
            entity.setDoctorName(currentName());
        }
        onlineMapper.updateById(entity);
        return requireOnlineVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OnlineConsultVO onlineReply(OnlineReplyDTO dto) {
        BizOnlineConsult entity = requireOnlineEntity(dto.getId());
        if (!Objects.equals(entity.getStatus(), OnlineConsultStatusEnum.ACCEPTED.getCode())) {
            throw new BusinessException("仅「接诊中」的问诊单可回复（当前：" + onlineStatusName(entity.getStatus()) + "）");
        }
        entity.setStatus(OnlineConsultStatusEnum.DONE.getCode());
        entity.setReply(cut(dto.getReply(), 1000));
        entity.setAdvice(cut(dto.getAdvice(), 500));
        entity.setNeedVisit(dto.getNeedVisit() == null ? 0 : dto.getNeedVisit());
        entity.setFinishBy(currentName());
        entity.setFinishTime(now());
        onlineMapper.updateById(entity);
        return requireOnlineVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OnlineConsultVO onlineReject(TeleActionDTO dto) {
        BizOnlineConsult entity = requireOnlineEntity(dto.getId());
        Integer st = entity.getStatus();
        if (Objects.equals(st, OnlineConsultStatusEnum.DONE.getCode()) || Objects.equals(st, OnlineConsultStatusEnum.REJECTED.getCode())) {
            throw new BusinessException("已完成/已退诊的问诊单不可再退诊");
        }
        String reason = trimToNull(dto.getContent());
        entity.setStatus(OnlineConsultStatusEnum.REJECTED.getCode());
        entity.setRejectReason(cut(reason, 500));
        onlineMapper.updateById(entity);
        return requireOnlineVo(entity.getId());
    }

    @Override
    public boolean onlineDeleteById(Long id) {
        BizOnlineConsult entity = requireOnlineEntity(id);
        if (!Objects.equals(entity.getStatus(), OnlineConsultStatusEnum.WAITING.getCode())) {
            throw new BusinessException("仅「待接诊」的问诊单可删除");
        }
        return onlineMapper.deleteById(id) > 0;
    }

    @Override
    public TeleConsultStatVO stat() {
        TeleConsultStatVO vo = new TeleConsultStatVO();
        long tp = 0, ta = 0, td = 0, tc = 0;
        for (Map<String, Object> row : teleMapper.countByStatus()) {
            long c = toLong(row.get("c"));
            switch ((int) toLong(row.get("k"))) {
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
        for (Map<String, Object> row : onlineMapper.countByStatus()) {
            long c = toLong(row.get("c"));
            switch ((int) toLong(row.get("k"))) {
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
        BizPatient patient = patientMapper.selectById(patientId);
        if (patient == null || !Objects.equals(patient.getDelFlag(), 0)) {
            throw new BusinessException("患者不存在或已删除");
        }
        return patient;
    }

    private TeleConsultVO requireTeleVo(Long id) {
        TeleConsultVO vo = teleMapper.selectTeleById(id);
        if (vo == null) {
            throw new BusinessException("远程会诊单不存在或已删除");
        }
        decorateTele(vo);
        return vo;
    }

    private BizTeleConsult requireTeleEntity(Long id) {
        BizTeleConsult entity = teleMapper.selectById(id);
        if (entity == null || !Objects.equals(entity.getDelFlag(), 0)) {
            throw new BusinessException("远程会诊单不存在或已删除");
        }
        return entity;
    }

    private OnlineConsultVO requireOnlineVo(Long id) {
        OnlineConsultVO vo = onlineMapper.selectOnlineById(id);
        if (vo == null) {
            throw new BusinessException("问诊单不存在或已删除");
        }
        decorateOnline(vo);
        return vo;
    }

    private BizOnlineConsult requireOnlineEntity(Long id) {
        BizOnlineConsult entity = onlineMapper.selectById(id);
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
        return prefix + LocalDate.now().format(NO_DATE) + String.format("%04d", sequenceService.next(module));
    }
}
