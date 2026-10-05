package com.his.operation.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.exception.BusinessException;
import com.his.operation.dto.AnesthesiaVisitFinishDTO;
import com.his.operation.dto.AnesthesiaVisitQueryPageDTO;
import com.his.operation.dto.AnesthesiaVisitUpsertDTO;
import com.his.operation.entity.BizAnesthesiaVisit;
import com.his.operation.entity.BizOperationApply;
import com.his.operation.mapper.BizAnesthesiaVisitMapper;
import com.his.operation.mapper.BizOperationApplyMapper;
import com.his.operation.service.AnesthesiaVisitService;
import com.his.operation.support.AnesthesiaLabels;
import com.his.operation.support.OperationApplyLabels;
import com.his.operation.vo.AnesthesiaVisitVO;
import com.his.security.entity.CurrentUser;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 麻醉术前访视服务实现（G15 第一环）。
 *
 * <p>本类固化了这些<b>至少踩过一次或一定会被追问</b>的点：
 *
 * <ol>
 *   <li><b>一台手术一份访视</b>：UNIQUE(apply_id) 之外再做一次计数，是为了把
 *       "重复建档"变成人话错误而不是一个 SQL 约束异常（后者会被全局异常渲染成 500）。</li>
 *   <li><b>已完成的访视不能直接改内容</b>：评估结论一旦出账就是麻醉科的正式意见，
 *       要改必须重新走 {@code finish}（留新的时间与新的结论）。</li>
 *   <li><b>困难气道必须写备选方案</b>：不写 ="明知插不上管却没准备"，这是术前访视最有价值的一句。</li>
 *   <li><b>ASA Ⅳ/Ⅴ 级必须写风险说明</b>：这两级意味着围术期风险显著，一句"风险高"不够。</li>
 *   <li><b>未完成访视不能开立麻醉记录</b>（急诊例外）——这条闸门在
 *       {@code AnesthesiaRecordServiceImpl#create} 里，本类只负责提供判定。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnesthesiaVisitServiceImpl implements AnesthesiaVisitService {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BizAnesthesiaVisitMapper visitMapper;
    private final BizOperationApplyMapper applyMapper;

    @Override
    public IPage<AnesthesiaVisitVO> listPage(AnesthesiaVisitQueryPageDTO query) {
        if (query == null) {
            query = new AnesthesiaVisitQueryPageDTO();
        }
        IPage<AnesthesiaVisitVO> page = visitMapper.selectVisitPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        page.getRecords().forEach(this::decorate);
        return page;
    }

    @Override
    public AnesthesiaVisitVO getDetailById(Long visitId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (visitId == null) {
            throw new BusinessException("访视单ID不能为空");
        }
        AnesthesiaVisitVO vo = visitMapper.selectVOById(visitId);
        if (vo == null) {
            throw new BusinessException("麻醉术前访视单不存在");
        }
        decorate(vo);
        return vo;
    }

    @Override
    public AnesthesiaVisitVO getByApply(Long applyId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (applyId == null) {
            throw new BusinessException("手术申请单ID不能为空");
        }
        AnesthesiaVisitVO vo = visitMapper.selectVOByApply(applyId);
        if (vo != null) {
            decorate(vo);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(AnesthesiaVisitUpsertDTO dto) {
        if (dto.getAsaGrade() != null && !AnesthesiaLabels.isValidAsa(dto.getAsaGrade())) {
            throw new BusinessException("ASA 分级取值不合法（应为 1~5），当前=" + dto.getAsaGrade());
        }
        if (dto.getMallampati() != null && !AnesthesiaLabels.isValidMallampati(dto.getMallampati())) {
            throw new BusinessException("Mallampati 分级取值不合法（应为 1~4），当前=" + dto.getMallampati());
        }
        if (dto.getNpoStatus() != null && !AnesthesiaLabels.isValidNpo(dto.getNpoStatus())) {
            throw new BusinessException("禁食禁饮状态取值不合法（应为 0~2），当前=" + dto.getNpoStatus());
        }
        if (dto.getConclusion() != null && !AnesthesiaLabels.isValidVisitConclusion(dto.getConclusion())) {
            throw new BusinessException("访视结论取值不合法（应为 1~3），当前=" + dto.getConclusion());
        }
        validateHighRisk(dto);

        BizOperationApply apply = applyMapper.selectById(dto.getApplyId());
        if (apply == null) {
            throw new BusinessException("手术申请单不存在");
        }
        if (Integer.valueOf(OperationApplyLabels.ST_CANCELLED).equals(apply.getOperationStatus())) {
            throw new BusinessException("手术单 " + apply.getApplyNo() + " 已取消，不需要再访视");
        }

        boolean create = dto.getId() == null;
        BizAnesthesiaVisit entity;
        if (create) {
            if (visitMapper.countByApply(dto.getApplyId()) > 0) {
                throw new BusinessException("该手术已有术前访视单，不能重复建档（一台手术一份评估，重复会打架）");
            }
            entity = new BizAnesthesiaVisit();
            entity.setApplyId(apply.getId());
            entity.setApplyNo(apply.getApplyNo());
            entity.setAdmissionId(apply.getAdmissionId());
            entity.setPatientId(apply.getPatientId());
            entity.setPatientName(apply.getPatientName());
            entity.setGender(apply.getGender());
            entity.setAge(apply.getAge());
            entity.setDiagnosis(apply.getPreopDiagnosis());
            entity.setPlannedOperationCode(apply.getPlannedOperationCode());
            entity.setPlannedOperationName(apply.getPlannedOperationName());
            entity.setOperationLevel(apply.getOperationLevel());
            entity.setAnesthesiaType(apply.getAnesthesiaType());
            entity.setIsEmergency(apply.getIsEmergency() == null ? 0 : apply.getIsEmergency());
            entity.setVisitNo(nextVisitNo());
            entity.setVisitStatus(0);
            entity.setVisitDoctorId(currentEmpId());
            entity.setVisitDoctorName(currentName());
        } else {
            entity = visitMapper.selectById(dto.getId());
            if (entity == null) {
                throw new BusinessException("麻醉术前访视单不存在");
            }
            if (Integer.valueOf(1).equals(entity.getVisitStatus())) {
                throw new BusinessException("访视单 " + entity.getVisitNo()
                        + " 已完成（结论已出账），不能再改内容；请重新完成一份访视");
            }
        }

        entity.setAsaGrade(dto.getAsaGrade());
        entity.setAsaEmergency(dto.getAsaEmergency() == null ? 0 : dto.getAsaEmergency());
        entity.setMallampati(dto.getMallampati());
        entity.setMouthOpenCm(dto.getMouthOpenCm());
        entity.setNeckMobility(dto.getNeckMobility());
        entity.setDifficultAirway(dto.getDifficultAirway() == null ? 0 : dto.getDifficultAirway());
        entity.setAirwayNote(dto.getAirwayNote());
        entity.setPastAnesthesiaHistory(dto.getPastAnesthesiaHistory());
        entity.setAllergyHistory(dto.getAllergyHistory());
        entity.setMedicationHistory(dto.getMedicationHistory());
        entity.setSmokeDrink(dto.getSmokeDrink());
        entity.setNpoStatus(dto.getNpoStatus());
        entity.setHeightCm(dto.getHeightCm());
        entity.setWeightKg(dto.getWeightKg());
        entity.setExamSummary(dto.getExamSummary());
        entity.setAnesthesiaPlan(dto.getAnesthesiaPlan());
        entity.setMonitoringPlan(dto.getMonitoringPlan());
        entity.setRiskAssessment(dto.getRiskAssessment());
        entity.setBackupPlan(dto.getBackupPlan());
        entity.setConclusion(dto.getConclusion());
        entity.setConclusionNote(dto.getConclusionNote());
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
        entity.setVisitDoctorId(currentEmpId());
        entity.setVisitDoctorName(currentName());

        if (create) {
            visitMapper.insert(entity);
        } else {
            visitMapper.updateById(entity);
        }
        log.info("{}麻醉术前访视 visitNo={} applyNo={} ASA={} 困难气道={} 结论={} 访视医师={}",
                create ? "新建" : "修改", entity.getVisitNo(), apply.getApplyNo(),
                AnesthesiaLabels.asaText(entity.getAsaGrade()),
                AnesthesiaLabels.yesNoText(entity.getDifficultAirway()),
                AnesthesiaLabels.visitConclusionText(entity.getConclusion()), currentName());
        return entity.getVisitNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(AnesthesiaVisitFinishDTO dto) {
        if (!AnesthesiaLabels.isValidVisitConclusion(dto.getConclusion())) {
            throw new BusinessException("访视结论取值不合法（应为 1-可施行麻醉 / 2-暂缓手术 / 3-需会诊），当前="
                    + dto.getConclusion());
        }
        BizAnesthesiaVisit entity = visitMapper.selectById(dto.getVisitId());
        if (entity == null) {
            throw new BusinessException("麻醉术前访视单不存在");
        }
        // B-条件必填：结论非「可施行麻醉」时才要求说明，跨字段条件，DTO 注解无法表达，保留
        if (!Objects.equals(1, dto.getConclusion()) && !StringUtils.hasText(dto.getConclusionNote())) {
            throw new BusinessException("结论为「" + AnesthesiaLabels.visitConclusionText(dto.getConclusion())
                    + "」时必须填写结论说明（为什么不能按计划麻醉）");
        }
        if (Integer.valueOf(1).equals(entity.getDifficultAirway()) && !StringUtils.hasText(entity.getBackupPlan())) {
            throw new BusinessException("已标记预计困难气道，必须先填写备选方案才能完成访视");
        }
        entity.setConclusion(dto.getConclusion());
        entity.setConclusionNote(dto.getConclusionNote());
        entity.setVisitStatus(1);
        entity.setVisitDoctorId(currentEmpId());
        entity.setVisitDoctorName(currentName());
        entity.setVisitTime(now());
        visitMapper.updateById(entity);
        log.info("完成麻醉术前访视 visitNo={} 结论={}（{}）访视医师={}",
                entity.getVisitNo(), AnesthesiaLabels.visitConclusionText(dto.getConclusion()),
                StringUtils.hasText(dto.getConclusionNote()) ? dto.getConclusionNote() : "无补充说明", currentName());
    }

    @Override
    public boolean hasApprovedVisit(Long applyId) {
        if (applyId == null) {
            return false;
        }
        AnesthesiaVisitVO vo = visitMapper.selectVOByApply(applyId);
        return vo != null
                && Integer.valueOf(1).equals(vo.getVisitStatus())
                && Integer.valueOf(AnesthesiaLabels.VISIT_CONCLUSION_OK).equals(vo.getConclusion());
    }

    @Override
    public long countFinishedWithoutVisit() {
        return visitMapper.countFinishedWithoutVisit();
    }

    // 展示态

    private void decorate(AnesthesiaVisitVO vo) {
        vo.setAsaText(AnesthesiaLabels.asaText(vo.getAsaGrade()));
        vo.setAsaFullText(AnesthesiaLabels.asaFullText(vo.getAsaGrade(), vo.getAsaEmergency()));
        vo.setMallampatiText(AnesthesiaLabels.mallampatiText(vo.getMallampati()));
        vo.setNeckMobilityText(AnesthesiaLabels.neckMobilityText(vo.getNeckMobility()));
        vo.setNpoText(AnesthesiaLabels.npoText(vo.getNpoStatus()));
        vo.setConclusionText(AnesthesiaLabels.visitConclusionText(vo.getConclusion()));
        vo.setVisitStatusText(AnesthesiaLabels.visitStatusText(vo.getVisitStatus()));
        vo.setDifficultAirwayText(AnesthesiaLabels.yesNoText(vo.getDifficultAirway()));
        vo.setAnesthesiaTypeText(OperationApplyLabels.anesthesiaText(vo.getAnesthesiaType()));
        vo.setEmergencyText(OperationApplyLabels.emergencyText(vo.getIsEmergency()));
        vo.setOperationStatusText(OperationApplyLabels.statusText(vo.getOperationStatus()));
        vo.setBmi(bmi(vo.getHeightCm(), vo.getWeightKg()));

        boolean draft = !Integer.valueOf(1).equals(vo.getVisitStatus());
        vo.setCanEdit(draft);
        vo.setCanFinish(true);
        vo.setCanOpenRecord(Integer.valueOf(1).equals(vo.getVisitStatus())
                && Integer.valueOf(AnesthesiaLabels.VISIT_CONCLUSION_OK).equals(vo.getConclusion()));

        String warn = null;
        if (Integer.valueOf(1).equals(vo.getDifficultAirway()) && !StringUtils.hasText(vo.getBackupPlan())) {
            warn = "已标记预计困难气道，但未填写备选方案";
        } else if (draft) {
            warn = "草稿状态：尚未给出访视结论，不能作为开立麻醉记录的依据";
        } else if (!Integer.valueOf(1).equals(vo.getConclusion())) {
            warn = "访视结论为「" + AnesthesiaLabels.visitConclusionText(vo.getConclusion()) + "」，不可据此开立麻醉记录";
        } else if (Integer.valueOf(2).equals(vo.getNpoStatus())) {
            warn = "急诊饱胃：返流误吸高危，诱导方式需另行评估";
        }
        vo.setWarningText(warn);
    }

    /** BMI（任一项缺失返回 null —— 缺一项就"算不出来"，不编一个数） */
    private BigDecimal bmi(BigDecimal heightCm, BigDecimal weightKg) {
        if (heightCm == null || weightKg == null) {
            return null;
        }
        double h = heightCm.doubleValue() / 100d;
        if (h <= 0) {
            return null;
        }
        return weightKg.divide(BigDecimal.valueOf(h * h), 1, RoundingMode.HALF_UP);
    }

    /**
     * 高风险访视的必填校验。
     *
     * <p>只卡两件事：困难气道要有备选方案、ASA Ⅳ/Ⅴ 要写风险说明。
     * 其余字段一律允许空 —— 术前访视最怕的是"为了过校验把每一项都勾成'正常'"，
     * 少校验一点比催生假数据好。
     */
    private void validateHighRisk(AnesthesiaVisitUpsertDTO dto) {
        // B-条件必填：标记困难气道时才要求备选方案，跨字段条件，DTO 注解无法表达，保留
        if (Integer.valueOf(1).equals(dto.getDifficultAirway()) && !StringUtils.hasText(dto.getBackupPlan())) {
            throw new BusinessException("已标记预计困难气道，必须填写备选方案（备用气道工具/清醒插管/转局麻等）");
        }
        // B-条件必填：ASA≥4 时才要求风险评估，跨字段条件，DTO 注解无法表达，保留
        if (dto.getAsaGrade() != null && dto.getAsaGrade() >= 4
                && !StringUtils.hasText(dto.getRiskAssessment())) {
            throw new BusinessException("ASA " + AnesthesiaLabels.asaText(dto.getAsaGrade())
                    + " 必须填写风险评估（这一级意味着围术期风险显著，不能空着）");
        }
    }

    private String nextVisitNo() {
        String prefix = "MF" + LocalDate.now().format(NO_DATE);
        long seq = visitMapper.countByNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    /** 留痕一律用**员工ID**（不是用户的ID），与医嘱/站内信同一口径 */
    private Long currentEmpId() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user == null) {
                return null;
            }
            return user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private String currentName() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user == null) {
                return null;
            }
            if (StringUtils.hasText(user.getEmployeeName())) {
                return user.getEmployeeName();
            }
            if (StringUtils.hasText(user.getRealName())) {
                return user.getRealName();
            }
            return user.getUsername();
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
