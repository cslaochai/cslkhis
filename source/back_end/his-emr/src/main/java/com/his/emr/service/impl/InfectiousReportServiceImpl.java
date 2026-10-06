package com.his.emr.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.emr.dto.InfectiousReportDTO;
import com.his.emr.dto.InfectiousReportQueryPageDTO;
import com.his.emr.entity.BizInfectiousReport;
import com.his.emr.entity.SysInfectiousDisease;
import com.his.emr.enums.InfectiousClassEnum;
import com.his.emr.enums.InfectiousReportStatusEnum;
import com.his.emr.mapper.BizInfectiousReportMapper;
import com.his.emr.mapper.SysInfectiousDiseaseMapper;
import com.his.emr.service.InfectiousReportService;
import com.his.emr.vo.InfectiousReportVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 传染病报告卡服务实现
 * <p>
 * 状态机：1 待审核 → 2 已审核(待直报) → 3 已直报；1/2 → 4 已退报（必填原因），
 * 退报卡修改后重新提交回 1（report_count 递增留痕）。3 是终态：直报过的卡只读。
 * 时限快照在填卡时由目录推导（甲类 2h / 乙丙 24h），逾期催报走站内信、按天幂等。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InfectiousReportServiceImpl implements InfectiousReportService {

    private final BizInfectiousReportMapper reportMapper;
    private final SysInfectiousDiseaseMapper diseaseMapper;
    private final RedisSequenceService sequenceService;
    private final com.his.system.service.SysMessageService sysMessageService;

    // 查询

    private static String tr(String s) {
        return s == null ? null : s.trim();
    }

    private static Long toLong(Object o) {
        return o == null ? null : Long.valueOf(String.valueOf(o));
    }

    private static Integer toInteger(Object o) {
        return o == null ? null : Integer.valueOf(String.valueOf(o));
    }

    @Override
    public PageResult<InfectiousReportVO.Row> page(InfectiousReportQueryPageDTO q) {
        LambdaQueryWrapper<BizInfectiousReport> w = new LambdaQueryWrapper<BizInfectiousReport>()
                .eq(q.getReportStatus() != null, BizInfectiousReport::getReportStatus, q.getReportStatus())
                .eq(q.getInfectiousClass() != null, BizInfectiousReport::getInfectiousClass, q.getInfectiousClass())
                .and(StringUtils.hasText(q.getKeyword()), x -> x
                        .like(BizInfectiousReport::getReportNo, tr(q.getKeyword()))
                        .or().like(BizInfectiousReport::getPatientName, tr(q.getKeyword()))
                        .or().like(BizInfectiousReport::getDiseaseName, tr(q.getKeyword())))
                .ge(q.getReportTimeStart() != null, BizInfectiousReport::getReportTime, q.getReportTimeStart())
                .le(q.getReportTimeEnd() != null, BizInfectiousReport::getReportTime, q.getReportTimeEnd())
                .orderByDesc(BizInfectiousReport::getReportTime)
                .orderByDesc(BizInfectiousReport::getId);
        List<BizInfectiousReport> list = reportMapper.selectList(w);
        // 筛选口径里"逾期未报"要在内存里按 deadline 比（状态 1 且已过期），MP 条件构造器表达不了"现在"比较两列
        if (q.getOverdue() != null && q.getOverdue() == 1) {
            list = list.stream().filter(r -> isOverdue(r)).toList();
        }
        int total = list.size();
        int from = Math.min((q.getPageNum() - 1) * q.getPageSize(), total);
        int to = Math.min(from + q.getPageSize(), total);
        List<InfectiousReportVO.Row> rows = list.subList(from, to).stream().map(this::toRow).toList();
        return PageResult.of(total, q.getPageNum(), q.getPageSize(), (total + q.getPageSize() - 1) / q.getPageSize(), rows);
    }

    // 写路径

    @Override
    public InfectiousReportVO.Detail getDetailById(Long id) {
        BizInfectiousReport r = reportMapper.selectById(id);
        if (r == null || r.getDelFlag() != 0) {
            throw new BusinessException("报卡不存在或已删除");
        }
        InfectiousReportVO.Detail d = new InfectiousReportVO.Detail();
        d.setCard(toRow(r));
        d.setDirectPayloadPreview(r.getDirectPayload());
        return d;
    }

    @Override
    public List<InfectiousReportVO.DiseaseSelectListVO> diseaseSelectList(String keyword) {
        LambdaQueryWrapper<SysInfectiousDisease> w = new LambdaQueryWrapper<SysInfectiousDisease>()
                .eq(SysInfectiousDisease::getStatus, 1)
                .eq(SysInfectiousDisease::getDelFlag, 0)
                .and(StringUtils.hasText(keyword), x -> x
                        .like(SysInfectiousDisease::getDiseaseName, tr(keyword))
                        .or().like(SysInfectiousDisease::getDiseaseCode, tr(keyword)))
                .orderByAsc(SysInfectiousDisease::getInfectiousClass)
                .orderByAsc(SysInfectiousDisease::getDiseaseCode)
                .last("LIMIT 50");
        return diseaseMapper.selectList(w).stream().map(d -> {
            InfectiousReportVO.DiseaseSelectListVO r = new InfectiousReportVO.DiseaseSelectListVO();
            r.setId(String.valueOf(d.getId()));
            r.setDiseaseCode(d.getDiseaseCode());
            r.setDiseaseName(d.getDiseaseName());
            r.setInfectiousClass(d.getInfectiousClass());
            r.setInfectiousClassText(classText(d.getInfectiousClass()));
            r.setDeadlineHours(d.getDeadlineHours());
            r.setIcd10(d.getIcd10());
            return r;
        }).toList();
    }

    @Override
    public InfectiousReportVO.Stats stats() {
        List<BizInfectiousReport> all = reportMapper.selectList(
                new LambdaQueryWrapper<BizInfectiousReport>().eq(BizInfectiousReport::getDelFlag, 0));
        InfectiousReportVO.Stats s = new InfectiousReportVO.Stats();
        LocalDateTime todayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        for (BizInfectiousReport r : all) {
            Integer reportStatus = r.getReportStatus();
            if (Objects.equals(InfectiousReportStatusEnum.PENDING.getCode(), reportStatus)) {
                s.setPendingAudit(s.getPendingAudit() + 1);
                if (isOverdue(r)) {
                    s.setOverduePending(s.getOverduePending() + 1);
                }
                if (Objects.equals(InfectiousClassEnum.CLASS_A.getCode(), r.getInfectiousClass())) {
                    s.setClassAPending(s.getClassAPending() + 1);
                }
            } else if (Objects.equals(InfectiousReportStatusEnum.AUDITED.getCode(), reportStatus)) {
                s.setAudited(s.getAudited() + 1);
            } else if (Objects.equals(InfectiousReportStatusEnum.DIRECT.getCode(), reportStatus)) {
                s.setDirectReported(s.getDirectReported() + 1);
            } else if (Objects.equals(InfectiousReportStatusEnum.RETURNED.getCode(), reportStatus)) {
                s.setReturned(s.getReturned() + 1);
            }
            if (r.getReportTime() != null && r.getReportTime().isAfter(todayStart)) {
                s.setTodayNew(s.getTodayNew() + 1);
            }
        }
        return s;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(InfectiousReportDTO.Upsert dto) {
        if (dto.getRegistId() == null && dto.getInpId() == null) {
            throw new BusinessException("门诊就诊与住院记录至少填一项（报卡必须能追到具体就诊）");
        }
        SysInfectiousDisease disease = diseaseMapper.selectById(dto.getDiseaseId());
        if (disease == null || disease.getDelFlag() != 0 || disease.getStatus() != 1) {
            throw new BusinessException("传染病病种不存在或已停用：" + dto.getDiseaseId());
        }
        Map<String, Object> patient = reportMapper.selectPatientSnapshot(dto.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在：" + dto.getPatientId());
        }
        Long operatorId = UserUtils.getCurrentEmployeeId();
        String operatorName = UserUtils.getCurrentEmployeeName();

        if (dto.getId() == null) {
            BizInfectiousReport r = new BizInfectiousReport();
            r.setReportNo(sequenceService.generateInfectiousReportNo());
            fillCard(r, dto, disease, patient, operatorId, operatorName);
            r.setReportCount(1);
            r.setReportStatus(InfectiousReportStatusEnum.PENDING.getCode());
            r.setReportTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            r.setRemark(null);
            reportMapper.insert(r);
            return r.getId();
        }

        BizInfectiousReport exists = reportMapper.selectById(dto.getId());
        if (exists == null || exists.getDelFlag() != 0) {
            throw new BusinessException("报卡不存在或已删除");
        }
        if (exists.getReportStatus() == InfectiousReportStatusEnum.DIRECT.getCode()) {
            throw new BusinessException("已直报的卡是法定留痕凭证，不能修改");
        }
        if (exists.getReportStatus() == InfectiousReportStatusEnum.AUDITED.getCode()) {
            throw new BusinessException("已审核的卡不能直接改，请先退报再修改重报");
        }
        // 1 待审核 = 直接改；4 已退报 = 修改后重报，计数+1 并留重报说明
        boolean resubmit = exists.getReportStatus() == InfectiousReportStatusEnum.RETURNED.getCode();
        if (resubmit && !StringUtils.hasText(dto.getResubmitRemark())) {
            throw new BusinessException("退报重报必须说明修改内容");
        }
        fillCard(exists, dto, disease, patient, exists.getReportBy(), exists.getReportByName());
        exists.setReportStatus(InfectiousReportStatusEnum.PENDING.getCode());
        exists.setReturnReason(null);
        if (resubmit) {
            exists.setReportCount(exists.getReportCount() + 1);
            exists.setRemark("第 " + exists.getReportCount() + " 次重报：" + tr(dto.getResubmitRemark()));
        }
        exists.setReportTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        reportMapper.updateById(exists);
        return exists.getId();
    }

    // 时限催报（发送方：定时 + 手工补跑双路径，按天幂等）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(InfectiousReportDTO.Audit dto) {
        BizInfectiousReport r = requireCard(dto.getId());
        if (r.getReportStatus() != InfectiousReportStatusEnum.PENDING.getCode()) {
            throw new BusinessException(statusText(r.getReportStatus()) + "的卡不能审核（仅待审核可审）");
        }
        String name = StringUtils.hasText(dto.getAuditByName()) ? dto.getAuditByName() : UserUtils.getCurrentEmployeeName();
        r.setReportStatus(InfectiousReportStatusEnum.AUDITED.getCode());
        r.setAuditByName(name);
        r.setAuditTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        r.setAuditOpinion(tr(dto.getOpinion()));
        reportMapper.updateById(r);
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnCard(InfectiousReportDTO.ReturnCard dto) {
        BizInfectiousReport r = requireCard(dto.getId());
        if (r.getReportStatus() == InfectiousReportStatusEnum.DIRECT.getCode()) {
            throw new BusinessException("已直报的卡不能退报（订正走疾控订正流程，不在本页）");
        }
        if (r.getReportStatus() == InfectiousReportStatusEnum.RETURNED.getCode()) {
            throw new BusinessException("已是退报状态，无需重复退报");
        }
        String name = StringUtils.hasText(dto.getAuditByName()) ? dto.getAuditByName() : UserUtils.getCurrentEmployeeName();
        r.setReportStatus(InfectiousReportStatusEnum.RETURNED.getCode());
        r.setReturnReason(tr(dto.getReason()));
        r.setAuditByName(name);
        r.setAuditTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        reportMapper.updateById(r);
    }

    /**
     * 直报：组装标准报文落 payload 并置已直报。
     * 真实对接时本方法是唯一替换点——把"落 payload"换成"http 客户端发疾控 + 回执落库"，接口面与报文结构不变。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String directReport(Long id) {
        BizInfectiousReport r = requireCard(id);
        if (r.getReportStatus() != InfectiousReportStatusEnum.AUDITED.getCode()) {
            throw new BusinessException(statusText(r.getReportStatus()) + "的卡不能直报（须先审核通过）");
        }
        String payload = buildDirectPayload(r);
        r.setReportStatus(InfectiousReportStatusEnum.DIRECT.getCode());
        r.setDirectPayload(payload);
        r.setDirectTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        reportMapper.updateById(r);
        return payload;
    }

    @Override
    public int notifyOverdue() {
        List<BizInfectiousReport> overdue = reportMapper.selectList(
                new LambdaQueryWrapper<BizInfectiousReport>()
                        .eq(BizInfectiousReport::getReportStatus, InfectiousReportStatusEnum.PENDING.getCode())
                        .eq(BizInfectiousReport::getDelFlag, 0)
                        .lt(BizInfectiousReport::getReportDeadline, LocalDateTime.now())
                        .orderByAsc(BizInfectiousReport::getReportDeadline));
        if (overdue.isEmpty()) {
            return 0;
        }
        LocalDateTime todayStart = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        int sent = 0;
        for (BizInfectiousReport r : overdue) {
            try {
                // 每张卡每天最多催一条：notify_time 在今天 → 跳过（定时 + 手工补跑共用）
                if (r.getNotifyTime() != null && r.getNotifyTime().isAfter(todayStart)) {
                    continue;
                }
                long lateHours = ChronoUnit.HOURS.between(r.getReportDeadline(), LocalDateTime.now());
                String content = String.format(
                        "传染病报卡 %s（患者 %s，%s %s，%s）已于 %s 超过报卡时限 %d 小时仍未审核上报，请尽快处理。",
                        r.getReportNo(), r.getPatientName(), classText(r.getInfectiousClass()),
                        r.getDiseaseName(), r.getReportByName(), r.getReportDeadline(), lateHours);
                String payload = JSONUtil.toJsonStr(new LinkedHashMap<String, Object>() {{
                    put("reportNo", r.getReportNo());
                    put("diseaseName", r.getDiseaseName());
                    put("patientName", r.getPatientName());
                    put("deadline", String.valueOf(r.getReportDeadline()));
                    put("lateHours", lateHours);
                }});
                boolean ok = sysMessageService.sendSystemMessage(r.getReportBy(), r.getReportByName(),
                        "传染病报卡超时：" + r.getReportNo(), content,
                        com.his.system.enums.BizTypeEnum.INFECTIOUS_REPORT.getType(), r.getId(), "warning", payload, null);
                if (ok) {
                    r.setNotifyTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
                    reportMapper.updateById(r);
                    sent++;
                }
            } catch (Exception ex) {
                log.warn("[传染病报卡] 催报发送失败 reportId={} reportBy={}", r.getId(), r.getReportBy(), ex);
            }
        }
        log.info("[传染病报卡] 时限催报扫描完成：超时未报 {} 张，发送催报 {} 条", overdue.size(), sent);
        return sent;
    }

    private void fillCard(BizInfectiousReport r, InfectiousReportDTO.Upsert dto, SysInfectiousDisease disease,
                          Map<String, Object> patient, Long reportBy, String reportByName) {
        r.setPatientId(toLong(patient.get("patientId")));
        r.setPatientNo((String) patient.get("patientNo"));
        r.setPatientName((String) patient.get("patientName"));
        r.setGender(toInteger(patient.get("gender")));
        r.setAge(toInteger(patient.get("age")));
        r.setRegistId(dto.getRegistId());
        r.setInpId(dto.getInpId());
        // 发现科室：挂号单科室兜底，允许填卡人指定（报卡的发现科室是填卡人认定的事实）
        if (dto.getRegistId() != null) {
            Map<String, Object> dept = reportMapper.selectRegistDept(dto.getRegistId());
            if (dept != null) {
                r.setVisitDeptId(toLong(dept.get("deptId")));
                r.setVisitDeptName((String) dept.get("deptName"));
            }
        }
        if (StringUtils.hasText(dto.getVisitDeptName())) {
            r.setVisitDeptName(tr(dto.getVisitDeptName()));
        }
        r.setDiseaseId(disease.getId());
        r.setDiseaseCode(disease.getDiseaseCode());
        r.setDiseaseName(disease.getDiseaseName());
        r.setInfectiousClass(disease.getInfectiousClass());
        r.setIcd10(disease.getIcd10());
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        r.setReportDeadline(now.plusHours(disease.getDeadlineHours()));
        r.setClinicalDesc(tr(dto.getClinicalDesc()));
        r.setReportBy(reportBy);
        r.setReportByName(reportByName);
    }

    private String buildDirectPayload(BizInfectiousReport r) {
        Map<String, Object> m = new HashMap<>();
        m.put("cardNo", r.getReportNo());
        m.put("orgCode", "HN-LK-YY");
        m.put("orgName", "长沙麓康医院");
        m.put("patient", Map.of(
                "no", r.getPatientNo() == null ? "" : r.getPatientNo(),
                "name", r.getPatientName() == null ? "" : r.getPatientName(),
                "gender", r.getGender() == null ? 9 : r.getGender(),
                "age", r.getAge() == null ? 0 : r.getAge()));
        m.put("disease", Map.of(
                "code", r.getDiseaseCode(),
                "name", r.getDiseaseName(),
                "class", r.getInfectiousClass(),
                "icd10", r.getIcd10() == null ? "" : r.getIcd10()));
        m.put("visit", Map.of(
                "registId", r.getRegistId() == null ? "" : String.valueOf(r.getRegistId()),
                "inpId", r.getInpId() == null ? "" : String.valueOf(r.getInpId()),
                "deptName", r.getVisitDeptName() == null ? "" : r.getVisitDeptName()));
        m.put("clinicalDesc", r.getClinicalDesc() == null ? "" : r.getClinicalDesc());
        m.put("reportBy", r.getReportByName());
        m.put("reportTime", String.valueOf(r.getReportTime()));
        m.put("auditBy", r.getAuditByName() == null ? "" : r.getAuditByName());
        m.put("auditTime", String.valueOf(r.getAuditTime()));
        m.put("reportCount", r.getReportCount());
        // 报文即契约：真实对接时按疾控接口规范替换此 map 结构，外层流程不变
        return JSONUtil.toJsonStr(m);
    }

    private BizInfectiousReport requireCard(Long id) {
        BizInfectiousReport r = reportMapper.selectById(id);
        if (r == null || r.getDelFlag() != 0) {
            throw new BusinessException("报卡不存在或已删除");
        }
        return r;
    }

    private boolean isOverdue(BizInfectiousReport r) {
        return r.getReportStatus() == InfectiousReportStatusEnum.PENDING.getCode() && r.getReportDeadline() != null
                && r.getReportDeadline().isBefore(LocalDateTime.now());
    }

    private InfectiousReportVO.Row toRow(BizInfectiousReport r) {
        InfectiousReportVO.Row vo = new InfectiousReportVO.Row();
        vo.setId(String.valueOf(r.getId()));
        vo.setReportNo(r.getReportNo());
        vo.setPatientId(String.valueOf(r.getPatientId()));
        vo.setPatientNo(r.getPatientNo());
        vo.setPatientName(r.getPatientName());
        vo.setGender(r.getGender());
        vo.setAge(r.getAge());
        vo.setRegistId(r.getRegistId() == null ? null : String.valueOf(r.getRegistId()));
        vo.setInpId(r.getInpId() == null ? null : String.valueOf(r.getInpId()));
        vo.setVisitDeptId(r.getVisitDeptId() == null ? null : String.valueOf(r.getVisitDeptId()));
        vo.setVisitDeptName(r.getVisitDeptName());
        vo.setDiseaseId(String.valueOf(r.getDiseaseId()));
        vo.setDiseaseCode(r.getDiseaseCode());
        vo.setDiseaseName(r.getDiseaseName());
        vo.setInfectiousClass(r.getInfectiousClass());
        vo.setInfectiousClassText(classText(r.getInfectiousClass()));
        vo.setIcd10(r.getIcd10());
        vo.setReportDeadline(r.getReportDeadline());
        vo.setClinicalDesc(r.getClinicalDesc());
        vo.setReportStatus(r.getReportStatus());
        vo.setReportStatusText(statusText(r.getReportStatus()));
        vo.setReportCount(r.getReportCount());
        vo.setReportByName(r.getReportByName());
        vo.setReportTime(r.getReportTime());
        vo.setAuditByName(r.getAuditByName());
        vo.setAuditTime(r.getAuditTime());
        vo.setAuditOpinion(r.getAuditOpinion());
        vo.setReturnReason(r.getReturnReason());
        vo.setNotifyTime(r.getNotifyTime());
        vo.setDirectTime(r.getDirectTime());
        vo.setDirectPayload(r.getDirectPayload());
        vo.setCreateTime(r.getCreateTime() == null ? null : String.valueOf(r.getCreateTime()));
        vo.setRemark(r.getRemark());
        if (isOverdue(r)) {
            vo.setOverdue(true);
            vo.setRemainHours(ChronoUnit.HOURS.between(LocalDateTime.now(), r.getReportDeadline()));
        } else {
            vo.setOverdue(false);
            vo.setRemainHours(r.getReportDeadline() == null ? null
                    : ChronoUnit.HOURS.between(LocalDateTime.now(), r.getReportDeadline()));
        }
        return vo;
    }

    private String classText(Integer c) {
        return InfectiousClassEnum.getText(c);
    }

    private String statusText(Integer s) {
        // 文案差异：本模块界面口径把「已审核待直报」叫「已审核」，其余沿用枚举 label，兜底走枚举 getText
        if (Objects.equals(InfectiousReportStatusEnum.AUDITED.getCode(), s)) {
            return "已审核";
        }
        return InfectiousReportStatusEnum.getText(s);
    }
}
