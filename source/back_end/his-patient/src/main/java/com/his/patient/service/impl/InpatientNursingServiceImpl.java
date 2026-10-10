package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.constant.DictType;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.*;
import com.his.patient.entity.*;
import com.his.patient.enums.*;
import com.his.patient.mapper.*;
import com.his.patient.service.InpatientNursingService;
import com.his.patient.vo.*;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeService;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 护理文书服务实现（三测单 / 护理记录单 / 生命体征监测）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientNursingServiceImpl implements InpatientNursingService {

    private final RedisSequenceService redisSequenceService;
    private static final BigDecimal MIN_TEMP = new BigDecimal("34");
    private static final BigDecimal MAX_TEMP = new BigDecimal("43");

    private final DeptScopeService deptScopeService;

    private final BizNursingRecordMapper bizNursingRecordMapper;

    private final BizInpatientRecordLogMapper bizInpatientRecordLogMapper;

    private final BizAdmissionMapper bizAdmissionMapper;

    private final BizPatientMapper bizPatientMapper;

    private final SysBedMapper sysBedMapper;

    private final BizNursingAssessmentMapper bizNursingAssessmentMapper;

    private final ObjectMapper objectMapper;

    private final DictCacheService dictCacheService;

    // 录入 / 修改

    /**
     * 量表分数段 → 风险等级（唯一口径；前端 lib/nursingAssessment.js 与此一致，冲突以后端为准）
     */
    private static int riskLevelOf(int assessType, int score) {
        return switch (assessType) {
            // Braden 6~23：分数越低压疮风险越高
            case 1 -> score <= 9 ? 4 : score <= 12 ? 3 : score <= 14 ? 2 : 1;
            // Morse 0~125：≥45 高风险（无极高档）
            case 2 -> score >= 45 ? 3 : score >= 25 ? 2 : 1;
            // NRS 0~10：0~3 轻度 / 4~6 中度 / 7~10 重度
            case 3 -> score >= 7 ? 3 : score >= 4 ? 2 : 1;
            // Caprini 0~58（全部勾选理论上限，年龄三档临床互斥）：0~2 低 / 3~4 中 / 5~6 高 / ≥7 极高（sql/159）
            case 4 -> score >= 7 ? 4 : score >= 5 ? 3 : score >= 3 ? 2 : 1;
            // 管路滑脱 0~24：0~3 低 / 4~7 中 / 8~11 高 / ≥12 极高（sql/159）
            case 5 -> score >= 12 ? 4 : score >= 8 ? 3 : score >= 4 ? 2 : 1;
            // 码值合法性由 DTO 的 @InEnum(NursingAssessTypeEnum) 兜住，走到这里必是 1~5 之一；
            // 兜底不返回 0（risk_level 合法值是 1~4，返回 0 等于静默写脏数据）
            default -> throw new IllegalStateException("评估类型无分档口径：" + assessType);
        };
    }

    private static String asText(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd.stripTrailingZeros().toPlainString();
        }
        return String.valueOf(v);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NursingRecordVO save(NursingRecordUpsertDTO dto) {
        if (dto.getId() == null) {
            return create(dto);
        }
        return update(dto);
    }

    private NursingRecordVO create(NursingRecordUpsertDTO dto) {
        // B-条件必填：save 按 dto.id 分新增/修改，这三项只在新增分支必填，修改分支可省略；挂 @NotNull 会把合法的修改请求挡成 400，保留
        if (dto.getAdmissionId() == null) {
            throw new BusinessException("入院ID不能为空");
        }
        // B-条件必填：同上，护理文书类型仅新增分支必填，修改分支可省略，DTO 一刀切 @NotNull 会挡掉合法修改，保留
        if (dto.getNursingType() == null) {
            throw new BusinessException("护理文书类型不能为空");
        }
        // B-条件必填：同上，测量/记录时间仅新增分支必填（三测单按时点唯一），修改分支可省略，保留
        if (dto.getMeasureTime() == null) {
            throw new BusinessException("测量/记录时间不能为空（三测单按时点唯一，时间是它的主键语义）");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizAdmission admission = bizAdmissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        BizPatient patient = bizPatientMapper.selectById(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        validateContent(dto);

        BizNursingRecord record = new BizNursingRecord();
        record.setRecordNo(nextRecordNo());
        record.setAdmissionId(admission.getAdmissionId());
        record.setPatientId(admission.getPatientId());
        record.setPatientNo(patient.getPatientNo());
        record.setPatientName(patient.getPatientName());

        Long deptId = admission.getDeptId();
        String deptName = null;
        String wardName = null;
        if (admission.getWardId() != null) {
            WardVO ward = sysBedMapper.selectWardById(admission.getWardId());
            if (ward != null) {
                wardName = ward.getWardName();
                deptName = ward.getDeptName();
                if (deptId == null) {
                    deptId = ward.getDeptId();
                }
            }
        }
        record.setDeptId(deptId);
        record.setDeptName(deptName);
        record.setWardId(admission.getWardId());
        record.setWardName(wardName);
        if (admission.getBedId() != null) {
            SysBed bed = sysBedMapper.selectById(admission.getBedId());
            if (bed != null) {
                record.setBedNo(bed.getBedNo());
            }
        }

        record.setNursingType(dto.getNursingType());
        record.setMeasureTime(TimeUtil.toSeconds(dto.getMeasureTime()));
        record.setShift(dto.getShift());
        applyContent(record, dto);
        record.setNursingLevel(dto.getNursingLevel());
        record.setNursingContent(dto.getNursingContent());
        record.setRecordStatus(RecordStatusEnum.DRAFT.getCode());
        record.setNurseId(operatorUser.getEmployeeId());
        record.setNurseName(operatorUser.getRealName());
        record.setRemark(dto.getRemark());

        try {
            bizNursingRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该测量时点已存在同类型的护理文书（三测单同一时点只允许一条），请核对时间或改为修改已有记录");
        }
        writeActionLog(record, "创建");
        log.info("录入护理文书 recordNo={} admissionId={} type={} 护士={} 时点={}",
                record.getRecordNo(), record.getAdmissionId(),
                NursingDocTypeEnum.getText(record.getNursingType()),
                record.getNurseName(), record.getMeasureTime());
        return toVO(record);
    }

    // 查询

    private NursingRecordVO update(NursingRecordUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizNursingRecord record = bizNursingRecordMapper.selectById(dto.getId());
        if (record == null) {
            throw new BusinessException("护理文书不存在");
        }
        if (InpatientRecordStatusEnum.isArchived(record.getRecordStatus())) {
            throw new BusinessException("护理文书 " + record.getRecordNo()
                    + " 已归档，不允许修改（归档是单向门）");
        }
        if (dto.getNursingType() != null && !Objects.equals(dto.getNursingType(), record.getNursingType())) {
            throw new BusinessException("不能修改护理文书类型（改类型 = 换一份文书，统计口径会失真）；请另建新文书");
        }
        validateContent(dto);

        List<BizInpatientRecordLog> changes = diffContent(record, dto);
        if (dto.getShift() != null) {
            record.setShift(dto.getShift());
        }
        if (dto.getMeasureTime() != null) {
            record.setMeasureTime(TimeUtil.toSeconds(dto.getMeasureTime()));
        }
        record.setNursingLevel(dto.getNursingLevel());
        record.setNursingContent(dto.getNursingContent());
        record.setRemark(dto.getRemark());
        applyContent(record, dto);
        record.setNurseId(operatorUser.getEmployeeId());
        record.setNurseName(operatorUser.getRealName());

        try {
            bizNursingRecordMapper.updateById(record);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该测量时点已存在同类型的护理文书，不能改到这个时点上");
        }
        for (BizInpatientRecordLog row : changes) {
            bizInpatientRecordLogMapper.insert(row);
        }
        if (!changes.isEmpty()) {
            log.info("修改护理文书 recordNo={} 变更字段数={}", record.getRecordNo(), changes.size());
        }
        return toVO(record);
    }

    private void applyContent(BizNursingRecord record, NursingRecordUpsertDTO dto) {
        record.setTemperature(dto.getTemperature());
        record.setPulse(dto.getPulse());
        record.setRespiration(dto.getRespiration());
        record.setSystolicPressure(dto.getSystolicPressure());
        record.setDiastolicPressure(dto.getDiastolicPressure());
        record.setSpo2(dto.getSpo2());
        record.setStoolCount(dto.getStoolCount());
        record.setUrineVolume(dto.getUrineVolume());
        record.setIntakeVolume(dto.getIntakeVolume());
        record.setOutputVolume(dto.getOutputVolume());
    }

    private void validateContent(NursingRecordUpsertDTO dto) {
        Integer type = dto.getNursingType();
        if (Objects.equals(NursingDocTypeEnum.TEMP.getCode(), type) || Objects.equals(NursingDocTypeEnum.VITAL.getCode(), type)) {
            boolean any = dto.getTemperature() != null || dto.getPulse() != null
                    || dto.getRespiration() != null || dto.getSystolicPressure() != null
                    || dto.getDiastolicPressure() != null || dto.getSpo2() != null;
            if (!any) {
                throw new BusinessException(NursingDocTypeEnum.labelOrUnknown(type)
                        + "至少要录一个体征值（体温/脉搏/呼吸/血压/血氧任选其一）");
            }
        }
        // 保留（类别①条件必填）：仅「护理记录单」类型必填正文，其余类型不适用
        if (Objects.equals(NursingDocTypeEnum.NOTE.getCode(), type) && !TextUtil.hasText(dto.getNursingContent())) {
            throw new BusinessException("护理记录单必须填写护理记录正文");
        }
        if (dto.getTemperature() != null
                && (dto.getTemperature().compareTo(MIN_TEMP) < 0 || dto.getTemperature().compareTo(MAX_TEMP) > 0)) {
            throw new BusinessException("体温取值超出可信范围（34~43℃），请核对单位（℃）");
        }
        if (dto.getPulse() != null && (dto.getPulse() < 20 || dto.getPulse() > 250)) {
            throw new BusinessException("脉搏取值超出可信范围（20~250 次/分）");
        }
        if (dto.getRespiration() != null && (dto.getRespiration() < 5 || dto.getRespiration() > 80)) {
            throw new BusinessException("呼吸取值超出可信范围（5~80 次/分）");
        }
        if (dto.getSystolicPressure() != null && dto.getDiastolicPressure() != null
                && dto.getSystolicPressure() <= dto.getDiastolicPressure()) {
            throw new BusinessException("收缩压必须大于舒张压（当前 "
                    + dto.getSystolicPressure() + "/" + dto.getDiastolicPressure() + "）");
        }
        if (dto.getSpo2() != null && (dto.getSpo2() < 50 || dto.getSpo2() > 100)) {
            throw new BusinessException("血氧饱和度取值超出可信范围（50~100%）");
        }
    }

    @Override
    public NursingRecordVO detail(Long id) {
        BizNursingRecord record = bizNursingRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("护理文书不存在");
        }
        return toVO(record);
    }

    // G14-1 体温单批量录入

    @Override
    public IPage<NursingRecordVO> listPage(NursingRecordQueryPageDTO query) {
        // 科室数据权限收口（M6）：护理文书归属科室（dept_id），受限角色只看授权科室的文书。
        // scopeDeptIds 是服务端专用字段，先清掉前端可能伪造的值。
        query.setScopeDeptIds(deptScopeService.isScoped()
                ? List.copyOf(deptScopeService.allowedDeptIds()) : null);
        IPage<BizNursingRecord> page = bizNursingRecordMapper.selectNursingPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        List<NursingRecordVO> rows = new ArrayList<>(page.getRecords().size());
        for (BizNursingRecord r : page.getRecords()) {
            rows.add(toVO(r));
        }
        Page<NursingRecordVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(rows);
        return result;
    }

    // G14-2 护理评估单（压疮 / 跌倒 / 疼痛）

    @Override
    public TempSheetVO tempSheet(Long admissionId, String beginDate, String endDate) {
        BizAdmission admission = bizAdmissionMapper.selectById(admissionId);
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        List<BizNursingRecord> list = bizNursingRecordMapper.selectByAdmissionAndType(admissionId, NursingDocTypeEnum.TEMP.getCode(), beginDate,
                TextUtil.hasText(endDate) ? endDate + " 23:59:59" : null);

        TempSheetVO sheet = new TempSheetVO();
        sheet.setAdmissionId(admissionId);
        sheet.setPatientName(admission == null ? null : patientNameOf(admission.getPatientId()));
        sheet.setBedNo(admission.getBedId() == null ? null : bedNoOf(admission.getBedId()));
        sheet.setBeginDate(beginDate);
        sheet.setEndDate(endDate);
        sheet.setPointCount(list.size());
        sheet.setPoints(new ArrayList<>(list.size()));

        BigDecimal minTemp = null;
        BigDecimal maxTemp = null;
        Integer maxPulse = null;
        for (BizNursingRecord r : list) {
            TempSheetVO.TempPointVO p = new TempSheetVO.TempPointVO();
            p.setRecordId(r.getId());
            p.setMeasureTime(r.getMeasureTime());
            p.setMeasureDate(r.getMeasureTime() == null ? null : r.getMeasureTime().toLocalDate().format(DateFormats.DATE));
            p.setMeasureClock(r.getMeasureTime() == null ? null : r.getMeasureTime().format(DateFormats.TIME_MINUTE));
            p.setShiftText(dictCacheService.getDicDataLabel(DictType.NURSING_SHIFT, r.getShift()));
            p.setTemperature(r.getTemperature());
            p.setPulse(r.getPulse());
            p.setRespiration(r.getRespiration());
            p.setSystolicPressure(r.getSystolicPressure());
            p.setDiastolicPressure(r.getDiastolicPressure());
            p.setBloodPressureText(r.getSystolicPressure() == null || r.getDiastolicPressure() == null
                    ? "—" : r.getSystolicPressure() + "/" + r.getDiastolicPressure());
            p.setStoolCount(r.getStoolCount());
            p.setUrineVolume(r.getUrineVolume());
            p.setNurseName(r.getNurseName());
            sheet.getPoints().add(p);

            if (r.getTemperature() != null) {
                minTemp = minTemp == null ? r.getTemperature() : minTemp.min(r.getTemperature());
                maxTemp = maxTemp == null ? r.getTemperature() : maxTemp.max(r.getTemperature());
            }
            if (r.getPulse() != null) {
                maxPulse = maxPulse == null ? r.getPulse() : Math.max(maxPulse, r.getPulse());
            }
        }
        sheet.setMinTemperature(minTemp);
        sheet.setMaxTemperature(maxTemp);
        sheet.setMaxPulse(maxPulse);
        return sheet;
    }

    @Override
    public List<CodeOptionVO> typeOptions() {
        List<CodeOptionVO> list = new ArrayList<>();
        list.add(new CodeOptionVO(NursingDocTypeEnum.TEMP.getCode(), NursingDocTypeEnum.getText(NursingDocTypeEnum.TEMP.getCode())));
        list.add(new CodeOptionVO(NursingDocTypeEnum.NOTE.getCode(), NursingDocTypeEnum.getText(NursingDocTypeEnum.NOTE.getCode())));
        list.add(new CodeOptionVO(NursingDocTypeEnum.VITAL.getCode(), NursingDocTypeEnum.getText(NursingDocTypeEnum.VITAL.getCode())));
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveBatch(NursingRecordBatchUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Set<Long> seen = new HashSet<>();
        List<NursingRecordUpsertDTO> prepared = new ArrayList<>(dto.getRows().size());
        for (int i = 0; i < dto.getRows().size(); i++) {
            NursingRecordBatchUpsertDTO.BatchRow row = dto.getRows().get(i);
            String at = "第 " + (i + 1) + " 行：";
            if (!seen.add(row.getAdmissionId())) {
                throw new BusinessException(at + "同一患者在本批中出现了两次（同一次测量同一个人只有一条）");
            }
            // D-业务规则：跨字段「至少一个」约束（五列任缺其一都合法，单字段注解表达不了）
            if (row.getTemperature() == null && row.getPulse() == null && row.getRespiration() == null
                    && row.getSystolicPressure() == null && row.getDiastolicPressure() == null
                    && row.getSpo2() == null) {
                throw new BusinessException(at + "至少要录一个体征值");
            }
            BizAdmission admission = bizAdmissionMapper.selectById(row.getAdmissionId());
            if (admission == null) {
                throw new BusinessException(at + "入院记录不存在（admissionId=" + row.getAdmissionId() + "）");
            }
            if (!Integer.valueOf(1).equals(admission.getAdmitStatus())) {
                throw new BusinessException(at + "患者 " + patientNameOf(admission.getPatientId())
                        + " 不在院（批量录入面向在院患者）");
            }

            NursingRecordUpsertDTO item = new NursingRecordUpsertDTO();
            item.setAdmissionId(row.getAdmissionId());
            item.setNursingType(NursingDocTypeEnum.TEMP.getCode());
            item.setMeasureTime(dto.getMeasureTime());
            item.setShift(dto.getShift());
            item.setTemperature(row.getTemperature());
            item.setPulse(row.getPulse());
            item.setRespiration(row.getRespiration());
            item.setSystolicPressure(row.getSystolicPressure());
            item.setDiastolicPressure(row.getDiastolicPressure());
            item.setSpo2(row.getSpo2());
            item.setStoolCount(row.getStoolCount());
            item.setUrineVolume(row.getUrineVolume());
            item.setIntakeVolume(row.getIntakeVolume());
            item.setOutputVolume(row.getOutputVolume());
            item.setRemark(row.getRemark());
            prepared.add(item);
        }
        for (NursingRecordUpsertDTO item : prepared) {
            create(item);
        }
        log.info("体温单批量录入 {} 行 时点={} 护士={}", prepared.size(),
                TimeUtil.toSeconds(dto.getMeasureTime()), operatorUser.getRealName());
        return prepared.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NursingAssessmentVO saveAssessment(NursingAssessmentUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        // 保留（类别③）：总分必须能由量表明细推导，是业务一致性规则而非入参非空
        int recomputed = sumItems(dto.getItemsJson());
        if (dto.getTotalScore() == null || dto.getTotalScore() != recomputed) {
            throw new BusinessException("总分与量表明细求和不一致（明细合计 " + recomputed
                    + "，传入 " + dto.getTotalScore() + "）——请核对量表项是否有漏选或多选");
        }
        int riskLevel = riskLevelOf(dto.getAssessType(), recomputed);
        validateScoreRange(dto.getAssessType(), recomputed);

        BizNursingAssessment row;
        if (dto.getId() == null) {
            BizAdmission admission = bizAdmissionMapper.selectById(dto.getAdmissionId());
            if (admission == null) {
                throw new BusinessException("入院记录不存在");
            }
            BizPatient patient = bizPatientMapper.selectById(admission.getPatientId());
            row = new BizNursingAssessment();
            row.setAssessNo(nextAssessNo());
            row.setAdmissionId(admission.getAdmissionId());
            row.setPatientId(admission.getPatientId());
            row.setPatientNo(patient == null ? null : patient.getPatientNo());
            row.setPatientName(patient == null ? null : patient.getPatientName());
            row.setWardId(admission.getWardId());
            if (admission.getWardId() != null) {
                WardVO ward = sysBedMapper.selectWardById(admission.getWardId());
                row.setWardName(ward == null ? null : ward.getWardName());
            }
            if (admission.getBedId() != null) {
                SysBed bed = sysBedMapper.selectById(admission.getBedId());
                row.setBedNo(bed == null ? null : bed.getBedNo());
            }
            row.setAssessType(dto.getAssessType());
            row.setTotalScore(recomputed);
            row.setRiskLevel(riskLevel);
            row.setItemsJson(dto.getItemsJson());
            row.setAssessTime(TimeUtil.toSeconds(dto.getAssessTime()));
            row.setAssessNurseId(operatorUser.getEmployeeId());
            row.setAssessNurseName(operatorUser.getRealName());
            row.setRemark(dto.getRemark());
            bizNursingAssessmentMapper.insert(row);
        } else {
            row = bizNursingAssessmentMapper.selectById(dto.getId());
            if (row == null) {
                throw new BusinessException("评估单不存在");
            }
            if (!Objects.equals(row.getAssessType(), dto.getAssessType())) {
                throw new BusinessException("不能修改评估类型（换量表 = 换一份评估，请另建新单）");
            }
            row.setTotalScore(recomputed);
            row.setRiskLevel(riskLevel);
            row.setItemsJson(dto.getItemsJson());
            row.setAssessTime(TimeUtil.toSeconds(dto.getAssessTime()));
            row.setAssessNurseId(operatorUser.getEmployeeId());
            row.setAssessNurseName(operatorUser.getRealName());
            row.setRemark(dto.getRemark());
            bizNursingAssessmentMapper.updateById(row);
        }
        log.info("护理评估单 {} assessNo={} type={} score={} risk={}",
                dto.getId() == null ? "创建" : "修改", row.getAssessNo(),
                row.getAssessType(), row.getTotalScore(), row.getRiskLevel());
        return toAssessVO(row);
    }

    @Override
    public IPage<NursingAssessmentVO> assessmentListPage(NursingAssessmentQueryPageDTO query) {
        // 保留（类别③）：查询守卫「评估单不做全院裸捞」是范围约束，两个条件任一满足即可，
        // 查询 DTO 禁止挂必填注解
        if (query.getAdmissionId() == null && query.getWardId() == null) {
            throw new BusinessException("必须按入院ID或病区查询（评估单不做全院裸捞）");
        }
        return bizNursingAssessmentMapper.selectAssessPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
    }

    /**
     * 专项评估透视：每类量表最新一条（1-Braden 2-Morse 3-NRS 4-Caprini 5-管路滑脱）。
     *
     * <p>护士站首页要看的是「这个患者五类专项各评到哪一步」，不是一堆流水 ——
     * 窗口函数取每类 assess_time 最新（同秒按 id 稳定），没评过的类型**不返回**，
     * 前端据此渲染「未评估」占位（空缺也是一种要被看见的状态）。
     */
    @Override
    public List<NursingAssessmentVO> assessmentLatestByType(Long admissionId) {
        return bizNursingAssessmentMapper.selectLatestByAdmission(admissionId);
    }

    /**
     * 对 itemsJson 里每个对象的 score 求和（量表明细是唯一事实，总分必须能被推导）。
     *
     * <p>专项评估单（sql/159）允许混排<b>非计分项</b>：疼痛的部位/性质/措施、管路的留置清单
     * 都是 {@code {key,label,value}} 形式，没有 score 字段 —— 求和时跳过，但整份明细里
     * <b>至少要有一个计分项</b>（纯元数据的"评估"没有临床意义）。
     */
    private int sumItems(String itemsJson) {
        try {
            JsonNode node = objectMapper.readTree(itemsJson);
            if (!node.isArray() || node.isEmpty()) {
                throw new BusinessException("评分明细必须是数组且至少一项");
            }
            int sum = 0;
            int scored = 0;
            for (JsonNode item : node) {
                JsonNode score = item.get("score");
                // 非计分项（value 元数据）合法存在，只跳过不计分
                if (score == null || !score.canConvertToInt()) {
                    continue;
                }
                sum += score.asInt();
                scored++;
            }
            if (scored == 0) {
                throw new BusinessException("评分明细里至少要有一个带 score 的计分项");
            }
            return sum;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("评分明细不是合法 JSON：" + e.getMessage());
        }
    }

    private void validateScoreRange(int assessType, int score) {
        String range = switch (assessType) {
            case 1 -> "Braden 总分应在 6~23";
            case 2 -> "Morse 总分应在 0~125";
            case 3 -> "NRS 总分应在 0~10";
            case 4 -> "Caprini 总分应在 0~58";
            case 5 -> "管路滑脱总分应在 0~24";
            // 码值合法性由 DTO 的 @InEnum(NursingAssessTypeEnum) 兜住，走到这里必是 1~5 之一
            default -> throw new IllegalStateException("评估类型无分档口径：" + assessType);
        };
        boolean outOfRange = switch (assessType) {
            case 1 -> score < 6 || score > 23;
            case 2 -> score < 0 || score > 125;
            case 3 -> score < 0 || score > 10;
            case 4 -> score < 0 || score > 58;
            case 5 -> score < 0 || score > 24;
            default -> false;
        };
        if (outOfRange) {
            throw new BusinessException(range + "（当前 " + score + "）");
        }
    }

    // G14-3 出入量小结

    private NursingAssessmentVO toAssessVO(BizNursingAssessment row) {
        NursingAssessmentVO vo = new NursingAssessmentVO();
        vo.setId(row.getId());
        vo.setAssessNo(row.getAssessNo());
        vo.setAdmissionId(row.getAdmissionId());
        vo.setPatientId(row.getPatientId());
        vo.setPatientNo(row.getPatientNo());
        vo.setPatientName(row.getPatientName());
        vo.setWardName(row.getWardName());
        vo.setBedNo(row.getBedNo());
        vo.setAssessType(row.getAssessType());
        vo.setAssessType(row.getAssessType());
        vo.setAssessTypeText(NursingAssessTypeEnum.getText(row.getAssessType()));
        vo.setTotalScore(row.getTotalScore());
        vo.setRiskLevel(row.getRiskLevel());
        vo.setRiskLevelText(dictCacheService.getDicDataLabel(DictType.ASSESS_RISK_LEVEL, row.getRiskLevel()));
        vo.setItemsJson(row.getItemsJson());
        vo.setAssessTime(row.getAssessTime());
        vo.setAssessNurseId(row.getAssessNurseId());
        vo.setAssessNurseName(row.getAssessNurseName());
        vo.setRemark(row.getRemark());
        return vo;
    }

    // 跨模块事实面（只聚事实不判异常 —— 阈值口径留在消费方）

    private String nextAssessNo() {
        return redisSequenceService.generateNursingAssessNo();
    }

    @Override
    public IntakeOutputSummaryVO intakeOutputSummary(Long admissionId, String beginDate, String endDate) {
        BizAdmission admission = bizAdmissionMapper.selectById(admissionId);
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        // endDate 是日期字符串，直接 `measure_time <= 'yyyy-MM-dd'` 会把当天全部时点滤掉
        // （datetime 恒大于当日 00:00:00 字符串）→ 补全天边界
        String endBoundary = TextUtil.hasText(endDate) ? endDate + " 23:59:59" : null;
        List<BizNursingRecord> list = bizNursingRecordMapper.selectByAdmissionAndTypes(
                admissionId, List.of(NursingDocTypeEnum.TEMP.getCode(), NursingDocTypeEnum.VITAL.getCode()), beginDate, endBoundary);

        // 按日聚合：TreeMap 保证日期升序（小结必须按时间正序读，倒序读会诱导漏看趋势）
        TreeMap<LocalDate, IntakeOutputSummaryVO.DayRow> byDay = new TreeMap<>();
        for (BizNursingRecord r : list) {
            if (r.getMeasureTime() == null) {
                continue;
            }
            LocalDate day = r.getMeasureTime().toLocalDate();
            IntakeOutputSummaryVO.DayRow row = byDay.computeIfAbsent(day, k -> {
                IntakeOutputSummaryVO.DayRow d = new IntakeOutputSummaryVO.DayRow();
                d.setDate(k.format(DateFormats.DATE));
                d.setIntake(0);
                d.setOutput(0);
                d.setUrine(0);
                d.setStool(0);
                d.setPointCount(0);
                return d;
            });
            row.setPointCount(row.getPointCount() + 1);
            if (r.getIntakeVolume() != null) {
                row.setIntake(row.getIntake() + r.getIntakeVolume());
            }
            if (r.getOutputVolume() != null) {
                row.setOutput(row.getOutput() + r.getOutputVolume());
            }
            if (r.getUrineVolume() != null) {
                row.setUrine(row.getUrine() + r.getUrineVolume());
            }
            if (r.getStoolCount() != null) {
                row.setStool(row.getStool() + r.getStoolCount());
            }
        }

        IntakeOutputSummaryVO vo = new IntakeOutputSummaryVO();
        vo.setAdmissionId(admissionId);
        vo.setPatientName(patientNameOf(admission.getPatientId()));
        vo.setBeginDate(beginDate);
        vo.setEndDate(endDate);
        vo.setRecordCount(list.size());
        int totalIntake = 0;
        int totalOutput = 0;
        int totalUrine = 0;
        int totalStool = 0;
        int totalPoints = 0;
        for (IntakeOutputSummaryVO.DayRow d : byDay.values()) {
            d.setNetBalance(d.getIntake() - d.getOutput());
            totalIntake += d.getIntake();
            totalOutput += d.getOutput();
            totalUrine += d.getUrine();
            totalStool += d.getStool();
            totalPoints += d.getPointCount();
        }
        IntakeOutputSummaryVO.DayRow totals = new IntakeOutputSummaryVO.DayRow();
        totals.setIntake(totalIntake);
        totals.setOutput(totalOutput);
        totals.setUrine(totalUrine);
        totals.setStool(totalStool);
        totals.setNetBalance(totalIntake - totalOutput);
        totals.setPointCount(totalPoints);
        vo.setTotals(totals);
        vo.setNetBalance(BigDecimal.valueOf(totalIntake - totalOutput));
        vo.setDays(new ArrayList<>(byDay.values()));
        return vo;
    }

    @Override
    public List<NursingVitalFactVO> latestVitalsByWard(Long wardId, LocalDateTime since) {
        // C-非 web 入参：his-ai DeteriorationAlertCapabilityImpl 跨模块直调（内部派生 wardId），Bean Validation 不覆盖，保留
        if (wardId == null) {
            throw new BusinessException("病区ID不能为空");
        }
        List<BizNursingRecord> rows = bizNursingRecordMapper.selectList(new LambdaQueryWrapper<BizNursingRecord>()
                .eq(BizNursingRecord::getWardId, wardId)
                .ge(since != null, BizNursingRecord::getMeasureTime, since)
                .and(w -> w.isNotNull(BizNursingRecord::getTemperature)
                        .or().isNotNull(BizNursingRecord::getPulse)
                        .or().isNotNull(BizNursingRecord::getRespiration)
                        .or().isNotNull(BizNursingRecord::getSystolicPressure)
                        .or().isNotNull(BizNursingRecord::getDiastolicPressure)
                        .or().isNotNull(BizNursingRecord::getSpo2)));
        Map<Long, BizNursingRecord> latest = new HashMap<>();
        for (BizNursingRecord r : rows) {
            if (r.getAdmissionId() == null || r.getMeasureTime() == null) {
                continue;
            }
            BizNursingRecord kept = latest.get(r.getAdmissionId());
            if (kept == null || r.getMeasureTime().isAfter(kept.getMeasureTime())) {
                latest.put(r.getAdmissionId(), r);
            }
        }
        List<NursingVitalFactVO> result = new ArrayList<>(latest.size());
        for (BizNursingRecord r : latest.values()) {
            result.add(toVitalFact(r));
        }
        result.sort((a, b) -> b.getMeasureTime().compareTo(a.getMeasureTime()));
        return result;
    }

    @Override
    public NursingVitalFactVO latestVitalByAdmission(Long admissionId, LocalDateTime since) {
        // C-非 web 入参：his-ai DeteriorationAlertCapabilityImpl 跨模块直调（内部派生 admissionId），Bean Validation 不覆盖，保留
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        List<BizNursingRecord> rows = bizNursingRecordMapper.selectList(new LambdaQueryWrapper<BizNursingRecord>()
                .eq(BizNursingRecord::getAdmissionId, admissionId)
                .ge(since != null, BizNursingRecord::getMeasureTime, since)
                .and(w -> w.isNotNull(BizNursingRecord::getTemperature)
                        .or().isNotNull(BizNursingRecord::getPulse)
                        .or().isNotNull(BizNursingRecord::getRespiration)
                        .or().isNotNull(BizNursingRecord::getSystolicPressure)
                        .or().isNotNull(BizNursingRecord::getDiastolicPressure)
                        .or().isNotNull(BizNursingRecord::getSpo2))
                .orderByDesc(BizNursingRecord::getMeasureTime)
                .last("LIMIT 1"));
        return rows.isEmpty() ? null : toVitalFact(rows.get(0));
    }

    @Override
    public WardNursingFactsVO wardShiftFacts(Long wardId, LocalDateTime begin, LocalDateTime end, Integer shift) {
        // C-非 web 入参：his-ai NursingHandoverCapabilityImpl 跨模块直调（内部派生病区与时间窗），Bean Validation 不覆盖，保留
        if (wardId == null || begin == null || end == null) {
            throw new BusinessException("病区ID与时间窗不能为空");
        }
        WardNursingFactsVO vo = new WardNursingFactsVO();
        vo.setWardId(wardId);
        WardVO ward = sysBedMapper.selectWardById(wardId);
        vo.setWardName(ward == null ? null : ward.getWardName());
        vo.setShift(shift);
        vo.setShiftText(dictCacheService.getDicDataLabel(DictType.NURSING_SHIFT, shift));
        vo.setWindowBegin(begin);
        vo.setWindowEnd(end);

        vo.setVitalRows(bizNursingRecordMapper.selectList(new LambdaQueryWrapper<BizNursingRecord>()
                        .eq(BizNursingRecord::getWardId, wardId)
                        .ge(BizNursingRecord::getMeasureTime, begin)
                        .lt(BizNursingRecord::getMeasureTime, end)
                        .orderByAsc(BizNursingRecord::getMeasureTime))
                .stream().map(this::toVitalFact).collect(java.util.stream.Collectors.toList()));

        vo.setAssessmentRows(bizNursingAssessmentMapper.selectList(new LambdaQueryWrapper<BizNursingAssessment>()
                        .eq(BizNursingAssessment::getWardId, wardId)
                        .ge(BizNursingAssessment::getAssessTime, begin)
                        .lt(BizNursingAssessment::getAssessTime, end)
                        .orderByAsc(BizNursingAssessment::getAssessTime))
                .stream().map(this::toAssessVO).collect(java.util.stream.Collectors.toList()));

        WardNursingFactsVO.Census census = new WardNursingFactsVO.Census();
        census.setInHospitalCount(Math.toIntExact(bizAdmissionMapper.selectCount(new LambdaQueryWrapper<BizAdmission>()
                .eq(BizAdmission::getWardId, wardId)
                .eq(BizAdmission::getAdmitStatus, AdmitStatusEnum.IN_HOSPITAL.getCode()))));
        census.setDischargeCount(Math.toIntExact(bizAdmissionMapper.selectCount(new LambdaQueryWrapper<BizAdmission>()
                .eq(BizAdmission::getWardId, wardId)
                .eq(BizAdmission::getAdmitStatus, AdmitStatusEnum.DISCHARGED.getCode())
                .ge(BizAdmission::getDischargeTime, begin)
                .lt(BizAdmission::getDischargeTime, end))));
        List<BizAdmission> newAdmissions = bizAdmissionMapper.selectList(new LambdaQueryWrapper<BizAdmission>()
                .eq(BizAdmission::getWardId, wardId)
                .ge(BizAdmission::getAdmitTime, begin)
                .lt(BizAdmission::getAdmitTime, end)
                .orderByAsc(BizAdmission::getAdmitTime));
        for (BizAdmission a : newAdmissions) {
            WardNursingFactsVO.AdmissionBrief brief = new WardNursingFactsVO.AdmissionBrief();
            brief.setAdmissionId(a.getAdmissionId());
            brief.setPatientName(patientNameOf(a.getPatientId()));
            brief.setBedNo(bedNoOf(a.getBedId()));
            brief.setAdmitTime(a.getAdmitTime());
            brief.setAdmitDiagnosisName(a.getAdmitDiagnosisName());
            census.getNewAdmissions().add(brief);
        }
        vo.setCensus(census);
        return vo;
    }

    // diff 留痕

    private NursingVitalFactVO toVitalFact(BizNursingRecord r) {
        NursingVitalFactVO vo = new NursingVitalFactVO();
        vo.setAdmissionId(r.getAdmissionId());
        vo.setPatientId(r.getPatientId());
        vo.setPatientName(r.getPatientName());
        vo.setBedNo(r.getBedNo());
        vo.setWardId(r.getWardId());
        vo.setWardName(r.getWardName());
        vo.setMeasureTime(r.getMeasureTime());
        vo.setShift(r.getShift());
        vo.setTemperature(r.getTemperature());
        vo.setPulse(r.getPulse());
        vo.setRespiration(r.getRespiration());
        vo.setSystolicPressure(r.getSystolicPressure());
        vo.setDiastolicPressure(r.getDiastolicPressure());
        vo.setSpo2(r.getSpo2());
        vo.setNursingLevel(r.getNursingLevel());
        vo.setNursingContent(r.getNursingContent());
        return vo;
    }

    private NursingRecordVO toVO(BizNursingRecord r) {
        NursingRecordVO vo = new NursingRecordVO();
        vo.setId(r.getId());
        vo.setRecordNo(r.getRecordNo());
        vo.setAdmissionId(r.getAdmissionId());
        vo.setPatientId(r.getPatientId());
        vo.setPatientName(r.getPatientName());
        vo.setWardName(r.getWardName());
        vo.setBedNo(r.getBedNo());
        vo.setNursingType(r.getNursingType());
        vo.setNursingTypeText(NursingDocTypeEnum.getText(r.getNursingType()));
        vo.setMeasureTime(r.getMeasureTime());
        vo.setMeasureDate(r.getMeasureTime() == null ? null : r.getMeasureTime().toLocalDate().format(DateFormats.DATE));
        vo.setMeasureClock(r.getMeasureTime() == null ? null : r.getMeasureTime().format(DateFormats.TIME_MINUTE));
        vo.setShift(r.getShift());
        vo.setShiftText(dictCacheService.getDicDataLabel(DictType.NURSING_SHIFT, r.getShift()));
        vo.setTemperature(r.getTemperature());
        vo.setPulse(r.getPulse());
        vo.setRespiration(r.getRespiration());
        vo.setSystolicPressure(r.getSystolicPressure());
        vo.setDiastolicPressure(r.getDiastolicPressure());
        vo.setBloodPressureText(r.getSystolicPressure() == null || r.getDiastolicPressure() == null
                ? "—" : r.getSystolicPressure() + "/" + r.getDiastolicPressure());
        vo.setSpo2(r.getSpo2());
        vo.setStoolCount(r.getStoolCount());
        vo.setUrineVolume(r.getUrineVolume());
        vo.setIntakeVolume(r.getIntakeVolume());
        vo.setOutputVolume(r.getOutputVolume());
        vo.setNursingLevel(r.getNursingLevel());
        vo.setNursingLevelText(dictCacheService.getDicDataLabel(DictType.NURSING_LEVEL, r.getNursingLevel()));
        vo.setNursingContent(r.getNursingContent());
        vo.setNurseId(r.getNurseId());
        vo.setNurseName(r.getNurseName());
        vo.setRecordStatus(r.getRecordStatus());
        vo.setRecordStatusText(SummaryStatusEnum.getText(r.getRecordStatus()));
        vo.setCanEdit(!InpatientRecordStatusEnum.isArchived(r.getRecordStatus()));
        vo.setRemark(r.getRemark());
        return vo;
    }

    private List<BizInpatientRecordLog> diffContent(BizNursingRecord oldRecord, NursingRecordUpsertDTO dto) {
        List<Object[]> pairs = new ArrayList<>();
        if (dto.getMeasureTime() != null) {
            pairs.add(new Object[]{"measure_time",
                    oldRecord.getMeasureTime() == null ? null : oldRecord.getMeasureTime().toString(),
                    TimeUtil.toSeconds(dto.getMeasureTime()).toString()});
        }
        pairs.add(new Object[]{"shift", oldRecord.getShift(), dto.getShift()});
        pairs.add(new Object[]{"temperature", oldRecord.getTemperature(), dto.getTemperature()});
        pairs.add(new Object[]{"pulse", oldRecord.getPulse(), dto.getPulse()});
        pairs.add(new Object[]{"respiration", oldRecord.getRespiration(), dto.getRespiration()});
        pairs.add(new Object[]{"systolic_pressure", oldRecord.getSystolicPressure(), dto.getSystolicPressure()});
        pairs.add(new Object[]{"diastolic_pressure", oldRecord.getDiastolicPressure(), dto.getDiastolicPressure()});
        pairs.add(new Object[]{"spo2", oldRecord.getSpo2(), dto.getSpo2()});
        pairs.add(new Object[]{"stool_count", oldRecord.getStoolCount(), dto.getStoolCount()});
        pairs.add(new Object[]{"urine_volume", oldRecord.getUrineVolume(), dto.getUrineVolume()});
        pairs.add(new Object[]{"intake_volume", oldRecord.getIntakeVolume(), dto.getIntakeVolume()});
        pairs.add(new Object[]{"output_volume", oldRecord.getOutputVolume(), dto.getOutputVolume()});
        pairs.add(new Object[]{"nursing_level", oldRecord.getNursingLevel(), dto.getNursingLevel()});
        pairs.add(new Object[]{"nursing_content", oldRecord.getNursingContent(), dto.getNursingContent()});
        if (dto.getRemark() != null) {
            pairs.add(new Object[]{"remark", oldRecord.getRemark(), dto.getRemark()});
        }

        List<BizInpatientRecordLog> changes = new ArrayList<>();
        for (Object[] p : pairs) {
            String code = (String) p[0];
            if (Objects.equals(asText(p[1]), asText(p[2]))) {
                continue;
            }
            BizInpatientRecordLog row = actionLog(oldRecord, "修改");
            row.setFieldName(code);
            row.setOldValue(asText(p[1]));
            row.setNewValue(asText(p[2]));
            changes.add(row);
        }
        return changes;
    }

    private BizInpatientRecordLog actionLog(BizNursingRecord record, String operation) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizInpatientRecordLog row = new BizInpatientRecordLog();
        row.setDocType(RecordDocTypeEnum.NURSING.getCode());
        row.setRecordId(record.getId());
        row.setRecordNo(record.getRecordNo());
        row.setRecordType(record.getNursingType());
        row.setUserId(operatorUser.getEmployeeId());
        row.setUserName(operatorUser.getRealName());
        row.setOperation(operation);
        return row;
    }

    // 私有辅助

    private void writeActionLog(BizNursingRecord record, String operation) {
        bizInpatientRecordLogMapper.insert(actionLog(record, operation));
    }

    private String patientNameOf(Long patientId) {
        BizPatient p = bizPatientMapper.selectById(patientId);
        return p == null ? null : p.getPatientName();
    }

    private String bedNoOf(Long bedId) {
        SysBed b = sysBedMapper.selectById(bedId);
        return b == null ? null : b.getBedNo();
    }

    private String nextRecordNo() {
        return redisSequenceService.generateNursingRecordNo();
    }

}