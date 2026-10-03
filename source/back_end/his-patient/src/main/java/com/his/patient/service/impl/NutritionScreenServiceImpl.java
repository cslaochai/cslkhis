package com.his.patient.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.patient.entity.BizPatient;
import com.his.patient.entity.BizAdmission;
import com.his.patient.entity.SysBed;
import com.his.patient.mapper.BizAdmissionMapper;
import com.his.patient.mapper.SysBedMapper;
import com.his.patient.support.NutritionRules;
import com.his.patient.dto.NutritionScreenQueryPageDTO;
import com.his.patient.dto.NutritionScreenUpsertDTO;
import com.his.patient.entity.BizNutritionScreen;
import com.his.patient.mapper.BizNutritionScreenMapper;
import com.his.patient.mapper.NutritionStatMapper;
import com.his.patient.service.NutritionScreenService;
import com.his.patient.vo.NutritionScreenVO;
import com.his.patient.vo.WardVO;
import com.his.patient.mapper.BizPatientMapper;
import com.his.security.DeptScopeGuard;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.his.patient.enums.NutritionScreenTypeEnum;

/**
 * 营养风险筛查实现。
 *
 * <p>三处必须防的"凑数"：
 * <ol>
 *   <li>总分与判定服务端算 —— 判定决定要不要干预，不能由请求体说了算；</li>
 *   <li>年龄项按患者真实年龄算，不许前端想加就加（加 1 分就可能从 2 分变 3 分"有风险"，
 *       也可能反过来被人抹掉）；</li>
 *   <li>判阴性必须留复筛日期 —— 漏筛最常见的形态是"入院筛过一次就再也没筛"。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionScreenServiceImpl implements NutritionScreenService {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String PREFIX_SCREEN = "NS";

    private final BizNutritionScreenMapper screenMapper;
    private final BizAdmissionMapper admissionMapper;
    private final BizPatientMapper patientMapper;
    private final SysBedMapper bedMapper;
    private final NutritionStatMapper statMapper;

    @Override
    public PageResult<NutritionScreenVO> screenListPage(NutritionScreenQueryPageDTO query) {
        query.setKeyword(trim(query.getKeyword()));
        applyDeptScope(query);
        Page<NutritionScreenVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<NutritionScreenVO> result = (Page<NutritionScreenVO>) screenMapper.selectScreenPage(page, query);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(),
                result.getRecords());
    }

    @Override
    public List<NutritionScreenVO> screenListByAdmission(Long admissionId) {
        // ②非web入口：service 入参守卫，GET 的 @RequestParam 没有 DTO 字段可挂注解（Spring 侧本身必填）
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        return screenMapper.selectByAdmission(admissionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NutritionScreenVO screenUpsert(NutritionScreenUpsertDTO dto) {
        Integer type = dto.getScreenType();
        // ③业务规则：量表码值合法性（非空与 1~3 已由 DTO @NotNull/@Min/@Max + @Valid 收口，此处兜内部调用）
        if (type == null || type < NutritionScreenTypeEnum.NRS2002.getCode() || type > NutritionScreenTypeEnum.MNA.getCode()) {
            throw new BusinessException("筛查量表取值不合法（1-NRS2002 2-PG-SGA 3-MNA）");
        }
        // ①条件必填：选了 NRS2002 才必填两个分项评分，换别的量表就必填总分，@NotNull 会误伤另一种量表
        if (type == NutritionScreenTypeEnum.NRS2002.getCode()
                && (dto.getImpairScore() == null || dto.getSeverityScore() == null)) {
            throw new BusinessException("NRS2002 必须填写营养状态受损评分与疾病严重程度评分");
        }
        if (type != NutritionScreenTypeEnum.NRS2002.getCode() && dto.getTotalScore() == null) {
            throw new BusinessException(NutritionRules.screenTypeText(type) + " 需提交评定总分");
        }

        BizAdmission admission = admissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        BizPatient patient = admission.getPatientId() == null ? null
                : patientMapper.selectById(admission.getPatientId());

        // 年龄项只认患者档案里的真实年龄：≥70 岁 1 分，其余 0 分
        int ageScore = patient != null && patient.getAge() != null && patient.getAge() >= 70 ? 1 : 0;
        int total = NutritionRules.totalScore(type, dto.getImpairScore(), dto.getSeverityScore(),
                ageScore, dto.getTotalScore());
        int risk = NutritionRules.riskFlag(type, total);
        if (type == NutritionScreenTypeEnum.NRS2002.getCode() && (total < 0 || total > 7)) {
            throw new BusinessException("NRS2002 总分应在 0~7 之间，请核对分项评分");
        }

        LocalDateTime screenTime = toSeconds(dto.getScreenTime() == null ? LocalDateTime.now() : dto.getScreenTime());
        LocalDate nextScreenDate = resolveNextScreenDate(type, risk, screenTime, dto.getNextScreenDate());

        BizNutritionScreen row;
        boolean insert = dto.getId() == null;
        if (insert) {
            row = new BizNutritionScreen();
            row.setScreenNo(nextNo(PREFIX_SCREEN, screenMapper.maxScreenSeq(PREFIX_SCREEN
                    + (screenTime == null ? LocalDate.now() : screenTime.toLocalDate()).format(DAY_FMT))));
        } else {
            row = screenMapper.selectById(dto.getId());
            if (row == null) {
                throw new BusinessException("筛查记录不存在或已删除");
            }
        }

        row.setAdmissionId(admission.getAdmissionId());
        row.setPatientId(admission.getPatientId());
        row.setPatientNo(patient == null ? null : patient.getPatientNo());
        row.setPatientName(patient == null ? null : patient.getPatientName());
        row.setDeptId(admission.getDeptId());
        row.setDeptName(admission.getDeptId() == null ? null : statMapper.selectDeptName(admission.getDeptId()));
        row.setWardId(admission.getWardId());
        row.setWardName(wardName(admission.getWardId()));
        row.setBedNo(bedNo(admission.getBedId()));
        row.setScreenType(type);
        row.setImpairScore(type == NutritionScreenTypeEnum.NRS2002.getCode() ? dto.getImpairScore() : null);
        row.setSeverityScore(type == NutritionScreenTypeEnum.NRS2002.getCode() ? dto.getSeverityScore() : null);
        row.setAgeScore(type == NutritionScreenTypeEnum.NRS2002.getCode() ? ageScore : null);
        row.setHeightCm(dto.getHeightCm());
        row.setWeightKg(dto.getWeightKg());
        row.setBmi(NutritionRules.bmiOf(dto.getHeightCm(), dto.getWeightKg()));
        row.setWeightLossPercent(dto.getWeightLossPercent());
        row.setTotalScore(total);
        row.setRiskFlag(risk);
        row.setScreenSource(dto.getScreenSource());
        row.setNextScreenDate(nextScreenDate);
        row.setItemsJson(StringUtils.hasText(dto.getItemsJson()) ? dto.getItemsJson().trim() : null);
        row.setScreenTime(screenTime);
        row.setScreenerId(UserUtils.getCurrentEmployeeId());
        row.setScreenerName(currentName());
        row.setRemark(cut(trim(dto.getRemark()), 500));

        if (insert) {
            screenMapper.insert(row);
            log.info("营养筛查登记 住院={} 量表={} 总分={} 判定={} 操作人={}", admission.getAdmissionNo(),
                    type, total, risk, currentName());
        } else {
            screenMapper.updateById(row);
            log.info("营养筛查修改 id={} 总分={} 判定={} 操作人={}", row.getId(), total, risk, currentName());
        }
        return screenMapper.selectVoById(row.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int screenDeleteById(Long id) {
        BizNutritionScreen row = screenMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("筛查记录不存在或已删除");
        }
        return screenMapper.deleteById(id);
    }

    // 内部

    /**
     * 复筛日期：NRS2002 判阳性 → 不再排复筛（走干预：膳食医嘱/营养会诊）；
     * 判阴性 → 提交值优先，否则筛查日 +7 天。PG-SGA/MNA 是评定不是筛查，沿用提交值。
     */
    private LocalDate resolveNextScreenDate(Integer type, int risk, LocalDateTime screenTime, LocalDate submitted) {
        if (type != NutritionScreenTypeEnum.NRS2002.getCode()) {
            return submitted;
        }
        if (risk == YesOrNoEnum.YES.getCode()) {
            return null;
        }
        if (submitted != null) {
            return submitted;
        }
        LocalDate base = screenTime == null ? LocalDate.now() : screenTime.toLocalDate();
        return base.plusDays(NutritionRules.RE_SCREEN_DAYS);
    }

    /** 科室数据权限收口：受限岗位只看得到授权科室的筛查（营养师 data_scope=1 全院，不受限） */
    private void applyDeptScope(NutritionScreenQueryPageDTO query) {
        Set<Long> allowed = DeptScopeGuard.allowedDeptIds();
        if (allowed != null) {
            query.setScopeDeptIds(new ArrayList<>(allowed));
        }
    }

    private String wardName(Long wardId) {
        if (wardId == null) {
            return null;
        }
        WardVO ward = bedMapper.selectWardById(wardId);
        return ward == null ? null : ward.getWardName();
    }

    private String bedNo(Long bedId) {
        if (bedId == null) {
            return null;
        }
        SysBed bed = bedMapper.selectById(bedId);
        return bed == null ? null : bed.getBedNo();
    }

    private String currentName() {
        String name = UserUtils.getCurrentEmployeeName();
        if (StringUtils.hasText(name)) {
            return name;
        }
        Long empId = UserUtils.getCurrentEmployeeId();
        return empId == null ? "system" : String.valueOf(empId);
    }

    private String nextNo(String prefix, long maxSeq) {
        return prefix + LocalDate.now().format(DAY_FMT) + String.format("%04d", maxSeq + 1);
    }

    private static LocalDateTime toSeconds(LocalDateTime time) {
        return time == null ? null : time.truncatedTo(ChronoUnit.SECONDS);
    }

    private static String trim(String v) {
        return v == null ? null : v.trim();
    }

    /** 入库前截到列宽：超长文本让 insert 报 Data too long 会把"备注太长"升级成 500 */
    private static String cut(String v, int max) {
        if (v == null) {
            return null;
        }
        return v.length() <= max ? v : v.substring(0, max);
    }
}
