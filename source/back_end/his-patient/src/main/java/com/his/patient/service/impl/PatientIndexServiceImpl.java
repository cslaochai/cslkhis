package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.patient.dto.PatientIndexQueryDTO;
import com.his.patient.dto.PatientMergeDTO;
import com.his.patient.dto.PatientMergeRevertDTO;
import com.his.patient.entity.BizPatient;
import com.his.patient.entity.BizPatientMergeLog;
import com.his.patient.enums.PatientMatchLevelEnum;
import com.his.patient.enums.PatientMergeStatusEnum;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.mapper.BizPatientMergeLogMapper;
import com.his.patient.mapper.PatientIndexMapper;
import com.his.patient.service.PatientIndexService;
import com.his.patient.support.PatientDataTables;
import com.his.common.enums.SysGenderEnum;
import com.his.patient.support.PatientProfileFields;
import com.his.patient.vo.*;
import com.his.system.utils.UserUtils;
import com.his.system.entity.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 患者主索引服务实现（P5.1 EMPI）
 *
 * <p>三条铁律的落地处：**不自动合并、不搬业务数据、可撤销**。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientIndexServiceImpl implements PatientIndexService {

    private static final String NO_PREFIX = "HB";

    private final BizPatientMapper patientMapper;
    private final BizPatientMergeLogMapper mergeLogMapper;
    private final PatientIndexMapper indexMapper;
    private final ObjectMapper objectMapper;

    // 列表 / 详情

    private static double rate(long hit, long total) {
        if (total <= 0) {
            return 0D;
        }
        return Math.round(hit * 1000D / total) / 10D;
    }

    // 重复检测

    @Override
    public PageResult<PatientIndexVO> selectIndexPage(PatientIndexQueryDTO dto) {
        LambdaQueryWrapper<BizPatient> w = new LambdaQueryWrapper<>();
        // 默认只看在册主档：日常查患者时影子档案冒出来，会诱导人去重复建档
        if (!Boolean.TRUE.equals(dto.getIncludeShadow())) {
            w.eq(BizPatient::getMergeStatus, PatientMergeStatusEnum.NORMAL.getCode());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            w.and(q -> q.like(BizPatient::getPatientName, kw)
                    .or().like(BizPatient::getPatientNo, kw)
                    .or().like(BizPatient::getPhone, kw)
                    .or().like(BizPatient::getIdCard, kw));
        }
        w.orderByDesc(BizPatient::getId);
        Page<BizPatient> page = patientMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()), w);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                enrich(page.getRecords()));
    }

    @Override
    public PatientIndexVO getIndexDetail(Long patientId) {
        BizPatient p = patientMapper.selectById(patientId);
        if (p == null) {
            throw new BusinessException("患者不存在");
        }
        PatientIndexVO vo = enrich(List.of(p)).get(0);
        // 同主档下的其他档案：主档看影子，影子看主档，便于"这一串是一个人"
        Long masterId = p.getMasterId() != null ? p.getMasterId() : p.getId();
        LambdaQueryWrapper<BizPatient> w = new LambdaQueryWrapper<>();
        w.eq(BizPatient::getMasterId, masterId).orderByDesc(BizPatient::getId);
        List<BizPatient> shadows = patientMapper.selectList(w);
        vo.setShadowCount(shadows.size());
        vo.setSiblingPatients(shadows.stream().map(this::briefOf).toList());
        return vo;
    }

    // 合并

    /**
     * 详情页用的轻量条目（只要认得出是哪份档案）
     */
    private PatientSiblingVO briefOf(BizPatient p) {
        PatientSiblingVO vo = new PatientSiblingVO();
        vo.setId(p.getId());
        vo.setPatientNo(p.getPatientNo());
        vo.setPatientName(p.getPatientName());
        vo.setGenderText(SysGenderEnum.getText(p.getGender()));
        vo.setIdCard(p.getIdCard());
        vo.setPhone(p.getPhone());
        vo.setMergeStatus(p.getMergeStatus());
        vo.setMergeTime(p.getMergeTime() == null ? null : p.getMergeTime().toString());
        return vo;
    }

    @Override
    public List<PatientDuplicateGroupVO> detectDuplicates(PatientIndexQueryDTO dto) {
        List<BizPatient> suspects = indexMapper.selectSuspectPatients(
                StringUtils.hasText(dto.getKeyword()) ? dto.getKeyword().trim() : null);
        if (suspects.isEmpty()) {
            return List.of();
        }

        // 级别从强到弱依次成组；一个人只进一个组（优先落进更强的组）
        Set<Long> grouped = new HashSet<>();
        List<PatientDuplicateGroupVO> groups = new ArrayList<>();
        int[] levels = {PatientMatchLevelEnum.ID_CARD.getCode(), PatientMatchLevelEnum.NAME_GENDER_BIRTH.getCode(),
                PatientMatchLevelEnum.NAME_PHONE.getCode(), PatientMatchLevelEnum.MANUAL.getCode()};

        for (int level : levels) {
            if (dto.getMatchLevel() != null && dto.getMatchLevel() != level) {
                continue;
            }
            Map<String, List<BizPatient>> byKey = new LinkedHashMap<>();
            for (BizPatient p : suspects) {
                if (grouped.contains(p.getId())) {
                    continue;
                }
                String key = PatientMatchLevelEnum.groupKey(level, p);
                if (key == null) {
                    continue;
                }
                byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
            }
            for (Map.Entry<String, List<BizPatient>> e : byKey.entrySet()) {
                List<BizPatient> members = e.getValue();
                if (members.size() < 2) {
                    continue;
                }
                // 组级别按"组内首员 vs 其余成员"重算一遍再取最强，防止成组键与实际规则漂移
                BizPatient head = members.get(0);
                Integer real = null;
                for (int i = 1; i < members.size(); i++) {
                    real = PatientMatchLevelEnum.stronger(real, PatientMatchLevelEnum.match(head, members.get(i)));
                }
                PatientDuplicateGroupVO g = new PatientDuplicateGroupVO();
                g.setMatchLevel(real);
                g.setMatchLevelText(PatientMatchLevelEnum.text(real));
                g.setMatchEvidence(PatientMatchLevelEnum.matchSnapshot(real, head, members.get(1)));
                g.setGroupKey(e.getKey());
                g.setStrong(PatientMatchLevelEnum.isStrong(real));
                g.setMinReasonLength(PatientMatchLevelEnum.minReasonLength(real));
                g.setTip(tipOf(real));
                g.setMembers(enrich(members));
                // 数据多的排前面：合并时应以"有业务数据的那份"为主档
                g.getMembers().sort((x, y) -> Integer.compare(
                        y.getTotalDataCount() == null ? 0 : y.getTotalDataCount(),
                        x.getTotalDataCount() == null ? 0 : x.getTotalDataCount()));
                groups.add(g);
                members.forEach(m -> grouped.add(m.getId()));
            }
        }

        // 强依据组排前面（更值得处理），同级按组内人数倒序
        groups.sort((x, y) -> {
            int c = Integer.compare(x.getMatchLevel() == null ? 9 : x.getMatchLevel(),
                    y.getMatchLevel() == null ? 9 : y.getMatchLevel());
            if (c != 0) {
                return c;
            }
            return Integer.compare(y.getMembers().size(), x.getMembers().size());
        });
        return groups;
    }

    // 归并（CDR 等按患者聚合的地方必须走这里）

    /**
     * 每个级别的操作提示。措辞直接反映实测结论，不给"应该可以合并"的错觉
     */
    private String tipOf(Integer level) {
        if (level == null) {
            return "—";
        }
        PatientMatchLevelEnum item = PatientMatchLevelEnum.fromCode(level);
        // 脏码值按最可疑的人工判定档处理，不允许被升成可信档
        return switch (item == null ? PatientMatchLevelEnum.MANUAL : item) {
            case ID_CARD -> "身份证号完全一致，基本可确认是同一人。合并前请确认两条记录都不是他人证件误录。";
            case NAME_GENDER_BIRTH -> "姓名、性别、出生日期一致，但**没有身份证号佐证**。同名同生日在真实人群里并不罕见，"
                    + "请核对证件原件后再决定是否合并。";
            case NAME_PHONE -> "仅凭姓名 + 手机号不足以认定同一人：实测本院存在多人共用同一手机号建档的情况"
                    + "（一个号码下挂 10 个患者）。默认不应合并。";
            case MANUAL -> "仅姓名相同，其余关键字段均不一致。默认**不应合并** —— 除非已核对证件确认"
                    + "是同一人的重复建档，否则这很可能是两个同名的人。";
        };
    }

    // 合并历史 / 概览

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientMergeLogVO merge(PatientMergeDTO dto) {
        if (dto.getMasterId().equals(dto.getMergedId())) {
            throw new BusinessException("不能把一份档案合并到它自己");
        }
        BizPatient master = patientMapper.selectById(dto.getMasterId());
        BizPatient merged = patientMapper.selectById(dto.getMergedId());
        if (master == null || merged == null) {
            throw new BusinessException("主档或被并档案不存在（可能已被删除）");
        }
        if (isShadow(merged)) {
            throw new BusinessException("档案 " + merged.getPatientNo() + " 已并入其它主档，不能重复合并");
        }
        if (isShadow(master)) {
            throw new BusinessException("主档 " + master.getPatientNo() + " 本身已是影子档案，"
                    + "不能作为合并目标 —— 请直接把档案合并到它所属的主档");
        }

        // 服务端自己定级，不采信前端传的值
        Integer level = PatientMatchLevelEnum.match(master, merged);
        if (dto.getMatchType() != null && !Objects.equals(dto.getMatchType(), level)) {
            log.warn("合并时前端传来的匹配级别与服务端判定不一致，以服务端为准。前端={} 服务端={} master={} merged={}",
                    dto.getMatchType(), level, master.getPatientNo(), merged.getPatientNo());
        }
        int minLen = PatientMatchLevelEnum.minReasonLength(level);
        // 保留（类别①条件必填）：合并理由的最小字数由服务端判级动态决定（级别越弱要求越长），
        // 阈值不在入参里，@Size 写不出来
        if (!StringUtils.hasText(dto.getReason()) || dto.getReason().trim().length() < minLen) {
            throw new BusinessException("合并理由至少 " + minLen + " 个字（当前级别："
                    + PatientMatchLevelEnum.text(level) + "）。级别越弱，越要写清是谁、依据什么核实的");
        }

        CurrentUser user = UserUtils.getCurrentUser();
        LocalDateTime now = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

        // 数据量快照：合并前后"这个档案名下有多少业务数据"必须有纸面记录
        Map<Long, Map<String, Integer>> counts = loadDataCounts(List.of(master.getId(), merged.getId()));
        String dataCountJson = toJson(Map.of(
                "master", counts.getOrDefault(master.getId(), Map.of()),
                "merged", counts.getOrDefault(merged.getId(), Map.of())));

        String masterSnapshot = toJson(snapshot(master));
        String mergedSnapshot = toJson(snapshot(merged));

        // 可选：把被并档"有值而主档为空"的关键字段补全到主档（默认不做）
        List<String> filledFields = new ArrayList<>();
        if (Boolean.TRUE.equals(dto.getFillBlank())) {
            filledFields = fillBlankFields(master, merged);
        }

        // 唯一的写动作：影子指向主档 + 在册状态失效
        merged.setMasterId(master.getId());
        merged.setMergeStatus(PatientMergeStatusEnum.MERGED.getCode());
        merged.setMergeTime(now);
        // 停用：否则它还能被挂号/开单选中，等于合并没生效
        merged.setStatus(0);
        patientMapper.updateById(merged);
        if (!filledFields.isEmpty()) {
            patientMapper.updateById(master);
        }

        BizPatientMergeLog logEntity = new BizPatientMergeLog();
        logEntity.setMergeNo(nextMergeNo());
        logEntity.setMasterId(master.getId());
        logEntity.setMasterNo(master.getPatientNo());
        logEntity.setMasterName(master.getPatientName());
        logEntity.setMergedId(merged.getId());
        logEntity.setMergedNo(merged.getPatientNo());
        logEntity.setMergedName(merged.getPatientName());
        logEntity.setMatchType(level);
        logEntity.setMatchSnapshot(PatientMatchLevelEnum.matchSnapshot(level, master, merged));
        logEntity.setMasterSnapshot(masterSnapshot);
        logEntity.setMergedSnapshot(mergedSnapshot);
        logEntity.setDataCount(dataCountJson);
        logEntity.setReason(dto.getReason().trim());
        logEntity.setOperatorId(user == null ? null : user.getEmployeeId());
        logEntity.setOperatorName(user == null ? null : user.getEmployeeName());
        logEntity.setMergeTime(now);
        logEntity.setLogStatus(1);
        mergeLogMapper.insert(logEntity);

        log.info("患者档案合并 master={}({}) 合并 merged={}({}) 级别={} 补全字段={} 操作人={}",
                master.getPatientNo(), master.getId(), merged.getPatientNo(), merged.getId(),
                PatientMatchLevelEnum.text(level), filledFields, logEntity.getOperatorName());
        return toLogVO(logEntity, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientMergeLogVO revert(PatientMergeRevertDTO dto) {
        BizPatientMergeLog logEntity = mergeLogMapper.selectById(dto.getLogId());
        if (logEntity == null) {
            throw new BusinessException("合并记录不存在");
        }
        if (!Objects.equals(logEntity.getLogStatus(), 1)) {
            throw new BusinessException("该合并记录已撤销，不能重复撤销");
        }
        BizPatient merged = patientMapper.selectById(logEntity.getMergedId());
        if (merged == null) {
            throw new BusinessException("被并档案不存在，无法撤销");
        }
        if (!Objects.equals(merged.getMasterId(), logEntity.getMasterId())
                || !Objects.equals(merged.getMergeStatus(), PatientMergeStatusEnum.MERGED.getCode())) {
            throw new BusinessException("档案当前的主索引状态已被改动，不能按这条记录撤销");
        }

        // 用快照还原在册状态：原来停用的档案不能因为撤销合并就被启用
        Integer originalStatus = readIntFromJson(logEntity.getMergedSnapshot(), "status");
        // 【必须用 UpdateWrapper 显式 set null】MyBatis-Plus 的 updateById 默认策略是
        // NOT_NULL —— 只 set 非空字段。写成 merged.setMasterId(null) + updateById，
        // master_id 根本不会被写进 SQL，于是"撤销"看起来成功（返回 200、log 也标了已撤销），
        // 但库里 master_id 仍然指着主档，患者还是影子。这类静默不生效只有回读数据库才能发现。
        patientMapper.update(new BizPatient(), new LambdaUpdateWrapper<BizPatient>()
                .set(BizPatient::getMasterId, null)
                .set(BizPatient::getMergeStatus, PatientMergeStatusEnum.NORMAL.getCode())
                .set(BizPatient::getMergeTime, null)
                .set(BizPatient::getStatus, originalStatus != null ? originalStatus : 1)
                .eq(BizPatient::getId, merged.getId()));

        logEntity.setLogStatus(2);
        logEntity.setRevertBy(UserUtils.getCurrentUser().getRealName());
        logEntity.setRevertTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        logEntity.setRevertReason(dto.getRevertReason().trim());
        mergeLogMapper.updateById(logEntity);

        log.info("撤销患者档案合并 mergeNo={} merged={} 理由={}",
                logEntity.getMergeNo(), merged.getPatientNo(), dto.getRevertReason());
        return toLogVO(logEntity, false);
    }

    // 内部工具

    @Override
    public List<Long> resolvePatientIds(Long patientId) {
        if (patientId == null) {
            return List.of();
        }
        BizPatient p = patientMapper.selectById(patientId);
        if (p == null) {
            return List.of(patientId);
        }
        Long masterId = p.getMasterId() != null ? p.getMasterId() : p.getId();
        LambdaQueryWrapper<BizPatient> w = new LambdaQueryWrapper<>();
        w.and(q -> q.eq(BizPatient::getId, masterId).or().eq(BizPatient::getMasterId, masterId));
        List<BizPatient> all = patientMapper.selectList(w);
        Set<Long> ids = new LinkedHashSet<>();
        ids.add(masterId);
        all.forEach(x -> ids.add(x.getId()));
        return new ArrayList<>(ids);
    }

    @Override
    public PageResult<PatientMergeLogVO> selectMergeLogPage(PatientIndexQueryDTO dto) {
        LambdaQueryWrapper<BizPatientMergeLog> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            w.and(q -> q.like(BizPatientMergeLog::getMergeNo, kw)
                    .or().like(BizPatientMergeLog::getMasterName, kw)
                    .or().like(BizPatientMergeLog::getMergedName, kw)
                    .or().like(BizPatientMergeLog::getMasterNo, kw)
                    .or().like(BizPatientMergeLog::getMergedNo, kw));
        }
        w.orderByDesc(BizPatientMergeLog::getMergeTime);
        Page<BizPatientMergeLog> page = mergeLogMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), w);
        List<PatientMergeLogVO> vos = page.getRecords().stream().map(x -> toLogVO(x, true)).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    public PatientIndexStatVO stats() {
        // 6 个标量子查询恒返回一行且 COUNT(*) 不会为 null，直接取值
        PatientIndexCountVO raw = indexMapper.selectIndexStats();
        long total = raw.getPatientTotal();
        long strongDup = raw.getStrongDupGroups();
        long merged = raw.getMergedCount();
        long idCardMissing = raw.getIdCardMissing();
        long phoneMissing = raw.getPhoneMissing();
        long allergyMissing = raw.getAllergyMissing();

        PatientIndexStatVO vo = new PatientIndexStatVO();
        vo.setPatientTotal(total);
        vo.setMergedCount(merged);
        vo.setStrongDupGroups(strongDup);
        vo.setIdCardMissing(idCardMissing);
        vo.setPhoneMissing(phoneMissing);
        vo.setAllergyMissing(allergyMissing);
        // 唯一性：身份证重复组不超标才算过（口径见 Mapper 注释）
        vo.setUniqueRate(rate(total - strongDup, total));
        vo.setIdCardCompleteRate(rate(total - idCardMissing, total));
        vo.setPhoneCompleteRate(rate(total - phoneMissing, total));
        vo.setAllergyCompleteRate(rate(total - allergyMissing, total));
        // 合并动作本身也是"唯一性治理"的进展指标
        vo.setMergeActions(mergeLogMapper.selectCount(null));
        return vo;
    }

    /**
     * 批量补齐完整度 / 数据量 / 主档信息（禁止逐条查，列表页会退化成 N+1）
     */
    private List<PatientIndexVO> enrich(List<BizPatient> patients) {
        if (patients == null || patients.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = patients.stream().map(BizPatient::getId).toList();
        Map<Long, Map<String, Integer>> dataMap = loadDataCounts(ids);
        Map<Long, Integer> shadowMap = loadShadowCounts(ids);
        Map<Long, BizPatient> masterMap = loadMasters(patients);

        List<PatientIndexVO> out = new ArrayList<>(patients.size());
        for (BizPatient p : patients) {
            PatientIndexVO vo = new PatientIndexVO();
            BeanUtils.copyProperties(p, vo);
            vo.setGenderText(SysGenderEnum.getText(p.getGender()));

            PatientProfileFields.ProfileScore sc = PatientProfileFields.score(p);
            vo.setCompleteCount(sc.completeCount());
            vo.setTotalFieldCount(sc.totalCount());
            vo.setCompleteRate(sc.rate());
            vo.setMissingFields(sc.missingFields());

            Map<String, Integer> counts = dataMap.getOrDefault(p.getId(), Map.of());
            List<PatientIndexVO.DataCountItem> items = new ArrayList<>();
            int sum = 0;
            for (Map.Entry<String, String> e : PatientDataTables.LABELS.entrySet()) {
                int c = counts.getOrDefault(e.getKey(), 0);
                sum += c;
                PatientIndexVO.DataCountItem item = new PatientIndexVO.DataCountItem();
                item.setKey(e.getKey());
                item.setLabel(e.getValue());
                item.setCount(c);
                items.add(item);
            }
            vo.setDataCounts(items);
            vo.setTotalDataCount(sum);

            if (p.getMasterId() != null) {
                BizPatient m = masterMap.get(p.getMasterId());
                if (m != null) {
                    vo.setMasterNo(m.getPatientNo());
                    vo.setMasterName(m.getPatientName());
                }
            }
            vo.setShadowCount(shadowMap.getOrDefault(p.getId(), 0));
            boolean shadow = isShadow(p);
            vo.setCanMerge(!shadow);
            vo.setCanBeMaster(!shadow && p.getMasterId() == null);
            out.add(vo);
        }
        return out;
    }

    private Map<Long, Map<String, Integer>> loadDataCounts(Collection<Long> ids) {
        Map<Long, Map<String, Integer>> m = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return m;
        }
        for (PatientDataCountVO row : indexMapper.countDataByPatientIds(ids)) {
            if (row.getPatientId() == null || row.getDataTable() == null) {
                continue;
            }
            long cnt = row.getCnt() == null ? 0L : row.getCnt();
            m.computeIfAbsent(row.getPatientId(), x -> new LinkedHashMap<>())
                    .put(row.getDataTable(), (int) cnt);
        }
        return m;
    }

    private Map<Long, Integer> loadShadowCounts(Collection<Long> ids) {
        Map<Long, Integer> m = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return m;
        }
        LambdaQueryWrapper<BizPatient> w = new LambdaQueryWrapper<>();
        w.select(BizPatient::getMasterId).in(BizPatient::getMasterId, ids);
        for (BizPatient s : patientMapper.selectList(w)) {
            m.merge(s.getMasterId(), 1, Integer::sum);
        }
        return m;
    }

    private Map<Long, BizPatient> loadMasters(List<BizPatient> patients) {
        Map<Long, BizPatient> m = new HashMap<>();
        List<Long> masterIds = patients.stream()
                .map(BizPatient::getMasterId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (masterIds.isEmpty()) {
            return m;
        }
        for (BizPatient p : patientMapper.selectBatchIds(masterIds)) {
            m.put(p.getId(), p);
        }
        return m;
    }

    private boolean isShadow(BizPatient p) {
        return p != null && Objects.equals(p.getMergeStatus(), PatientMergeStatusEnum.MERGED.getCode());
    }

    /**
     * 合并时可选把被并档的字段补进主档空位；返回补了哪些字段（中文名）
     */
    private List<String> fillBlankFields(BizPatient master, BizPatient merged) {
        List<String> filled = new ArrayList<>();
        if (!StringUtils.hasText(master.getIdCard()) && StringUtils.hasText(merged.getIdCard())) {
            master.setIdCard(merged.getIdCard());
            filled.add("身份证号");
        }
        if (!StringUtils.hasText(master.getPhone()) && StringUtils.hasText(merged.getPhone())) {
            master.setPhone(merged.getPhone());
            filled.add("手机号");
        }
        if (master.getBirthDate() == null && merged.getBirthDate() != null) {
            master.setBirthDate(merged.getBirthDate());
            filled.add("出生日期");
        }
        if (!StringUtils.hasText(master.getAddress()) && StringUtils.hasText(merged.getAddress())) {
            master.setAddress(merged.getAddress());
            filled.add("家庭住址");
        }
        if (!StringUtils.hasText(master.getAllergyHistory()) && StringUtils.hasText(merged.getAllergyHistory())) {
            master.setAllergyHistory(merged.getAllergyHistory());
            filled.add("过敏史");
        }
        if (!StringUtils.hasText(master.getMedicalHistory()) && StringUtils.hasText(merged.getMedicalHistory())) {
            master.setMedicalHistory(merged.getMedicalHistory());
            filled.add("既往病史");
        }
        if (!StringUtils.hasText(master.getBloodType()) && StringUtils.hasText(merged.getBloodType())) {
            master.setBloodType(merged.getBloodType());
            filled.add("血型");
        }
        if (!StringUtils.hasText(master.getMedicalInsuranceType()) && StringUtils.hasText(merged.getMedicalInsuranceType())) {
            master.setMedicalInsuranceType(merged.getMedicalInsuranceType());
            filled.add("医保类型");
        }
        return filled;
    }

    /**
     * 档案关键字段快照（撤销时靠它还原，不靠猜）
     */
    private PatientMergeSnapshotVO snapshot(BizPatient p) {
        PatientMergeSnapshotVO vo = new PatientMergeSnapshotVO();
        vo.setId(p.getId());
        vo.setPatientNo(p.getPatientNo());
        vo.setPatientName(p.getPatientName());
        vo.setGender(p.getGender());
        vo.setBirthDate(p.getBirthDate());
        vo.setIdCard(p.getIdCard());
        vo.setPhone(p.getPhone());
        vo.setStatus(p.getStatus());
        vo.setMergeStatus(p.getMergeStatus());
        return vo;
    }

    private String nextMergeNo() {
        String prefix = NO_PREFIX + LocalDateTime.now().format(DateFormats.COMPACT_DATE);
        long n = mergeLogMapper.countByNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", n % 10000);
    }

    private PatientMergeLogVO toLogVO(BizPatientMergeLog e, boolean computeCanRevert) {
        PatientMergeLogVO vo = new PatientMergeLogVO();
        BeanUtils.copyProperties(e, vo);
        vo.setMatchTypeText(PatientMatchLevelEnum.text(e.getMatchType()));
        vo.setLogStatusText(Objects.equals(e.getLogStatus(), 1) ? "已合并" : "已撤销");
        if (computeCanRevert) {
            fillCanRevert(e, vo);
        } else {
            vo.setCanRevert(false);
        }
        return vo;
    }

    private void fillCanRevert(BizPatientMergeLog e, PatientMergeLogVO vo) {
        if (!Objects.equals(e.getLogStatus(), 1)) {
            vo.setCanRevert(false);
            return;
        }
        BizPatient merged = patientMapper.selectById(e.getMergedId());
        vo.setCanRevert(merged != null
                && Objects.equals(merged.getMasterId(), e.getMasterId())
                && Objects.equals(merged.getMergeStatus(), PatientMergeStatusEnum.MERGED.getCode()));
    }

    private Integer readIntFromJson(String json, String field) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(json).get(field);
            return node == null || node.isNull() ? null : node.asInt();
        } catch (Exception ex) {
            log.warn("解析档案快照失败，撤销时将使用默认在册状态。json={}", json, ex);
            return null;
        }
    }

    private String toJson(Object o) {
        try {
            if (o instanceof Map<?, ?> map) {
                ObjectNode node = objectMapper.createObjectNode();
                map.forEach((k, v) -> node.set(String.valueOf(k), objectMapper.valueToTree(v)));
                return objectMapper.writeValueAsString(node);
            }
            return objectMapper.writeValueAsString(o);
        } catch (Exception ex) {
            throw new BusinessException("生成审计快照失败，已中止操作（审计不完整不允许落库）");
        }
    }

    @Override
    public PatientIndexDictVO dict() {
        List<PatientMatchLevelSelectListVO> levels = new ArrayList<>();
        for (int code : new int[]{PatientMatchLevelEnum.ID_CARD.getCode(), PatientMatchLevelEnum.NAME_GENDER_BIRTH.getCode(),
                PatientMatchLevelEnum.NAME_PHONE.getCode(), PatientMatchLevelEnum.MANUAL.getCode()}) {
            PatientMatchLevelSelectListVO item = new PatientMatchLevelSelectListVO();
            item.setCode(code);
            item.setText(PatientMatchLevelEnum.text(code));
            item.setStrong(PatientMatchLevelEnum.isStrong(code));
            item.setMinReasonLength(PatientMatchLevelEnum.minReasonLength(code));
            levels.add(item);
        }
        PatientIndexDictVO vo = new PatientIndexDictVO();
        vo.setMatchLevels(levels);
        vo.setProfileFields(PatientProfileFields.FIELDS);
        return vo;
    }
}
