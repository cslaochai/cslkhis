package com.his.medicaltech.service.impl;

import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.dto.CdrQueryDTO;
import com.his.medicaltech.enums.CdrEmergencyStatusEnum;
import com.his.medicaltech.enums.CdrEmergencyTriageEnum;
import com.his.medicaltech.enums.CdrRegistStatusEnum;
import com.his.medicaltech.enums.CdrVisitStatusEnum;
import com.his.medicaltech.mapper.CdrMapper;
import com.his.medicaltech.service.CdrService;
import com.his.medicaltech.enums.CdrEventTypeEnum;
import com.his.medicaltech.enums.CdrNodeTypeEnum;
import com.his.medicaltech.vo.*;
import com.his.patient.entity.BizPatient;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.service.PatientIndexService;
import com.his.patient.support.PatientProfileFields;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 患者全景时间轴实现（P5.2）。
 *
 * <p>取数只有 7 次数据库往返（事件 1 次 + 节点 4 次 + 档案 1 次 + 关系 1 次），
 * 其余全在内存里按锚点归位 —— 一个患者几十次就诊、上千条事件也不会变成查询风暴。
 *
 * <p>三个"不做就会错"的点，代码里都标了注释：
 * ① 患者ID必须经 EMPI 归并；② 码值翻译只走枚举的 getText；
 * ③ 归属不到就诊次的事件单列，不丢。
 */
@Service
@RequiredArgsConstructor
public class CdrServiceImpl implements CdrService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String ANCHOR_REGIST = "REGIST";
    private static final String ANCHOR_ADMISSION = "ADMISSION";
    private static final String ANCHOR_EMERGENCY = "EMERGENCY";
    private static final String ANCHOR_PATIENT = "PATIENT";

    private final CdrMapper cdrMapper;
    private final BizPatientMapper patientMapper;
    private final PatientIndexService patientIndexService;

    /**
     * 事件金额的语义标签。金额不能只给一个数字，得说清这是"实收"还是"总额"
     */
    private static String amountLabel(String etype) {
        return switch (etype) {
            case "charge" -> "实收";
            case "prepay" -> "本次预交";
            case "inpatientSummary", "inpatientSettlement", "insuranceSettlement" -> "费用总额";
            case "prescription", "laboratoryApply", "laboratoryReport",
                 "inspectionApply", "inspectionReport", "transfusion" -> "项目金额";
            case "inpatientOrder" -> "计价金额";
            default -> null;
        };
    }

    /**
     * 该行数据是否来自"非主档"的档案（EMPI 影子档案）
     */
    private static boolean isShadow(Object ownerPid, Long mainPid) {
        String owner = str(ownerPid);
        return owner != null && !owner.equals(String.valueOf(mainPid));
    }

    // 组装

    private static String str(Object v) {
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v);
        return s.isEmpty() ? null : s;
    }

    private static Integer intVal(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal dec(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal b) {
            return b;
        }
        if (v instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDateTime ldt(Object v) {
        return ldt(v, null);
    }

    private static LocalDateTime ldt(Object v, LocalDateTime fallback) {
        if (v == null) {
            return fallback;
        }
        if (v instanceof LocalDateTime l) {
            return l;
        }
        if (v instanceof Timestamp t) {
            return t.toLocalDateTime();
        }
        if (v instanceof java.sql.Date d) {
            return d.toLocalDate().atStartOfDay();
        }
        if (v instanceof LocalDate d) {
            return d.atStartOfDay();
        }
        if (v instanceof java.util.Date d) {
            return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) {
            return fallback;
        }
        try {
            return LocalDateTime.parse(s.replace(' ', 'T'));
        } catch (DateTimeParseException ignore) {
            // 继续尝试
        }
        try {
            return LocalDate.parse(s).atStartOfDay();
        } catch (DateTimeParseException e) {
            return fallback;
        }
    }

    private static String fmt(LocalDateTime t) {
        return t == null ? null : t.format(FMT);
    }

    private static List<String> splitIds(Object v) {
        List<String> out = new ArrayList<>();
        if (v == null) {
            return out;
        }
        for (String s : String.valueOf(v).split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    private static Long parseId(String v) {
        // ② 非 web 入口的入参：除时间轴的查询条件外，还被内部行数据解析复用，Bean Validation 只在 HTTP 参数绑定时跑
        if (!StringUtils.hasText(v)) {
            throw new BusinessException("患者ID不能为空");
        }
        try {
            return Long.valueOf(v.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("患者ID格式不正确：" + v);
        }
    }

    @Override
    public CdrTimelineVO getTimeline(CdrQueryDTO dto) {
        Long pid = parseId(dto == null ? null : dto.getPatientId());
        BizPatient main = patientMapper.selectById(pid);
        if (main == null) {
            throw new BusinessException("患者不存在或已删除");
        }

        // ① EMPI 归并：合并过的档案，历史数据必须一起出现
        List<Long> ids = resolveIds(pid);

        List<Map<String, Object>> eventRows = cdrMapper.selectTimelineEvents(ids);
        List<Map<String, Object>> admRows = cdrMapper.selectAdmissions(ids);
        List<Map<String, Object>> visitRows = cdrMapper.selectVisits(ids);
        List<Map<String, Object>> registRows = cdrMapper.selectRegistrations(ids);
        List<Map<String, Object>> emergencyRows = cdrMapper.selectEmergencies(ids);
        List<Map<String, Object>> archiveRows = cdrMapper.selectArchiveIdentities(ids);
        List<Map<String, Object>> profileRows = cdrMapper.selectHealthProfile(ids);

        // 档案号映射（用于把"这条数据其实挂在影子档案下"说清楚）
        Map<String, String> archiveNo = new HashMap<>();
        Map<String, String> archiveMergeTime = new HashMap<>();
        for (Map<String, Object> r : archiveRows) {
            archiveNo.put(str(r.get("pid")), str(r.get("patient_no")));
            archiveMergeTime.put(str(r.get("pid")), fmt(ldt(r.get("merge_time"))));
        }

        // 挂号ID → 节点键
        Map<String, Map<String, Object>> regMap = new LinkedHashMap<>();
        for (Map<String, Object> r : registRows) {
            regMap.put(str(r.get("regist_id")), r);
        }

        Map<String, CdrVisitNodeVO> nodes = new LinkedHashMap<>();
        Map<String, List<CdrEventVO>> nodeEvents = new LinkedHashMap<>();
        Map<String, String> registNode = new HashMap<>();
        Map<String, String> admNode = new HashMap<>();
        Map<String, String> emgNode = new HashMap<>();

        // 门诊就诊次（就诊次收录的挂号）
        for (Map<String, Object> v : visitRows) {
            String vid = str(v.get("visit_id"));
            String key = "V:" + vid;
            CdrVisitNodeVO node = newNode(key, CdrNodeTypeEnum.OUTPATIENT);
            node.setAnchorId(vid);
            node.setAnchorNo(str(v.get("visit_no")));
            node.setStartTime(fmt(ldt(v.get("start_time"))));
            node.setEndTime(fmt(ldt(v.get("end_time"))));
            node.setStatusText(CdrVisitStatusEnum.getText(intVal(v.get("visit_status"))));
            node.setTotalAmount(dec(v.get("total_amount")));
            node.setTitle(node.getNodeTypeText());

            // 本次就诊次包含的挂号：同时建立"挂号 → 就诊次"的映射
            List<String> rids = splitIds(v.get("regist_ids"));
            node.setSubtitle(rids.size() > 1 ? "含 " + rids.size() + " 张挂号单" : null);
            for (String rid : rids) {
                registNode.put(rid, key);
            }
            // 科室/医生取本次就诊次里最早的那张挂号
            String dept = null;
            String doctor = null;
            String owner = str(v.get("owner_pid"));
            for (String rid : rids) {
                Map<String, Object> rr = regMap.get(rid);
                if (rr == null) {
                    continue;
                }
                if (dept == null) {
                    dept = str(rr.get("dept_name"));
                }
                if (doctor == null) {
                    doctor = str(rr.get("doctor_name"));
                }
                if (owner == null) {
                    owner = str(rr.get("owner_pid"));
                }
            }
            node.setDeptName(dept);
            node.setOperatorName(doctor);
            node.setTitle(composeTitle(node.getNodeTypeText(), dept, doctor));
            nodes.put(key, node);
            nodeEvents.put(key, new ArrayList<>());
            node.setFromShadow(isShadow(v.get("owner_pid"), pid));
        }

        // 没有被任何就诊次收录的挂号：自己成一个节点（不丢）
        for (Map<String, Object> r : registRows) {
            String rid = str(r.get("regist_id"));
            if (registNode.containsKey(rid)) {
                continue;
            }
            String key = "R:" + rid;
            registNode.put(rid, key);
            CdrVisitNodeVO node = newNode(key, CdrNodeTypeEnum.OUTPATIENT);
            node.setAnchorId(rid);
            node.setAnchorNo(str(r.get("regist_no")));
            node.setStartTime(fmt(ldt(r.get("regist_time"), ldt(r.get("visit_date")))));
            node.setDeptName(str(r.get("dept_name")));
            node.setOperatorName(str(r.get("doctor_name")));
            node.setStatusText(CdrRegistStatusEnum.getText(intVal(r.get("regist_status"))));
            node.setTitle(composeTitle(node.getNodeTypeText(), node.getDeptName(), node.getOperatorName()));
            node.setSubtitle("该挂号未被就诊次收录");
            node.setFromShadow(isShadow(r.get("owner_pid"), pid));
            nodes.put(key, node);
            nodeEvents.put(key, new ArrayList<>());
        }

        // 住院
        for (Map<String, Object> a : admRows) {
            String aid = str(a.get("admission_id"));
            String key = "A:" + aid;
            admNode.put(aid, key);
            CdrVisitNodeVO node = newNode(key, CdrNodeTypeEnum.INPATIENT);
            node.setAnchorId(aid);
            node.setAnchorNo(str(a.get("admission_no")));
            LocalDateTime admit = ldt(a.get("admit_time"));
            LocalDateTime dis = ldt(a.get("discharge_time"));
            node.setStartTime(fmt(admit));
            node.setEndTime(fmt(dis));
            node.setDeptName(str(a.get("dept_name")));
            node.setStatusText(AdmitStatusEnum.getText(intVal(a.get("admit_status"))));
            String ward = str(a.get("ward_name"));
            String bed = str(a.get("bed_no"));
            node.setSubtitle(StringUtils.hasText(ward) || StringUtils.hasText(bed)
                    ? ((ward == null ? "" : ward) + " " + (bed == null ? "" : bed)).trim() : null);
            String diag = str(a.get("summary_diag"));
            node.setOutcome(StringUtils.hasText(diag) ? diag : str(a.get("diagnosis")));
            if (admit != null) {
                LocalDateTime end = dis != null ? dis : LocalDateTime.now();
                node.setDurationDays((int) Math.max(1, ChronoUnit.DAYS.between(admit.toLocalDate(), end.toLocalDate())));
            }
            node.setTitle(composeTitle(node.getNodeTypeText(), node.getDeptName(), null));
            node.setFromShadow(isShadow(a.get("owner_pid"), pid));
            nodes.put(key, node);
            nodeEvents.put(key, new ArrayList<>());
        }

        // 急诊
        for (Map<String, Object> e : emergencyRows) {
            String eid = str(e.get("emergency_id"));
            String key = "E:" + eid;
            emgNode.put(eid, key);
            CdrVisitNodeVO node = newNode(key, CdrNodeTypeEnum.EMERGENCY);
            node.setAnchorId(eid);
            node.setAnchorNo(str(e.get("emergency_no")));
            node.setStartTime(fmt(ldt(e.get("admission_time"))));
            node.setEndTime(fmt(ldt(e.get("finish_time"))));
            node.setDeptName(str(e.get("dept_name")));
            node.setOperatorName(str(e.get("doctor_name")));
            node.setStatusText(CdrEmergencyStatusEnum.getText(intVal(e.get("emergency_status"))));
            String triage = CdrEmergencyTriageEnum.getText(intVal(e.get("triage_level")));
            String zone = str(e.get("zone"));
            node.setSubtitle(StringUtils.hasText(triage) || StringUtils.hasText(zone)
                    ? ((triage == null ? "" : triage) + " " + (zone == null ? "" : zone)).trim() : null);
            node.setOutcome(str(e.get("diagnosis")));
            node.setTitle(composeTitle(node.getNodeTypeText(), node.getDeptName(), node.getOperatorName()));
            node.setFromShadow(isShadow(e.get("owner_pid"), pid));
            nodes.put(key, node);
            nodeEvents.put(key, new ArrayList<>());
        }

        // 事件归位
        List<CdrEventVO> unresolved = new ArrayList<>();
        Set<String> unknownTypes = new LinkedHashSet<>();
        for (Map<String, Object> r : eventRows) {
            CdrEventVO ev = toEvent(r, pid, archiveNo);
            if (ev.getEventTypeText().startsWith("未知事件")) {
                unknownTypes.add(ev.getEventType());
            }
            String atype = ev.getAnchorType();
            String aid = ev.getAnchorId();
            String key = null;
            if (aid != null) {
                if (ANCHOR_REGIST.equals(atype)) {
                    key = registNode.get(aid);
                } else if (ANCHOR_ADMISSION.equals(atype)) {
                    key = admNode.get(aid);
                } else if (ANCHOR_EMERGENCY.equals(atype)) {
                    key = emgNode.get(aid);
                } else if (ANCHOR_PATIENT.equals(atype)) {
                    key = "P:" + pid;
                }
            }
            if (key == null) {
                // ③ 归属不上就单列，绝不静默丢弃
                unresolved.add(ev);
                continue;
            }
            if (key.startsWith("P:") && !nodes.containsKey(key)) {
                CdrVisitNodeVO node = newNode(key, CdrNodeTypeEnum.PATIENT);
                node.setAnchorId(String.valueOf(pid));
                node.setTitle(node.getNodeTypeText());
                node.setSubtitle("不属于某一次就诊的记录（危急值 / 质控 / 随访 / 转诊 / 上报）");
                nodes.put(key, node);
                nodeEvents.put(key, new ArrayList<>());
            }
            nodeEvents.get(key).add(ev);
        }

        // 节点收尾：排序、计数、缺口
        List<CdrVisitNodeVO> visitList = new ArrayList<>();
        for (Map.Entry<String, CdrVisitNodeVO> en : nodes.entrySet()) {
            CdrVisitNodeVO node = en.getValue();
            List<CdrEventVO> evs = nodeEvents.get(en.getKey());
            evs.sort(Comparator.comparing(CdrEventVO::getEventTime,
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            node.setEvents(evs);
            node.setEventCount(evs.size());
            node.setCounts(countByType(evs));
            node.setGaps(findGaps(node, evs));
            if (node.getTotalAmount() == null || node.getTotalAmount().signum() == 0) {
                node.setTotalAmount(sumCharge(evs));
            }
            if (node.getOutcome() == null) {
                node.setOutcome(firstDiagnosis(evs));
            }
            visitList.add(node);
        }
        visitList.sort(Comparator.comparing(CdrVisitNodeVO::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        // 过滤（就诊次类型 / 事件类型 / 日期区间）
        if (StringUtils.hasText(dto.getEventType())) {
            String want = dto.getEventType().trim();
            // "只看某类事件"必须真的只留这类事件 —— 只筛节点、节点内其它事件照旧显示，
            // 使用者会以为筛没生效。缺口（gaps）刻意保留筛选前的判定结果：
            // 筛的是屏幕上的事件，不是病历的完整与否。
            for (CdrVisitNodeVO n : visitList) {
                List<CdrEventVO> hit = n.getEvents().stream()
                        .filter(e -> want.equals(e.getEventType())).collect(Collectors.toList());
                n.setEvents(hit);
                n.setEventCount(hit.size());
                n.setCounts(countByType(hit));
            }
            visitList = visitList.stream().filter(n -> n.getEventCount() > 0).collect(Collectors.toList());
            unresolved = unresolved.stream().filter(e -> want.equals(e.getEventType())).collect(Collectors.toList());
        }
        if (StringUtils.hasText(dto.getStartDate()) || StringUtils.hasText(dto.getEndDate())) {
            visitList = visitList.stream().filter(n -> inRange(n.getStartTime(),
                    dto.getStartDate(), dto.getEndDate())).collect(Collectors.toList());
        }

        // 未归位事件的档案号也要能看出来
        unresolved.sort(Comparator.comparing(CdrEventVO::getEventTime,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        // 概览
        CdrSummaryVO summary = new CdrSummaryVO();
        int outCount = (int) visitList.stream().filter(n -> CdrNodeTypeEnum.OUTPATIENT.getCode().equals(n.getNodeType())).count();
        int inCount = (int) visitList.stream().filter(n -> CdrNodeTypeEnum.INPATIENT.getCode().equals(n.getNodeType())).count();
        int emCount = (int) visitList.stream().filter(n -> CdrNodeTypeEnum.EMERGENCY.getCode().equals(n.getNodeType())).count();
        int activeIn = (int) visitList.stream().filter(n -> CdrNodeTypeEnum.INPATIENT.getCode().equals(n.getNodeType())
                && "在院".equals(n.getStatusText())).count();
        summary.setVisitCount(outCount + inCount + emCount);
        summary.setOutpatientCount(outCount);
        summary.setInpatientCount(inCount);
        summary.setEmergencyCount(emCount);
        summary.setActiveInpatientCount(activeIn);
        int totalEvents = visitList.stream().mapToInt(n -> n.getEventCount() == null ? 0 : n.getEventCount()).sum();
        summary.setEventCount(totalEvents);
        summary.setUnresolvedEventCount(unresolved.size());
        summary.setEventCounts(countByType(visitList.stream()
                .flatMap(n -> n.getEvents().stream()).collect(Collectors.toList())));
        List<String> nodeStarts = visitList.stream().map(CdrVisitNodeVO::getStartTime)
                .filter(Objects::nonNull).sorted().collect(Collectors.toList());
        if (!nodeStarts.isEmpty()) {
            summary.setFirstVisitTime(nodeStarts.get(0));
            summary.setLastVisitTime(nodeStarts.get(nodeStarts.size() - 1));
        }
        BigDecimal total = BigDecimal.ZERO;
        for (CdrVisitNodeVO n : visitList) {
            if (n.getTotalAmount() != null) {
                total = total.add(n.getTotalAmount());
            }
        }
        summary.setTotalAmount(total);

        // 患者身份卡
        CdrPatientVO pv = toPatient(main, ids, archiveRows, pid, profileRows);

        // 健康档案
        List<CdrProfileGroupVO> profile = toProfile(profileRows);

        // 提示
        List<String> warnings = new ArrayList<>();
        if (ids.size() > 1) {
            warnings.add("本页按患者主索引归并了 " + ids.size() + " 份档案的数据（含主档 " + main.getPatientNo() + "）。"
                    + "被并档案的记录仍在原档案号下，未做搬迁。");
        }
        if (!unresolved.isEmpty()) {
            warnings.add("有 " + unresolved.size() + " 条记录归属不到任何一次就诊"
                    + "（它们指向的挂号/入院记录不存在，或不属于该患者），已单列在页面最下方。");
        }
        if (!unknownTypes.isEmpty()) {
            warnings.add("出现了未登记的事件类型：" + String.join("、", unknownTypes) + "。请核对是否新增了数据源未同步字典。");
        }
        List<String> gapNodes = visitList.stream()
                .filter(n -> n.getGaps() != null && !n.getGaps().isEmpty())
                .map(n -> (n.getAnchorNo() == null ? n.getNodeKey() : n.getAnchorNo()) + "（" + String.join("、", n.getGaps()) + "）")
                .collect(Collectors.toList());
        if (!gapNodes.isEmpty()) {
            warnings.add("发现 " + gapNodes.size() + " 个就诊次存在病历完整性缺口：" + String.join("；", gapNodes));
        }

        CdrTimelineVO vo = new CdrTimelineVO();
        vo.setPatient(pv);
        vo.setSummary(summary);
        vo.setVisits(visitList);
        vo.setUnresolvedEvents(unresolved);
        vo.setProfile(profile);
        vo.setWarnings(warnings);
        return vo;
    }

    @Override
    public List<CdrEventTypeSelectListVO> eventDict() {
        List<CdrEventTypeSelectListVO> list = new ArrayList<>();
        for (CdrEventTypeEnum t : CdrEventTypeEnum.values()) {
            CdrEventTypeSelectListVO item = new CdrEventTypeSelectListVO();
            item.setCode(t.getCode());
            item.setText(t.getText());
            item.setNodeType(t.getNodeType() == null ? null : t.getNodeType().getCode());
            item.setNodeTypeText(t.getNodeType() == null ? null : t.getNodeType().getText());
            item.setSecondaryLabel(t.getSecondaryLabel());
            list.add(item);
        }
        return list;
    }

    private CdrEventVO toEvent(Map<String, Object> r, Long mainPid, Map<String, String> archiveNo) {
        String etype = str(r.get("etype"));
        CdrEventTypeEnum type = CdrEventTypeEnum.parse(etype);
        CdrEventVO ev = new CdrEventVO();
        ev.setEventType(etype);
        ev.setEventTypeText(type == null ? "未知事件(" + etype + ")" : type.getText());
        ev.setSourceTable(str(r.get("src_table")));
        ev.setSourceId(str(r.get("src_id")));
        ev.setEventTime(fmt(ldt(r.get("etime"))));
        ev.setTitle(str(r.get("title")));
        ev.setSummary(str(r.get("summary")));
        ev.setDeptName(str(r.get("dept_name")));
        ev.setOperatorName(str(r.get("operator_name")));
        ev.setAnchorType(str(r.get("anchor_type")));
        ev.setAnchorId(str(r.get("anchor_id")));
        ev.setAmount(dec(r.get("amount")));
        ev.setAmountLabel(amountLabel(etype));
        Integer status = intVal(r.get("status_code"));
        Integer second = intVal(r.get("secondary_code"));
        ev.setStatusCode(status);
        ev.setSecondaryCode(second);
        if (type != null) {
            // ② 码值翻译只在这里，展示口径走枚举 getText，未知码值给空串不回落
            ev.setStatusText(type.statusText(status));
            ev.setSecondaryText(type.secondaryText(second));
            ev.setSecondaryLabel(type.getSecondaryLabel());
        }
        String owner = str(r.get("owner_pid"));
        ev.setOwnerPatientId(owner);
        if (owner != null && !owner.equals(String.valueOf(mainPid))) {
            ev.setOwnerArchiveNo(archiveNo.get(owner));
        }
        return ev;
    }

    private CdrPatientVO toPatient(BizPatient p, List<Long> ids, List<Map<String, Object>> archiveRows, Long mainPid,
                                   List<Map<String, Object>> profileRows) {
        CdrPatientVO vo = new CdrPatientVO();
        vo.setPatientId(p.getId());
        vo.setPatientNo(p.getPatientNo());
        vo.setPatientName(p.getPatientName());
        vo.setGenderText(SysGenderEnum.getText(p.getGender()));
        vo.setAge(p.getAge());
        vo.setBirthDate(p.getBirthDate() == null ? null : p.getBirthDate().format(DATE_FMT));
        vo.setIdCard(p.getIdCard());
        vo.setPhone(p.getPhone());
        vo.setAddress(p.getAddress());
        vo.setBloodType(p.getBloodType());
        vo.setMedicalInsuranceType(p.getMedicalInsuranceType());
        vo.setMasterId(p.getMasterId());
        vo.setMergeStatus(p.getMergeStatus());
        vo.setMergeStatusText(p.getMergeStatus() == null ? null
                : (p.getMergeStatus() == 1 ? "已并入主档" : "正常（主档）"));
        vo.setResolvedArchiveCount(ids.size());
        List<CdrArchiveVO> shadows = new ArrayList<>();
        for (Map<String, Object> r : archiveRows) {
            String rid = str(r.get("pid"));
            if (rid == null || rid.equals(String.valueOf(mainPid))) {
                continue;
            }
            CdrArchiveVO a = new CdrArchiveVO();
            a.setPatientId(parseId(rid));
            a.setPatientNo(str(r.get("patient_no")));
            a.setPatientName(str(r.get("patient_name")));
            a.setMergeTime(fmt(ldt(r.get("merge_time"))));
            shadows.add(a);
        }
        vo.setShadowArchives(shadows);
        // 结构化档案表里已有数据的分组，用来修正"文本字段为空"造成的假缺失
        // （详见 PatientProfileFields.applyProfileCoverage 的注释）
        Set<String> covered = new HashSet<>();
        for (Map<String, Object> r : profileRows) {
            String k = str(r.get("pkey"));
            if (k != null) {
                covered.add(k);
            }
        }
        PatientProfileFields.ProfileScore score = PatientProfileFields.applyProfileCoverage(
                PatientProfileFields.score(p), covered);
        vo.setCompleteRate((int) Math.floor(score.rate()));
        vo.setMissingFields(score.missingFields());
        return vo;
    }

    private List<CdrProfileGroupVO> toProfile(List<Map<String, Object>> rows) {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("allergy", "过敏史");
        labels.put("pastDisease", "既往史");
        labels.put("surgery", "手术史");
        labels.put("family", "家族史");
        labels.put("medication", "用药史");
        labels.put("contact", "联系人");
        Map<String, List<CdrProfileGroupVO.CdrProfileItemVO>> grouped = new LinkedHashMap<>();
        labels.keySet().forEach(k -> grouped.put(k, new ArrayList<>()));
        for (Map<String, Object> r : rows) {
            String key = str(r.get("pkey"));
            if (!grouped.containsKey(key)) {
                continue;
            }
            CdrProfileGroupVO.CdrProfileItemVO item = new CdrProfileGroupVO.CdrProfileItemVO();
            item.setId(str(r.get("sid")));
            item.setTitle(str(r.get("title")));
            item.setSummary(str(r.get("summary")));
            item.setTime(fmt(ldt(r.get("tm"))));
            item.setOwnerPatientId(str(r.get("owner_pid")));
            grouped.get(key).add(item);
        }
        List<CdrProfileGroupVO> out = new ArrayList<>();
        for (Map.Entry<String, List<CdrProfileGroupVO.CdrProfileItemVO>> en : grouped.entrySet()) {
            CdrProfileGroupVO g = new CdrProfileGroupVO();
            g.setKey(en.getKey());
            g.setLabel(labels.get(en.getKey()));
            g.setCount(en.getValue().size());
            g.setItems(en.getValue());
            out.add(g);
        }
        return out;
    }

    // 取值工具

    /**
     * 病历完整性缺口。
     *
     * <p>这是 CDR 里唯一"主动判断"的部分：不是展示有什么，而是指出**该有的没有**。
     * 一条都不能凭空捏：判据全部来自节点自身字段或已取回的事件。
     */
    private List<String> findGaps(CdrVisitNodeVO node, List<CdrEventVO> evs) {
        List<String> gaps = new ArrayList<>();
        Set<String> types = evs.stream().map(CdrEventVO::getEventType).collect(Collectors.toSet());
        if (CdrNodeTypeEnum.INPATIENT.getCode().equals(node.getNodeType())) {
            boolean hasAdmitRecord = evs.stream().anyMatch(e -> "inpatientRecord".equals(e.getEventType())
                    && Integer.valueOf(1).equals(e.getSecondaryCode()));
            if (!hasAdmitRecord) {
                gaps.add("缺入院记录");
            }
            if (!types.contains("inpatientDiagnosis")) {
                gaps.add("缺住院诊断");
            }
            if (!types.contains("inpatientOrder")) {
                gaps.add("本次住院无医嘱记录");
            }
            if (!types.contains("inpatientSummary")) {
                gaps.add("缺病案首页");
            }
            boolean discharged = "已出院".equals(node.getStatusText()) || types.contains("discharge");
            if (discharged) {
                boolean hasOutRecord = evs.stream().anyMatch(e -> "inpatientRecord".equals(e.getEventType())
                        && Integer.valueOf(7).equals(e.getSecondaryCode()));
                if (!hasOutRecord) {
                    gaps.add("缺出院记录");
                }
                if (!types.contains("inpatientSettlement")) {
                    gaps.add("缺住院结算单");
                }
            }
        } else if (CdrNodeTypeEnum.OUTPATIENT.getCode().equals(node.getNodeType())) {
            if (types.contains("prescription") && !types.contains("outpatientRecord")) {
                gaps.add("有处方但没有门诊病历");
            }
            if (types.contains("laboratoryApply") && !types.contains("laboratoryReport")) {
                gaps.add("检验已开单但没有报告");
            }
            if (types.contains("inspectionApply") && !types.contains("inspectionReport")) {
                gaps.add("检查已开单但没有报告");
            }
            if (evs.isEmpty()) {
                gaps.add("该就诊次下没有任何记录");
            }
        } else if (CdrNodeTypeEnum.EMERGENCY.getCode().equals(node.getNodeType())) {
            if (!StringUtils.hasText(node.getOutcome())) {
                gaps.add("缺初步诊断");
            }
        }
        return gaps;
    }

    private List<CdrCountVO> countByType(List<CdrEventVO> evs) {
        Map<String, int[]> counter = new LinkedHashMap<>();
        Map<String, String> labelMap = new LinkedHashMap<>();
        for (CdrEventVO e : evs) {
            int[] c = counter.computeIfAbsent(e.getEventType(), k -> new int[1]);
            c[0]++;
            labelMap.putIfAbsent(e.getEventType(), e.getEventTypeText());
        }
        List<CdrCountVO> out = new ArrayList<>();
        for (Map.Entry<String, int[]> en : counter.entrySet()) {
            out.add(new CdrCountVO(en.getKey(), labelMap.get(en.getKey()), en.getValue()[0]));
        }
        out.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        return out;
    }

    private BigDecimal sumCharge(List<CdrEventVO> evs) {
        BigDecimal sum = BigDecimal.ZERO;
        for (CdrEventVO e : evs) {
            if ("charge".equals(e.getEventType()) && e.getAmount() != null) {
                sum = sum.add(e.getAmount());
            }
        }
        return sum;
    }

    private String firstDiagnosis(List<CdrEventVO> evs) {
        // 结算/首页里已经带了权威诊断，优先；否则看有没有诊断类事件写明了诊断名
        for (CdrEventVO e : evs) {
            if ("inpatientSummary".equals(e.getEventType()) && StringUtils.hasText(e.getTitle())) {
                return e.getTitle().replaceFirst("^病案首页 ", "");
            }
        }
        return null;
    }

    private CdrVisitNodeVO newNode(String key, CdrNodeTypeEnum type) {
        CdrVisitNodeVO node = new CdrVisitNodeVO();
        node.setNodeKey(key);
        node.setNodeType(type.getCode());
        node.setNodeTypeText(type.getText());
        node.setTitle(type.getText());
        node.setFromShadow(false);
        return node;
    }

    private String composeTitle(String typeText, String dept, String doctor) {
        StringBuilder sb = new StringBuilder(typeText);
        if (StringUtils.hasText(dept)) {
            sb.append(" · ").append(dept);
        }
        if (StringUtils.hasText(doctor)) {
            sb.append(" · ").append(doctor);
        }
        return sb.toString();
    }

    private boolean inRange(String startTime, String from, String to) {
        if (startTime == null) {
            return false;
        }
        String day = startTime.length() >= 10 ? startTime.substring(0, 10) : startTime;
        if (StringUtils.hasText(from) && day.compareTo(from) < 0) {
            return false;
        }
        return !StringUtils.hasText(to) || day.compareTo(to) <= 0;
    }

    /**
     * EMPI 归并：查询口径统一走 his-patient，不在报表模块里另写一份
     */
    private List<Long> resolveIds(Long pid) {
        List<Long> ids = patientIndexService.resolvePatientIds(pid);
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>(List.of(pid));
        }
        if (!ids.contains(pid)) {
            ids = new ArrayList<>(ids);
            ids.add(pid);
        }
        return ids;
    }
}
