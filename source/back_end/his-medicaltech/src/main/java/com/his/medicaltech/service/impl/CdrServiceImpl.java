package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.enums.AppointStatusEnum;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.medicaltech.dto.CdrQueryDTO;
import com.his.common.enums.EmergencyStatusEnum;
import com.his.common.enums.EmergencyTriageLevelEnum;
import com.his.medicaltech.enums.CdrEventTypeEnum;
import com.his.medicaltech.enums.CdrNodeTypeEnum;
import com.his.medicaltech.mapper.CdrMapper;
import com.his.medicaltech.service.CdrService;
import com.his.medicaltech.vo.*;
import com.his.patient.entity.BizPatient;
import com.his.patient.enums.VisitStatusEnum;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.service.PatientIndexService;
import com.his.patient.support.PatientProfileFields;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 患者全景时间轴实现（P5.2）。
 */
@Service
@RequiredArgsConstructor
public class CdrServiceImpl extends ServiceImpl<BizPatientMapper, BizPatient> implements CdrService {

    private static final String ANCHOR_REGIST = "REGIST";
    private static final String ANCHOR_ADMISSION = "ADMISSION";
    private static final String ANCHOR_EMERGENCY = "EMERGENCY";
    private static final String ANCHOR_PATIENT = "PATIENT";

    private final CdrMapper cdrMapper;

    private final BizPatientMapper bizPatientMapper;

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
    private static boolean isShadow(String ownerPid, Long mainPid) {
        return ownerPid != null && !ownerPid.isEmpty() && !ownerPid.equals(String.valueOf(mainPid));
    }

    // 组装

    /**
     * 空串归一为 null。
     *
     * <p>为什么需要：SQL 里不少列是 {@code CONCAT}/{@code LEFT} 拼出来的，空值会拼成
     * 空串而不是 null；直接透传会让页面出现"标题存在但内容空白"的行。
     */
    private static String str(String v) {
        return v == null || v.isEmpty() ? null : v;
    }

    private static String fmt(LocalDateTime t) {
        return t == null ? null : t.format(DateFormats.DATETIME);
    }

    private static List<Long> splitIds(String v) {
        List<Long> out = new ArrayList<>();
        if (v == null) {
            return out;
        }
        for (String s : v.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                out.add(Long.valueOf(t));
            }
        }
        return out;
    }

    /**
     * 解析裸 SQL 行里的 pid 字符串。
     *
     * <p><b>只用于内部行数据</b>（{@code CdrArchiveIdentityRowVO#pid}，SQL 里 CAST AS CHAR 的列），
     * 不是 HTTP 入参 —— HTTP 入参一律走 DTO 的 Long 字段，让 Jackson 在绑定层就报 400。
     */
    private static Long parseRowId(String v) {
        // C-非 web 入参：解析的是内部裸 SQL 行里的字符串列，不是 HTTP 参数绑定，Bean Validation 不覆盖，保留
        if (!TextUtil.hasText(v)) {
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
        Long pid = dto.getPatientId();
        BizPatient main = bizPatientMapper.selectById(pid);
        if (main == null) {
            throw new BusinessException("患者不存在或已删除");
        }

        // ① EMPI 归并：合并过的档案，历史数据必须一起出现
        List<Long> ids = resolveIds(pid);

        List<CdrEventRowVO> eventRows = cdrMapper.selectTimelineEvents(ids);
        List<CdrAdmissionRowVO> admRows = cdrMapper.selectAdmissions(ids);
        List<CdrVisitRowVO> visitRows = cdrMapper.selectVisits(ids);
        List<CdrRegistrationRowVO> registRows = cdrMapper.selectRegistrations(ids);
        List<CdrEmergencyRowVO> emergencyRows = cdrMapper.selectEmergencies(ids);
        List<CdrArchiveIdentityRowVO> archiveRows = cdrMapper.selectArchiveIdentities(ids);
        List<CdrProfileRowVO> profileRows = cdrMapper.selectHealthProfile(ids);

        // 档案号映射（用于把"这条数据其实挂在影子档案下"说清楚）
        Map<String, String> archiveNo = new HashMap<>();
        for (CdrArchiveIdentityRowVO r : archiveRows) {
            archiveNo.put(str(r.getPid()), str(r.getPatientNo()));
        }

        // 挂号ID → 节点键
        Map<Long, CdrRegistrationRowVO> regMap = new LinkedHashMap<>();
        for (CdrRegistrationRowVO r : registRows) {
            regMap.put(r.getRegistId(), r);
        }

        Map<String, CdrVisitNodeVO> nodes = new LinkedHashMap<>();
        Map<String, List<CdrEventVO>> nodeEvents = new LinkedHashMap<>();
        Map<Long, String> registNode = new HashMap<>();
        Map<Long, String> admNode = new HashMap<>();
        Map<Long, String> emgNode = new HashMap<>();

        // 门诊就诊次（就诊次收录的挂号）
        for (CdrVisitRowVO v : visitRows) {
            Long vid = v.getVisitId();
            String key = "V:" + vid;
            CdrVisitNodeVO node = newNode(key, CdrNodeTypeEnum.OUTPATIENT);
            node.setAnchorId(vid);
            node.setAnchorNo(str(v.getVisitNo()));
            node.setStartTime(fmt(v.getStartTime()));
            node.setEndTime(fmt(v.getEndTime()));
            node.setStatusText(VisitStatusEnum.getText(v.getVisitStatus()));
            node.setTotalAmount(v.getTotalAmount());
            node.setTitle(node.getNodeTypeText());

            // 本次就诊次包含的挂号：同时建立"挂号 → 就诊次"的映射
            List<Long> rids = splitIds(v.getRegistIds());
            node.setSubtitle(rids.size() > 1 ? "含 " + rids.size() + " 张挂号单" : null);
            for (Long rid : rids) {
                registNode.put(rid, key);
            }
            // 科室/医生取本次就诊次里最早的那张挂号
            String dept = null;
            String doctor = null;
            String owner = str(v.getOwnerPid());
            for (Long rid : rids) {
                CdrRegistrationRowVO rr = regMap.get(rid);
                if (rr == null) {
                    continue;
                }
                if (dept == null) {
                    dept = str(rr.getDeptName());
                }
                if (doctor == null) {
                    doctor = str(rr.getDoctorName());
                }
                if (owner == null) {
                    owner = str(rr.getOwnerPid());
                }
            }
            node.setDeptName(dept);
            node.setOperatorName(doctor);
            node.setTitle(composeTitle(node.getNodeTypeText(), dept, doctor));
            nodes.put(key, node);
            nodeEvents.put(key, new ArrayList<>());
            node.setFromShadow(isShadow(v.getOwnerPid(), pid));
        }

        // 没有被任何就诊次收录的挂号：自己成一个节点（不丢）
        for (CdrRegistrationRowVO r : registRows) {
            Long rid = r.getRegistId();
            if (registNode.containsKey(rid)) {
                continue;
            }
            String key = "R:" + rid;
            registNode.put(rid, key);
            CdrVisitNodeVO node = newNode(key, CdrNodeTypeEnum.OUTPATIENT);
            node.setAnchorId(rid);
            node.setAnchorNo(str(r.getRegistNo()));
            // 挂号时间缺失时用就诊日期兜底：宁可给个粗粒度时间，也不要让节点没有起始时间
            LocalDateTime start = r.getRegistTime() != null ? r.getRegistTime()
                    : (TimeUtil.dayStart(r.getVisitDate()));
            node.setStartTime(fmt(start));
            node.setDeptName(str(r.getDeptName()));
            node.setOperatorName(str(r.getDoctorName()));
            node.setStatusText(AppointStatusEnum.getText(r.getRegistStatus()));
            node.setTitle(composeTitle(node.getNodeTypeText(), node.getDeptName(), node.getOperatorName()));
            node.setSubtitle("该挂号未被就诊次收录");
            node.setFromShadow(isShadow(r.getOwnerPid(), pid));
            nodes.put(key, node);
            nodeEvents.put(key, new ArrayList<>());
        }

        // 住院
        for (CdrAdmissionRowVO a : admRows) {
            Long aid = a.getAdmissionId();
            String key = "A:" + aid;
            admNode.put(aid, key);
            CdrVisitNodeVO node = newNode(key, CdrNodeTypeEnum.INPATIENT);
            node.setAnchorId(aid);
            node.setAnchorNo(str(a.getAdmissionNo()));
            LocalDateTime admit = a.getAdmitTime();
            LocalDateTime dis = a.getDischargeTime();
            node.setStartTime(fmt(admit));
            node.setEndTime(fmt(dis));
            node.setDeptName(str(a.getDeptName()));
            node.setStatusText(AdmitStatusEnum.getText(a.getAdmitStatus()));
            String ward = str(a.getWardName());
            String bed = str(a.getBedNo());
            node.setSubtitle(TextUtil.hasText(ward) || TextUtil.hasText(bed)
                    ? ((ward == null ? "" : ward) + " " + (bed == null ? "" : bed)).trim() : null);
            String diag = str(a.getSummaryDiag());
            node.setOutcome(TextUtil.hasText(diag) ? diag : str(a.getDiagnosis()));
            if (admit != null) {
                LocalDateTime end = dis != null ? dis : LocalDateTime.now();
                node.setDurationDays((int) Math.max(1, ChronoUnit.DAYS.between(admit.toLocalDate(), end.toLocalDate())));
            }
            node.setTitle(composeTitle(node.getNodeTypeText(), node.getDeptName(), null));
            node.setFromShadow(isShadow(a.getOwnerPid(), pid));
            nodes.put(key, node);
            nodeEvents.put(key, new ArrayList<>());
        }

        // 急诊
        for (CdrEmergencyRowVO e : emergencyRows) {
            Long eid = e.getEmergencyId();
            String key = "E:" + eid;
            emgNode.put(eid, key);
            CdrVisitNodeVO node = newNode(key, CdrNodeTypeEnum.EMERGENCY);
            node.setAnchorId(eid);
            node.setAnchorNo(str(e.getEmergencyNo()));
            node.setStartTime(fmt(e.getAdmissionTime()));
            node.setEndTime(fmt(e.getFinishTime()));
            node.setDeptName(str(e.getDeptName()));
            node.setOperatorName(str(e.getDoctorName()));
            node.setStatusText(EmergencyStatusEnum.getText(e.getEmergencyStatus()));
            String triage = EmergencyTriageLevelEnum.getText(e.getTriageLevel());
            String zone = str(e.getZone());
            node.setSubtitle(TextUtil.hasText(triage) || TextUtil.hasText(zone)
                    ? ((triage == null ? "" : triage) + " " + (zone == null ? "" : zone)).trim() : null);
            node.setOutcome(str(e.getDiagnosis()));
            node.setTitle(composeTitle(node.getNodeTypeText(), node.getDeptName(), node.getOperatorName()));
            node.setFromShadow(isShadow(e.getOwnerPid(), pid));
            nodes.put(key, node);
            nodeEvents.put(key, new ArrayList<>());
        }

        // 事件归位
        List<CdrEventVO> unresolved = new ArrayList<>();
        Set<String> unknownTypes = new LinkedHashSet<>();
        for (CdrEventRowVO r : eventRows) {
            CdrEventVO ev = toEvent(r, pid, archiveNo);
            if (ev.getEventTypeText().startsWith("未知事件")) {
                unknownTypes.add(ev.getEventType());
            }
            String atype = ev.getAnchorType();
            Long aid = ev.getAnchorId();
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
                node.setAnchorId(pid);
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
        if (TextUtil.hasText(dto.getEventType())) {
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
        if (TextUtil.hasText(dto.getStartDate()) || TextUtil.hasText(dto.getEndDate())) {
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

    private CdrEventVO toEvent(CdrEventRowVO r, Long mainPid, Map<String, String> archiveNo) {
        String etype = str(r.getEtype());
        CdrEventTypeEnum type = CdrEventTypeEnum.parse(etype);
        CdrEventVO ev = new CdrEventVO();
        ev.setEventType(etype);
        ev.setEventTypeText(type == null ? "未知事件(" + etype + ")" : type.getText());
        ev.setSourceTable(str(r.getSrcTable()));
        ev.setSourceId(r.getSrcId());
        ev.setEventTime(fmt(r.getEtime()));
        ev.setTitle(str(r.getTitle()));
        ev.setSummary(str(r.getSummary()));
        ev.setDeptName(str(r.getDeptName()));
        ev.setOperatorName(str(r.getOperatorName()));
        ev.setAnchorType(str(r.getAnchorType()));
        ev.setAnchorId(r.getAnchorId());
        ev.setAmount(r.getAmount());
        ev.setAmountLabel(amountLabel(etype));
        Integer status = r.getStatusCode();
        Integer second = r.getSecondaryCode();
        ev.setStatusCode(status);
        ev.setSecondaryCode(second);
        if (type != null) {
            // ② 码值翻译只在这里，展示口径走枚举 getText，未知码值给空串不回落
            ev.setStatusText(type.statusText(status));
            ev.setSecondaryText(type.secondaryText(second));
            ev.setSecondaryLabel(type.getSecondaryLabel());
        }
        String owner = str(r.getOwnerPid());
        Long ownerPid = owner == null ? null : Long.valueOf(owner);
        ev.setOwnerPatientId(ownerPid);
        if (ownerPid != null && !ownerPid.equals(mainPid)) {
            ev.setOwnerArchiveNo(archiveNo.get(owner));
        }
        return ev;
    }

    private CdrPatientVO toPatient(BizPatient p, List<Long> ids, List<CdrArchiveIdentityRowVO> archiveRows, Long mainPid,
                                   List<CdrProfileRowVO> profileRows) {
        CdrPatientVO vo = new CdrPatientVO();
        vo.setPatientId(p.getId());
        vo.setPatientNo(p.getPatientNo());
        vo.setPatientName(p.getPatientName());
        vo.setGenderText(SysGenderEnum.getText(p.getGender()));
        vo.setAge(p.getAge());
        vo.setBirthDate(p.getBirthDate() == null ? null : p.getBirthDate().format(DateFormats.DATE));
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
        for (CdrArchiveIdentityRowVO r : archiveRows) {
            String rid = str(r.getPid());
            if (rid == null || rid.equals(String.valueOf(mainPid))) {
                continue;
            }
            CdrArchiveVO a = new CdrArchiveVO();
            a.setPatientId(parseRowId(rid));
            a.setPatientNo(str(r.getPatientNo()));
            a.setPatientName(str(r.getPatientName()));
            a.setMergeTime(fmt(r.getMergeTime()));
            shadows.add(a);
        }
        vo.setShadowArchives(shadows);
        // 结构化档案表里已有数据的分组，用来修正"文本字段为空"造成的假缺失
        // （详见 PatientProfileFields.applyProfileCoverage 的注释）
        Set<String> covered = new HashSet<>();
        for (CdrProfileRowVO r : profileRows) {
            String k = str(r.getPkey());
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

    private List<CdrProfileGroupVO> toProfile(List<CdrProfileRowVO> rows) {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("allergy", "过敏史");
        labels.put("pastDisease", "既往史");
        labels.put("surgery", "手术史");
        labels.put("family", "家族史");
        labels.put("medication", "用药史");
        labels.put("contact", "联系人");
        Map<String, List<CdrProfileGroupVO.CdrProfileItemVO>> grouped = new LinkedHashMap<>();
        labels.keySet().forEach(k -> grouped.put(k, new ArrayList<>()));
        for (CdrProfileRowVO r : rows) {
            String key = str(r.getPkey());
            if (!grouped.containsKey(key)) {
                continue;
            }
            CdrProfileGroupVO.CdrProfileItemVO item = new CdrProfileGroupVO.CdrProfileItemVO();
            item.setId(parseRowId(r.getSid()));
            item.setTitle(str(r.getTitle()));
            item.setSummary(str(r.getSummary()));
            // 档案日期是 date 列（家族史与联系人天然为 null），补成零点让前端时间轴按同一格式渲染
            item.setTime(fmt(TimeUtil.dayStart(r.getTm())));
            String itemOwner = str(r.getOwnerPid());
            item.setOwnerPatientId(itemOwner == null ? null : Long.valueOf(itemOwner));
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
            if (!TextUtil.hasText(node.getOutcome())) {
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
            if ("inpatientSummary".equals(e.getEventType()) && TextUtil.hasText(e.getTitle())) {
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
        if (TextUtil.hasText(dept)) {
            sb.append(" · ").append(dept);
        }
        if (TextUtil.hasText(doctor)) {
            sb.append(" · ").append(doctor);
        }
        return sb.toString();
    }

    private boolean inRange(String startTime, String from, String to) {
        if (startTime == null) {
            return false;
        }
        String day = startTime.length() >= 10 ? startTime.substring(0, 10) : startTime;
        if (TextUtil.hasText(from) && day.compareTo(from) < 0) {
            return false;
        }
        return !TextUtil.hasText(to) || day.compareTo(to) <= 0;
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