package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.emr.dto.InfectionMonitorDTO;
import com.his.emr.entity.BizHandHygieneObs;
import com.his.emr.entity.BizInfectionCase;
import com.his.emr.entity.BizInfectionMonitor;
import com.his.emr.entity.BizInfectionMonitorDaily;
import com.his.emr.enums.DeviceMonitorStatusEnum;
import com.his.emr.enums.InfectionCaseStatusEnum;
import com.his.emr.enums.InfectionMonitorTypeEnum;
import com.his.emr.enums.InfectionSourceEnum;
import com.his.emr.mapper.BizHandHygieneObsMapper;
import com.his.emr.mapper.BizInfectionCaseMapper;
import com.his.emr.mapper.BizInfectionMonitorDailyMapper;
import com.his.emr.mapper.BizInfectionMonitorMapper;
import com.his.emr.service.InfectionMonitorService;
import com.his.emr.vo.InfectionMonitorVO;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 院感监测服务实现（L10）
 * <p>
 * 三块口径：
 * 1. 病例报告卡：1 待核实 → 2 已确认 / 3 已排除（核实是感控办的结论，只允许核实一次，
 * 结论错了建新卡订正——质控留痕不覆盖）。漏报调查发现的应报未报走补报建卡（leakFlag=1），
 * 与正常报卡同链路，仅统计口径区分。
 * 2. 目标性监测：感染确认（infectionFlag=1）不改在管状态（status），两条线独立——
 * 感染后继续在管到拔管，导管日统计才完整。每日打卡只增禁删禁改（质控留痕），
 * 导管日 = 有效打卡数，感染率（‰）= 感染例次 / 导管日 × 1000。
 * 3. 手卫生观察：只增不改。依从率（%）= SUM(执行) / SUM(时机) × 100，先聚合再算比率，
 * 不做单条比率的二次平均（平均的比率是错的）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InfectionMonitorServiceImpl implements InfectionMonitorService {

    private final BizInfectionCaseMapper caseMapper;
    private final BizInfectionMonitorMapper monitorMapper;
    private final BizInfectionMonitorDailyMapper dailyMapper;
    private final BizHandHygieneObsMapper handObsMapper;
    private final RedisSequenceService sequenceService;

    // 病例报告卡

    /**
     * 比率兜底：分母 0 给 0，不抛异常不猜 NaN
     */
    private static double rate(long num, long den) {
        return den <= 0 ? 0.0 : (double) num / den;
    }

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
    public InfectionMonitorVO.CaseStats caseStats() {
        List<BizInfectionCase> all = caseMapper.selectList(
                new LambdaQueryWrapper<BizInfectionCase>().orderByDesc(BizInfectionCase::getId));
        InfectionMonitorVO.CaseStats s = new InfectionMonitorVO.CaseStats();
        LocalDate today = LocalDate.now();
        for (BizInfectionCase c : all) {
            Integer caseStatus = c.getCaseStatus();
            if (Objects.equals(InfectionCaseStatusEnum.PENDING.getCode(), caseStatus)) {
                s.setPendingAudit(s.getPendingAudit() + 1);
            } else if (Objects.equals(InfectionCaseStatusEnum.CONFIRMED.getCode(), caseStatus)) {
                s.setConfirmed(s.getConfirmed() + 1);
                if (Objects.equals(InfectionSourceEnum.HOSPITAL.getCode(), c.getCaseSource())) {
                    s.setHospitalInfection(s.getHospitalInfection() + 1);
                }
            } else if (Objects.equals(InfectionCaseStatusEnum.EXCLUDED.getCode(), caseStatus)) {
                s.setExcluded(s.getExcluded() + 1);
            }
            if (Objects.equals(YesOrNoEnum.YES.getCode(), c.getLeakFlag())) {
                s.setLeakResubmit(s.getLeakResubmit() + 1);
            }
            if (today.equals(c.getInfectDate())) {
                s.setTodayNew(s.getTodayNew() + 1);
            }
        }
        return s;
    }

    @Override
    public PageResult<InfectionMonitorVO.CaseRow> casePage(InfectionMonitorDTO.CaseQueryPage q) {
        LambdaQueryWrapper<BizInfectionCase> w = new LambdaQueryWrapper<BizInfectionCase>()
                .eq(q.getCaseStatus() != null, BizInfectionCase::getCaseStatus, q.getCaseStatus())
                .eq(q.getCaseSource() != null, BizInfectionCase::getCaseSource, q.getCaseSource())
                .eq(q.getLeakFlag() != null, BizInfectionCase::getLeakFlag, q.getLeakFlag())
                .and(StringUtils.hasText(q.getKeyword()), x -> x
                        .like(BizInfectionCase::getCaseNo, tr(q.getKeyword()))
                        .or().like(BizInfectionCase::getPatientName, tr(q.getKeyword()))
                        .or().like(BizInfectionCase::getInfectionDiag, tr(q.getKeyword())))
                .ge(q.getReportTimeStart() != null, BizInfectionCase::getReportTime, q.getReportTimeStart())
                .le(q.getReportTimeEnd() != null, BizInfectionCase::getReportTime, q.getReportTimeEnd())
                .orderByDesc(BizInfectionCase::getReportTime)
                .orderByDesc(BizInfectionCase::getId);
        List<BizInfectionCase> list = caseMapper.selectList(w);
        return pageOf(list, q.getPageNo(), q.getPageSize(), this::toCaseRow);
    }

    @Override
    public InfectionMonitorVO.CaseRow caseGetDetailById(Long id) {
        return toCaseRow(requireCase(id));
    }

    // 目标性监测

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long caseUpsert(InfectionMonitorDTO.CaseUpsert dto) {
        // 就诊锚点：门诊必须给 registId，住院必须给 inpId，二选一是硬约束
        if (dto.getVisitType() == 1 && dto.getRegistId() == null) {
            throw new BusinessException("门诊病例必须关联门诊就诊（registId）");
        }
        if (dto.getVisitType() == 2 && dto.getInpId() == null) {
            throw new BusinessException("住院病例必须关联住院记录（inpId）");
        }
        Map<String, Object> patient = caseMapper.selectPatientSnapshot(dto.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在：" + dto.getPatientId());
        }
        Long operatorId = UserUtils.getCurrentEmployeeId();
        String operatorName = UserUtils.getCurrentEmployeeName();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        if (dto.getId() == null) {
            BizInfectionCase c = new BizInfectionCase();
            c.setCaseNo(sequenceService.generateInfectionCaseNo());
            fillCase(c, dto, patient, operatorId, operatorName);
            c.setCaseStatus(InfectionCaseStatusEnum.PENDING.getCode());
            c.setLeakFlag(Integer.valueOf(1).equals(dto.getLeakFlag()) ? 1 : 0);
            c.setReportTime(now);
            caseMapper.insert(c);
            return c.getId();
        }

        BizInfectionCase exists = requireCase(dto.getId());
        if (exists.getCaseStatus() != InfectionCaseStatusEnum.PENDING.getCode()) {
            throw new BusinessException(statusText(exists.getCaseStatus()) + "的病例不能修改（核实结论错了请建新卡订正，留痕不覆盖）");
        }
        fillCase(exists, dto, patient, exists.getReportBy(), exists.getReportName());
        if (Integer.valueOf(1).equals(dto.getLeakFlag())) {
            exists.setLeakFlag(YesOrNoEnum.YES.getCode());
        }
        exists.setReportTime(now);
        caseMapper.updateById(exists);
        return exists.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void caseAudit(InfectionMonitorDTO.CaseAudit dto) {
        BizInfectionCase c = requireCase(dto.getId());
        if (c.getCaseStatus() != InfectionCaseStatusEnum.PENDING.getCode()) {
            throw new BusinessException(statusText(c.getCaseStatus()) + "的病例不能核实（仅待核实可审）");
        }
        c.setCaseStatus(dto.getAuditResult());
        c.setAuditName(UserUtils.getCurrentEmployeeName());
        c.setAuditTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        c.setAuditRemark(tr(dto.getAuditRemark()));
        caseMapper.updateById(c);
    }

    private void fillCase(BizInfectionCase c, InfectionMonitorDTO.CaseUpsert dto,
                          Map<String, Object> patient, Long reportBy, String reportName) {
        c.setPatientId(toLong(patient.get("patientId")));
        c.setPatientNo((String) patient.get("patientNo"));
        c.setPatientName((String) patient.get("patientName"));
        c.setGender(toInteger(patient.get("gender")));
        c.setAge(toInteger(patient.get("age")));
        c.setVisitType(dto.getVisitType());
        c.setRegistId(dto.getRegistId());
        c.setInpId(dto.getInpId());
        // 发现科室：就诊锚点兜底，允许上报人改口径（上报人认定的事实优先）
        Map<String, Object> dept = dto.getVisitType() == 1
                ? caseMapper.selectRegistDept(dto.getRegistId())
                : caseMapper.selectInpDept(dto.getInpId());
        if (dept != null) {
            c.setDeptId(toLong(dept.get("deptId")));
            c.setDeptName((String) dept.get("deptName"));
        }
        c.setCaseSource(dto.getCaseSource());
        c.setInfectionSite(tr(dto.getInfectionSite()));
        c.setInfectionDiag(tr(dto.getInfectionDiag()));
        c.setPathogen(tr(dto.getPathogen()));
        c.setSpecimen(tr(dto.getSpecimen()));
        c.setInfectDate(dto.getInfectDate());
        c.setReportBy(reportBy);
        c.setReportName(reportName);
        c.setRemark(tr(dto.getRemark()));
    }

    private BizInfectionCase requireCase(Long id) {
        BizInfectionCase c = caseMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("院感病例不存在或已删除");
        }
        return c;
    }

    @Override
    public PageResult<InfectionMonitorVO.MonitorRow> monitorPage(InfectionMonitorDTO.MonitorQueryPage q) {
        LambdaQueryWrapper<BizInfectionMonitor> w = new LambdaQueryWrapper<BizInfectionMonitor>()
                .eq(q.getMonitorType() != null, BizInfectionMonitor::getMonitorType, q.getMonitorType())
                .eq(q.getStatus() != null, BizInfectionMonitor::getStatus, q.getStatus())
                .eq(q.getInfectionFlag() != null, BizInfectionMonitor::getInfectionFlag, q.getInfectionFlag())
                .and(StringUtils.hasText(q.getKeyword()), x -> x
                        .like(BizInfectionMonitor::getMonitorNo, tr(q.getKeyword()))
                        .or().like(BizInfectionMonitor::getPatientName, tr(q.getKeyword())))
                .orderByDesc(BizInfectionMonitor::getInsertDate)
                .orderByDesc(BizInfectionMonitor::getId);
        List<BizInfectionMonitor> list = monitorMapper.selectList(w);
        return pageOf(list, q.getPageNo(), q.getPageSize(), this::toMonitorRow);
    }

    @Override
    public InfectionMonitorVO.MonitorRow monitorGetDetailById(Long id) {
        return toMonitorRow(requireMonitor(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long monitorAdd(InfectionMonitorDTO.MonitorAdd dto) {
        Map<String, Object> patient = caseMapper.selectPatientSnapshot(dto.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在：" + dto.getPatientId());
        }
        if (dto.getInsertDate().isAfter(LocalDate.now())) {
            throw new BusinessException("置入日期不能是未来");
        }
        BizInfectionMonitor m = new BizInfectionMonitor();
        m.setMonitorNo(sequenceService.generateInfectionMonitorNo());
        m.setPatientId(dto.getPatientId());
        m.setPatientNo((String) patient.get("patientNo"));
        m.setPatientName((String) patient.get("patientName"));
        m.setMonitorType(dto.getMonitorType());
        Map<String, Object> dept = monitorMapper.selectInpDept(dto.getInpId());
        if (dept != null) {
            m.setDeptId(toLong(dept.get("deptId")));
            m.setDeptName((String) dept.get("deptName"));
        }
        m.setInsertDate(dto.getInsertDate());
        m.setStatus(DeviceMonitorStatusEnum.IN_USE.getCode());
        m.setInfectionFlag(YesOrNoEnum.NO.getCode());
        m.setRemark(tr(dto.getRemark()));
        monitorMapper.insert(m);
        return m.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void monitorPunchDaily(InfectionMonitorDTO.PunchDaily dto) {
        BizInfectionMonitor m = requireMonitor(dto.getMonitorId());
        if (m.getStatus() == DeviceMonitorStatusEnum.REMOVED.getCode()) {
            throw new BusinessException("已拔管的监测不再打卡（导管日止于拔除日）");
        }
        LocalDate date = dto.getMonitorDate() == null ? LocalDate.now() : dto.getMonitorDate();
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessException("打卡日期不能是未来");
        }
        if (date.isBefore(m.getInsertDate())) {
            throw new BusinessException("打卡日期不能早于置入日期 " + m.getInsertDate());
        }
        // 打卡只增禁删：查重必须含软删行（无唯一键，防重全靠这一查）
        if (dailyMapper.countByMonitorAndDate(m.getId(), date.toString()) > 0) {
            throw new BusinessException(date + " 已打卡，勿重复记录");
        }
        BizInfectionMonitorDaily d = new BizInfectionMonitorDaily();
        d.setMonitorId(m.getId());
        d.setMonitorDate(date);
        d.setRecorderId(UserUtils.getCurrentEmployeeId());
        d.setRecorderName(UserUtils.getCurrentEmployeeName());
        d.setRecordTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        d.setRemark(tr(dto.getRemark()));
        dailyMapper.insert(d);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void monitorRemove(InfectionMonitorDTO.MonitorRemove dto) {
        BizInfectionMonitor m = requireMonitor(dto.getMonitorId());
        if (m.getStatus() == DeviceMonitorStatusEnum.REMOVED.getCode()) {
            throw new BusinessException("该监测已拔管，无需重复操作");
        }
        if (dto.getRemoveDate().isBefore(m.getInsertDate())) {
            throw new BusinessException("拔除日期不能早于置入日期 " + m.getInsertDate());
        }
        if (dto.getRemoveDate().isAfter(LocalDate.now())) {
            throw new BusinessException("拔除日期不能是未来");
        }
        m.setStatus(DeviceMonitorStatusEnum.REMOVED.getCode());
        m.setRemoveDate(dto.getRemoveDate());
        if (StringUtils.hasText(dto.getRemark())) {
            m.setRemark(tr(dto.getRemark()));
        }
        monitorMapper.updateById(m);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void monitorConfirmInfection(InfectionMonitorDTO.ConfirmInfection dto) {
        BizInfectionMonitor m = requireMonitor(dto.getMonitorId());
        if (Integer.valueOf(1).equals(m.getInfectionFlag())) {
            throw new BusinessException("该监测已确认感染，订正请先与感控办核对（留痕不覆盖）");
        }
        if (dto.getInfectionDate().isBefore(m.getInsertDate())) {
            throw new BusinessException("感染日期不能早于置入日期 " + m.getInsertDate());
        }
        if (m.getRemoveDate() != null && dto.getInfectionDate().isAfter(m.getRemoveDate())) {
            throw new BusinessException("感染日期不能晚于拔除日期 " + m.getRemoveDate());
        }
        m.setInfectionFlag(YesOrNoEnum.YES.getCode());
        m.setInfectionDate(dto.getInfectionDate());
        m.setInfectionSite(tr(dto.getInfectionSite()));
        m.setInfectionDiag(tr(dto.getInfectionDiag()));
        monitorMapper.updateById(m);
    }

    // 手卫生依从性

    @Override
    public InfectionMonitorVO.MonitorStats monitorStats() {
        List<BizInfectionMonitor> all = monitorMapper.selectList(
                new LambdaQueryWrapper<BizInfectionMonitor>().orderByDesc(BizInfectionMonitor::getId));
        InfectionMonitorVO.MonitorStats s = new InfectionMonitorVO.MonitorStats();
        Map<Integer, long[]> byType = new HashMap<>();
        for (BizInfectionMonitor m : all) {
            if (m.getStatus() == DeviceMonitorStatusEnum.IN_USE.getCode()) {
                s.setInCatheter(s.getInCatheter() + 1);
            } else {
                s.setRemoved(s.getRemoved() + 1);
            }
            int days = dailyMapper.countCatheterDays(m.getId());
            s.setCatheterDays(s.getCatheterDays() + days);
            if (Integer.valueOf(1).equals(m.getInfectionFlag())) {
                s.setInfectionConfirmed(s.getInfectionConfirmed() + 1);
            }
            long[] g = byType.computeIfAbsent(m.getMonitorType(), k -> new long[3]);
            g[0]++;
            g[1] += days;
            if (Integer.valueOf(1).equals(m.getInfectionFlag())) {
                g[2]++;
            }
        }
        s.setInfectionRate(rate(s.getInfectionConfirmed(), s.getCatheterDays()) * 1000);
        List<InfectionMonitorVO.TypeGroup> groups = new ArrayList<>();
        for (Map.Entry<Integer, long[]> e : byType.entrySet()) {
            InfectionMonitorVO.TypeGroup t = new InfectionMonitorVO.TypeGroup();
            t.setMonitorType(e.getKey());
            t.setMonitorTypeText(monitorTypeText(e.getKey()));
            t.setRegistered(e.getValue()[0]);
            t.setCatheterDays(e.getValue()[1]);
            t.setInfectionCases(e.getValue()[2]);
            t.setInfectionRate(rate(e.getValue()[2], e.getValue()[1]) * 1000);
            groups.add(t);
        }
        groups.sort((a, b) -> Integer.compare(a.getMonitorType(), b.getMonitorType()));
        s.setByType(groups);
        return s;
    }

    private BizInfectionMonitor requireMonitor(Long id) {
        BizInfectionMonitor m = monitorMapper.selectById(id);
        if (m == null) {
            throw new BusinessException("监测登记不存在或已删除");
        }
        return m;
    }

    @Override
    public java.util.List<InfectionMonitorVO.DailyRow> monitorDailyList(Long monitorId) {
        requireMonitor(monitorId);
        return dailyMapper.selectList(new LambdaQueryWrapper<BizInfectionMonitorDaily>()
                        .eq(BizInfectionMonitorDaily::getMonitorId, monitorId)
                        .orderByDesc(BizInfectionMonitorDaily::getMonitorDate)
                        .orderByDesc(BizInfectionMonitorDaily::getId))
                .stream().map(d -> {
                    InfectionMonitorVO.DailyRow vo = new InfectionMonitorVO.DailyRow();
                    vo.setId(d.getId());
                    vo.setMonitorId(d.getMonitorId());
                    vo.setMonitorDate(d.getMonitorDate());
                    vo.setRecorderName(d.getRecorderName());
                    vo.setRecordTime(d.getRecordTime());
                    vo.setRemark(d.getRemark());
                    return vo;
                }).toList();
    }

    private InfectionMonitorVO.MonitorRow toMonitorRow(BizInfectionMonitor m) {
        InfectionMonitorVO.MonitorRow vo = new InfectionMonitorVO.MonitorRow();
        vo.setId(m.getId());
        vo.setMonitorNo(m.getMonitorNo());
        vo.setPatientId(m.getPatientId());
        vo.setPatientNo(m.getPatientNo());
        vo.setPatientName(m.getPatientName());
        vo.setMonitorType(m.getMonitorType());
        vo.setMonitorTypeText(monitorTypeText(m.getMonitorType()));
        vo.setDeptId(m.getDeptId());
        vo.setDeptName(m.getDeptName());
        vo.setInsertDate(m.getInsertDate());
        vo.setRemoveDate(m.getRemoveDate());
        vo.setStatus(m.getStatus());
        vo.setStatusText(m.getStatus() == DeviceMonitorStatusEnum.REMOVED.getCode() ? "已拔管" : "在管");
        vo.setInfectionFlag(m.getInfectionFlag());
        vo.setInfectionDate(m.getInfectionDate());
        vo.setInfectionSite(m.getInfectionSite());
        vo.setInfectionSiteText(infectionSiteText(m.getInfectionSite()));
        vo.setInfectionDiag(m.getInfectionDiag());
        vo.setCatheterDays(dailyMapper.countCatheterDays(m.getId()));
        vo.setRemark(m.getRemark());
        return vo;
    }

    @Override
    public PageResult<InfectionMonitorVO.HandObsRow> handObsPage(InfectionMonitorDTO.HandObsQueryPage q) {
        LambdaQueryWrapper<BizHandHygieneObs> w = new LambdaQueryWrapper<BizHandHygieneObs>()
                .eq(q.getDeptId() != null, BizHandHygieneObs::getDeptId, q.getDeptId())
                .eq(q.getObsObject() != null, BizHandHygieneObs::getObsObject, q.getObsObject())
                .ge(q.getObsDateStart() != null, BizHandHygieneObs::getObsDate, q.getObsDateStart())
                .le(q.getObsDateEnd() != null, BizHandHygieneObs::getObsDate, q.getObsDateEnd())
                .orderByDesc(BizHandHygieneObs::getObsDate)
                .orderByDesc(BizHandHygieneObs::getId);
        List<BizHandHygieneObs> list = handObsMapper.selectList(w);
        return pageOf(list, q.getPageNo(), q.getPageSize(), this::toHandObsRow);
    }

    // 内部工具

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long handObsAdd(InfectionMonitorDTO.HandObsAdd dto) {
        if (dto.getComplyCount() > dto.getOpportunityCount()) {
            throw new BusinessException("执行数不能大于时机数（依从率不可能超过 100%）");
        }
        if (dto.getObsDate().isAfter(LocalDate.now())) {
            throw new BusinessException("观察日期不能是未来");
        }
        Map<String, Object> dept = selectDeptName(dto.getDeptId());
        if (dept == null) {
            throw new BusinessException("科室不存在：" + dto.getDeptId());
        }
        BizHandHygieneObs o = new BizHandHygieneObs();
        o.setObsDate(dto.getObsDate());
        o.setDeptId(dto.getDeptId());
        o.setDeptName((String) dept.get("deptName"));
        o.setObsObject(dto.getObsObject());
        o.setOpportunityCount(dto.getOpportunityCount());
        o.setComplyCount(dto.getComplyCount());
        o.setObserverId(UserUtils.getCurrentEmployeeId());
        o.setObserverName(UserUtils.getCurrentEmployeeName());
        o.setObsTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        o.setRemark(tr(dto.getRemark()));
        handObsMapper.insert(o);
        return o.getId();
    }

    @Override
    public InfectionMonitorVO.HandObsRow handObsGetDetailById(Long id) {
        BizHandHygieneObs o = handObsMapper.selectById(id);
        if (o == null) {
            throw new BusinessException("观察记录不存在或已删除");
        }
        return toHandObsRow(o);
    }

    @Override
    public InfectionMonitorVO.HandObsStats handObsStats(InfectionMonitorDTO.HandObsStatsQuery q) {
        LocalDate end = q.getObsDateEnd() == null ? LocalDate.now() : q.getObsDateEnd();
        LocalDate start = q.getObsDateStart() == null ? end.minusDays(30) : q.getObsDateStart();
        List<BizHandHygieneObs> list = handObsMapper.selectList(
                new LambdaQueryWrapper<BizHandHygieneObs>()
                        .ge(BizHandHygieneObs::getObsDate, start)
                        .le(BizHandHygieneObs::getObsDate, end)
                        .orderByDesc(BizHandHygieneObs::getObsDate)
                        .orderByDesc(BizHandHygieneObs::getId));
        InfectionMonitorVO.HandObsStats s = new InfectionMonitorVO.HandObsStats();
        Map<Integer, long[]> byObject = new HashMap<>();
        for (BizHandHygieneObs o : list) {
            s.setTotalOpportunity(s.getTotalOpportunity() + o.getOpportunityCount());
            s.setTotalComply(s.getTotalComply() + o.getComplyCount());
            s.setRecordCount(s.getRecordCount() + 1);
            long[] g = byObject.computeIfAbsent(o.getObsObject(), k -> new long[2]);
            g[0] += o.getOpportunityCount();
            g[1] += o.getComplyCount();
        }
        s.setComplyRate(rate(s.getTotalComply(), s.getTotalOpportunity()) * 100);
        List<InfectionMonitorVO.ObjectGroup> groups = new ArrayList<>();
        for (Map.Entry<Integer, long[]> e : byObject.entrySet()) {
            InfectionMonitorVO.ObjectGroup g = new InfectionMonitorVO.ObjectGroup();
            g.setObsObject(e.getKey());
            g.setObsObjectText(obsObjectText(e.getKey()));
            g.setOpportunity(e.getValue()[0]);
            g.setComply(e.getValue()[1]);
            g.setComplyRate(rate(e.getValue()[1], e.getValue()[0]) * 100);
            groups.add(g);
        }
        groups.sort((a, b) -> Integer.compare(a.getObsObject(), b.getObsObject()));
        s.setByObject(groups);
        return s;
    }

    private Map<String, Object> selectDeptName(Long deptId) {
        return caseMapper.selectDeptName(deptId);
    }

    private <T, R> PageResult<R> pageOf(List<T> list, Long pageNo, Long pageSize, RowMapper<T, R> mapper) {
        int total = list.size();
        int ps = pageSize == null || pageSize < 1 ? 10 : pageSize.intValue();
        int pn = pageNo == null || pageNo < 1 ? 1 : pageNo.intValue();
        int from = Math.min((pn - 1) * ps, total);
        int to = Math.min(from + ps, total);
        List<R> rows = list.subList(from, to).stream().map(mapper::map).toList();
        return PageResult.of(total, pn, ps, (total + ps - 1) / ps, rows);
    }

    private String statusText(Integer s) {
        if (s == null) return "未知(0)";
        String label = InfectionCaseStatusEnum.labelOf(s);
        return label == null ? "未知(" + s + ")" : label;
    }

    private String monitorTypeText(Integer t) {
        if (t == null) return "未知(0)";
        String label = InfectionMonitorTypeEnum.labelOf(t);
        return label == null ? "未知(" + t + ")" : label;
    }

    private String infectionSiteText(String site) {
        if (!StringUtils.hasText(site)) return null;
        return switch (site) {
            case "1" -> "下呼吸道";
            case "2" -> "泌尿道";
            case "3" -> "胃肠道";
            case "4" -> "手术切口";
            case "5" -> "血流感染";
            case "6" -> "皮肤软组织";
            case "7" -> "腹腔内";
            case "9" -> "其他";
            default -> "未知(" + site + ")";
        };
    }

    private String obsObjectText(Integer o) {
        if (o == null) return "未知(0)";
        return switch (o) {
            case 1 -> "医生";
            case 2 -> "护士";
            case 3 -> "工勤/其他";
            default -> "未知(" + o + ")";
        };
    }

    private InfectionMonitorVO.CaseRow toCaseRow(BizInfectionCase c) {
        InfectionMonitorVO.CaseRow vo = new InfectionMonitorVO.CaseRow();
        vo.setId(c.getId());
        vo.setCaseNo(c.getCaseNo());
        vo.setPatientId(c.getPatientId());
        vo.setPatientNo(c.getPatientNo());
        vo.setPatientName(c.getPatientName());
        vo.setGender(c.getGender());
        vo.setAge(c.getAge());
        vo.setVisitType(c.getVisitType());
        vo.setVisitTypeText(c.getVisitType() == null ? null : (c.getVisitType() == 1 ? "门诊" : "住院"));
        vo.setRegistId(c.getRegistId());
        vo.setInpId(c.getInpId());
        vo.setDeptId(c.getDeptId());
        vo.setDeptName(c.getDeptName());
        vo.setCaseSource(c.getCaseSource());
        vo.setCaseSourceText(c.getCaseSource() == null ? null : (c.getCaseSource() == 2 ? "医院感染" : "社区感染"));
        vo.setInfectionSite(c.getInfectionSite());
        vo.setInfectionSiteText(infectionSiteText(c.getInfectionSite()));
        vo.setInfectionDiag(c.getInfectionDiag());
        vo.setPathogen(c.getPathogen());
        vo.setSpecimen(c.getSpecimen());
        vo.setInfectDate(c.getInfectDate());
        vo.setCaseStatus(c.getCaseStatus());
        vo.setCaseStatusText(statusText(c.getCaseStatus()));
        vo.setLeakFlag(c.getLeakFlag());
        vo.setReportName(c.getReportName());
        vo.setReportTime(c.getReportTime());
        vo.setAuditName(c.getAuditName());
        vo.setAuditTime(c.getAuditTime());
        vo.setAuditRemark(c.getAuditRemark());
        vo.setRemark(c.getRemark());
        vo.setCreateTime(c.getCreateTime() == null ? null : String.valueOf(c.getCreateTime()));
        return vo;
    }

    private InfectionMonitorVO.HandObsRow toHandObsRow(BizHandHygieneObs o) {
        InfectionMonitorVO.HandObsRow vo = new InfectionMonitorVO.HandObsRow();
        vo.setId(o.getId());
        vo.setObsDate(o.getObsDate());
        vo.setDeptId(o.getDeptId());
        vo.setDeptName(o.getDeptName());
        vo.setObsObject(o.getObsObject());
        vo.setObsObjectText(obsObjectText(o.getObsObject()));
        vo.setOpportunityCount(o.getOpportunityCount());
        vo.setComplyCount(o.getComplyCount());
        vo.setComplyRate(rate(o.getComplyCount(), o.getOpportunityCount()) * 100);
        vo.setObserverName(o.getObserverName());
        vo.setObsTime(o.getObsTime());
        vo.setRemark(o.getRemark());
        return vo;
    }

    private interface RowMapper<T, R> {
        R map(T source);
    }
}
