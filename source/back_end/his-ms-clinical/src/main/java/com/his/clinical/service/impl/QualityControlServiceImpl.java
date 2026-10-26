package com.his.clinical.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.DictTypeConst;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.clinical.dto.QcCandidatePageQueryDTO;
import com.his.clinical.dto.QcExecuteDTO;
import com.his.clinical.dto.QcPageQueryDTO;
import com.his.clinical.dto.QcSystemExecuteDTO;
import com.his.clinical.entity.BizMedicalRecord;
import com.his.clinical.entity.BizQualityControl;
import com.his.clinical.mapper.BizMedicalRecordMapper;
import com.his.clinical.mapper.BizQualityControlMapper;
import com.his.clinical.service.QcStoreService;
import com.his.clinical.service.QualityControlService;
import com.his.clinical.vo.QcIssueVO;
import com.his.clinical.vo.QcResultVO;
import com.his.clinical.support.QcRuleEngine;
import com.his.clinical.support.QcSnapshot;
import com.his.clinical.support.QcOperator;
import com.his.clinical.vo.*;
import com.his.spi.service.InpatientFacadeService;
import com.his.spi.vo.InpatientRecordBriefVO;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.BizTypeEnum;
import com.his.spi.service.DeptScopeService;
import com.his.spi.service.DictCacheService;
import com.his.spi.service.SysMessageService;
import com.his.common.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import com.his.common.enums.QcRuleEnum;

/**
 * 病案质控服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityControlServiceImpl extends ServiceImpl<BizQualityControlMapper, BizQualityControl> implements QualityControlService {
    /**
     * 单号冲突重试次数
     */
    private static final int MAX_NO_RETRY = 3;
    /**
     * 批量质控单次上限，防止前端误传全表 ID
     */
    private static final int MAX_BATCH_SIZE = 200;
    /**
     * 补扫回看窗口（天）：只捞这几天内结诊的病历，避免每次全表反查
     */
    private static final int RESCAN_WINDOW_DAYS = 3;
    /**
     * 补扫的操作人（定时任务豁免，见 AGENTS §12）：没有登录态，落带任务名的系统身份，
     * 让人一眼看出这张单不是人点的、是哪个任务跑的。
     */
    private static final String SYSTEM_RESCAN_OPERATOR = "system:qcAutoRescan";
    private static final Long SYSTEM_RESCAN_EMPLOYEE_ID = 0L;

    private final BizMedicalRecordMapper bizMedicalRecordMapper;

    private final InpatientFacadeService patientFacadeService;

    private final QcRuleEngine qcRuleEngine;

    private final QcStoreService qcStoreService;

    private final SysMessageService sysMessageService;

    private final DictCacheService dictCacheService;

    private final DeptScopeService deptScopeService;

    /**
     * 质控类型归一。
     *
     * <p>明确拒绝 4：AI 内涵质控有自己的接口与自己的落库路径，
     * 从形式质控的入口传 4 进来只会产生一张"规则跑不了、AI 又没跑"的空单。
     * 宁可报错，也不接受一个语义不明的参数。
     */
    private static Integer normalizeQcType(Integer qcType) {
        if (qcType == null || qcType == 0) {
            return 0;
        }
        if (QcDimensionEnum.fromCode(qcType) == null) {
            throw new BusinessException("不支持的质控类型：" + qcType
                    + "（可选 0-综合 1-完整性 2-规范性 3-逻辑性；AI 内涵质控请调用 /ai/emrQc 接口）");
        }
        return qcType;
    }

    /**
     * 病历质量等级：有否决项必为丙级，否则按分数线（甲≥90 乙75~89 丙&lt;75）。
     * score 为空（旧版质控）返回 null —— 不猜等级。
     */
    private static String grade(Integer score, Integer severityMax) {
        if (score == null) {
            return null;
        }
        if (severityMax != null && severityMax >= QcSeverityEnum.FATAL.getCode()) {
            return "丙";
        }
        if (score >= 90) {
            return "甲";
        }
        return score >= 75 ? "乙" : "丙";
    }

    /** 质控单/问题明细本身无科室列：按 record_source + record_id 反查病历归属科室后校验 */
    private void assertRecordDeptAccessible(String recordSource, Long recordId) {
        if (QcRecordSourceEnum.fromCode(recordSource) == QcRecordSourceEnum.OUTPATIENT) {
            BizMedicalRecord record = bizMedicalRecordMapper.selectById(recordId);
            deptScopeService.assertDeptAccessible(record == null ? null : record.getDeptId());
            return;
        }
        InpatientRecordBriefVO record = patientFacadeService.getInpatientRecord(recordId);
        deptScopeService.assertDeptAccessible(record == null ? null : record.getDeptId());
    }

    @Override
    public PageResult<BizQualityControlVO> selectQcPage(QcPageQueryDTO query) {
        Page<BizQualityControlVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        var result = baseMapper.selectQcPage(page, query, deptScopeService.scopedDeptIds(null));
        result.getRecords().forEach(this::enrich);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(),
                result.getRecords());
    }

    @Override
    public BizQualityControlVO getQcDetail(Long qcId) {
        BizQualityControlVO vo = baseMapper.selectQcById(qcId);
        if (vo == null) {
            throw new BusinessException("质控单不存在：" + qcId);
        }
        assertRecordDeptAccessible(vo.getRecordSource(), vo.getRecordId());
        enrich(vo);
        vo.setIssues(enrich(baseMapper.listIssueByQc(qcId)));
        return vo;
    }

    @Override
    public List<QcIssueVO> listIssueByQc(Long qcId) {
        BizQualityControl qc = this.getById(qcId);
        if (qc == null) {
            throw new BusinessException("质控单不存在：" + qcId);
        }
        assertRecordDeptAccessible(qc.getRecordSource(), qc.getRecordId());
        return enrich(baseMapper.listIssueByQc(qcId));
    }

    @Override
    public QcOverviewVO getOverview() {
        List<Long> scope = deptScopeService.scopedDeptIds(null);
        QcOverviewVO overview = baseMapper.selectOverview(scope);
        QcOverviewVO issueTotals = baseMapper.selectIssueTotals(scope);
        overview.setIssueCount(issueTotals.getIssueCount());
        overview.setIssueRecordCount(issueTotals.getIssueRecordCount());
        // 分母为 0 时给 null 而不是 0：0 看起来像"甲级率 0%"，而事实是"还没有质控数据"
        long scored = overview.getScoredCount();
        overview.setGradeARate(scored == 0 ? null
                : Math.round(overview.getGradeACount() * 1000.0 / scored) / 10.0);

        Map<Integer, QcRuleMetricVO> statByDimension = new HashMap<>();
        baseMapper.selectDimensionStat(scope).forEach(stat -> statByDimension.put(stat.getDimension(), stat));
        List<QcOverviewVO.DimensionStat> dimensions = new ArrayList<>();
        for (QcDimensionEnum dimension : QcDimensionEnum.values()) {
            QcOverviewVO.DimensionStat stat = new QcOverviewVO.DimensionStat();
            stat.setDimension(dimension.getCode());
            stat.setDimensionText(dimension.getLabel());
            QcRuleMetricVO hit = statByDimension.get(dimension.getCode());
            stat.setIssueCount(hit == null || hit.getHitCount() == null ? 0 : hit.getHitCount());
            stat.setDeductTotal(hit == null || hit.getDeductTotal() == null ? 0 : hit.getDeductTotal());
            dimensions.add(stat);
        }
        overview.setDimensionIssues(dimensions);
        return overview;
    }

    /**
     * 规则清单。**以规则枚举为基准**左连接命中统计 ——
     * 从明细表 GROUP BY 出来的清单会把"从未命中"和"压根没实现"显示成一个样子。
     */
    @Override
    public List<QcRuleMetricVO> listRuleMetric(Integer dimension) {
        QcDimensionEnum only = dimension == null ? null : QcDimensionEnum.fromCode(dimension);
        if (dimension != null && only == null) {
            throw new BusinessException("未知的质控维度：" + dimension + "（可选 1-完整性 2-规范性 3-逻辑性）");
        }
        Map<String, QcRuleMetricVO> statByRule = new HashMap<>();
        baseMapper.selectRuleStat().forEach(stat -> statByRule.put(stat.getRuleCode(), stat));

        List<QcRuleMetricVO> list = new ArrayList<>();
        for (QcRuleEnum rule : QcRuleEnum.values()) {
            if (only != null && rule.getDimension() != only) {
                continue;
            }
            QcRuleMetricVO vo = new QcRuleMetricVO();
            vo.setRuleCode(rule.getCode());
            vo.setRuleName(rule.getName());
            vo.setDimension(rule.getDimension().getCode());
            vo.setDimensionText(rule.getDimension().getLabel());
            vo.setSeverity(rule.getSeverity().getCode());
            vo.setSeverityText(rule.getSeverity().getLabel());
            vo.setDeduct(rule.getSeverity().getDeduct());
            vo.setScopeText(rule.getScope().getLabel());
            vo.setBasis(rule.getBasis());
            QcRuleMetricVO hit = statByRule.get(rule.getCode());
            long hitCount = hit == null || hit.getHitCount() == null ? 0L : hit.getHitCount();
            vo.setHitCount(hitCount);
            vo.setQcCount(hit == null || hit.getQcCount() == null ? 0L : hit.getQcCount());
            vo.setDeductTotal(hit == null || hit.getDeductTotal() == null ? 0L : hit.getDeductTotal());
            vo.setEmpty(hitCount == 0L);
            list.add(vo);
        }
        // 命中多的排前面；同为 0 命中时按规则编码，保证每次刷新顺序一致
        list.sort(Comparator.comparing(QcRuleMetricVO::getHitCount).reversed()
                .thenComparing(QcRuleMetricVO::getRuleCode));
        return list;
    }

    // 执行质控
    @Override
    public PageResult<QcCandidateVO> listCandidatePage(QcCandidatePageQueryDTO query) {
        QcRecordSourceEnum source = QcRecordSourceEnum.fromCode(query.getRecordSource());
        Page<QcCandidateVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        List<Long> scope = deptScopeService.scopedDeptIds(null);
        var result = source == QcRecordSourceEnum.OUTPATIENT
                ? baseMapper.selectOutpatientCandidatePage(page, query, scope)
                : baseMapper.selectInpatientCandidatePage(page, query, scope);
        result.getRecords().forEach(this::enrich);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(),
                result.getRecords());
    }

    @Override
    public List<QcDimensionSelectListVO> dimensionDict() {
        List<QcDimensionSelectListVO> list = new ArrayList<>();
        for (QcDimensionEnum dimension : QcDimensionEnum.values()) {
            QcDimensionSelectListVO item = new QcDimensionSelectListVO();
            item.setCode(dimension.getCode());
            item.setText(dimension.getLabel());
            item.setDescription(dimension.getDescription());
            list.add(item);
        }
        return list;
    }

    @Override
    public List<QcTypeSelectListVO> qcTypeDict() {
        List<QcTypeSelectListVO> list = new ArrayList<>();
        // 0 与 4 不是"维度"，但它们在质控检查记录的质控类型里，筛选框必须都能选
        for (int code : new int[]{0, 1, 2, 3, 4}) {
            QcTypeSelectListVO item = new QcTypeSelectListVO();
            item.setCode(code);
            item.setText(RecordQcTypeEnum.getLabel(code));
            list.add(item);
        }
        return list;
    }

    @Override
    public BizQualityControlVO executeQc(QcExecuteDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return doExecuteQc(dto.getRecordSource(), dto.getRecordId(), normalizeQcType(dto.getQcType()),
                QcTriggerSourceEnum.MANUAL,
                new QcOperator(operatorUser.getEmployeeId(), operatorUser.getRealName()));
    }

    /**
     * C-非 web 入参：结诊收尾与定时补扫在新线程里直调，Bean Validation 不覆盖内部调用，
     * 病历与触发来源缺值必须由这里拦住（HTTP 入口那侧靠 DTO 注解）。
     */
    @Override
    public BizQualityControlVO executeQcBySystem(QcSystemExecuteDTO dto) {
        if (dto == null || dto.getOperator() == null || dto.getTriggerSource() == null) {
            throw new BusinessException("系统质控缺少操作人或触发来源");
        }
        return doExecuteQc(dto.getRecordSource(), dto.getRecordId(), normalizeQcType(dto.getQcType()),
                dto.getTriggerSource(), dto.getOperator());
    }

    @Override
    public List<QcIssueVO> inspectSubmitVeto(QcSnapshot snapshot) {
        QcResultVO result = qcRuleEngine.inspect(snapshot, RecordQcTypeEnum.COMPREHENSIVE.getCode());
        List<QcIssueVO> vetoIssues = new ArrayList<>();
        for (QcIssueVO issue : result.getIssues()) {
            if (issue.getSeverity() != null && issue.getSeverity() == QcSeverityEnum.FATAL.getCode()) {
                vetoIssues.add(issue);
            }
        }
        enrich(vetoIssues);
        return vetoIssues;
    }

    @Override
    public int autoRescanOutpatientQc(int limit) {
        List<Long> recordIds = baseMapper.selectRecordsWithoutAutoQc(QcRecordSourceEnum.OUTPATIENT.getCode(),
                List.of(QcTriggerSourceEnum.CLINIC_SUBMIT.getCode(), QcTriggerSourceEnum.AUTO_RESCAN.getCode()),
                List.of(RecordStatusEnum.SUBMITTED.getCode(), RecordStatusEnum.ARCHIVED.getCode()),
                LocalDateTime.now().minusDays(RESCAN_WINDOW_DAYS), limit);
        if (recordIds.isEmpty()) {
            return 0;
        }
        QcOperator rescanOperator = new QcOperator(SYSTEM_RESCAN_EMPLOYEE_ID, SYSTEM_RESCAN_OPERATOR);
        int done = 0;
        for (Long recordId : recordIds) {
            try {
                QcSystemExecuteDTO dto = new QcSystemExecuteDTO();
                dto.setRecordSource(QcRecordSourceEnum.OUTPATIENT.getCode());
                dto.setRecordId(recordId);
                dto.setTriggerSource(QcTriggerSourceEnum.AUTO_RESCAN);
                dto.setOperator(rescanOperator);
                executeQcBySystem(dto);
                done++;
            } catch (Exception ex) {
                // 单张失败不拖垮整批：下一轮补扫还会把它捞回来（判据是「没有自动质控单」）
                log.error("[质控补扫] 门诊病历补跑质控失败 recordId={}", recordId, ex);
            }
        }
        log.info("[质控补扫] 近 {} 天漏单 {} 份，补跑成功 {} 份", RESCAN_WINDOW_DAYS, recordIds.size(), done);
        return done;
    }

    private BizQualityControlVO doExecuteQc(String recordSource, Long recordId, Integer qcType,
                                            QcTriggerSourceEnum triggerSource, QcOperator operator) {
        if (recordId == null) {
            throw new BusinessException("病历ID不能为空");
        }
        QcSnapshot snapshot = loadSnapshot(QcRecordSourceEnum.fromCode(recordSource), recordId, triggerSource);
        QcResultVO result = qcRuleEngine.inspect(snapshot, qcType);

        BizQualityControl saved = null;
        for (int attempt = 1; attempt <= MAX_NO_RETRY && saved == null; attempt++) {
            try {
                saved = qcStoreService.save(snapshot, result, qcType, triggerSource, operator);
            } catch (DuplicateKeyException ex) {
                // 单号撞唯一索引：事务已回滚，必须在事务外重试（这也是把落库单独拆成 Bean 的原因）
                log.warn("[病案质控] 第 {} 次生成质控单号冲突，重试（recordId={}）", attempt, recordId);
            }
        }
        if (saved == null) {
            throw new BusinessException("质控单号生成失败，请重试");
        }

        BizQualityControlVO vo = baseMapper.selectQcById(saved.getId());
        if (vo == null) {
            throw new BusinessException("质控单落库后回读失败：" + saved.getQcNo());
        }
        enrich(vo);
        // 明细从库里读回来，不用内存对象 —— 落库没写进去的话接口就该是错的
        vo.setIssues(enrich(baseMapper.listIssueByQc(saved.getId())));
        notifyDoctorOfQcIssues(snapshot, result, saved);
        return vo;
    }

    /**
     * emr-qc 发送方：质控发现问题（issueCount &gt; 0）→ 站内信通知病历书写医生。
     */
    private void notifyDoctorOfQcIssues(QcSnapshot snapshot, QcResultVO result, BizQualityControl saved) {
        if (result.getIssueCount() <= 0 || snapshot.getDoctorId() == null) {
            return;
        }
        try {
            String content = String.format(
                    "您书写的%s %s（患者 %s）已完成病案质控（质控单 %s），发现问题 %d 项，得分 %d 分（%s）。请查看问题明细并及时整改，整改后可重新质控。",
                    snapshot.recordTypeText(), snapshot.getRecordNo(),
                    snapshot.getPatientName() == null ? "未知" : snapshot.getPatientName(),
                    saved.getQcNo(), result.getIssueCount(), result.getScore(),
                    result.getGrade() == null ? "未评级" : result.getGrade());
            MessagePayloadVO msg = new MessagePayloadVO();
            msg.setPatientName(snapshot.getPatientName());
            msg.setRecordNo(snapshot.getRecordNo());
            msg.setQcNo(saved.getQcNo());
            msg.setIssueCount(result.getIssueCount());
            msg.setGrade(result.getGrade());
            String payload = cn.hutool.json.JSONUtil.toJsonStr(msg);
            sysMessageService.sendSystemMessage(snapshot.getDoctorId(), snapshot.getDoctorName(),
                    "病历质控问题：" + snapshot.getRecordNo(), content,
                    BizTypeEnum.EMR_QC.getType(), saved.getId(), "warning", payload, null);
        } catch (Exception ex) {
            log.warn("[病案质控] 质控问题通知发送失败 qcNo={} doctorId={}", saved.getQcNo(), snapshot.getDoctorId(), ex);
        }
    }

    @Override
    public List<BizQualityControlVO> executeQcBatch(String recordSource, List<Long> recordIds, Integer qcType) {
        if (recordIds.size() > MAX_BATCH_SIZE) {
            throw new BusinessException("单次批量质控不得超过 " + MAX_BATCH_SIZE + " 份病历");
        }
        List<BizQualityControlVO> list = new ArrayList<>(recordIds.size());
        for (Long recordId : recordIds) {
            QcExecuteDTO dto = new QcExecuteDTO();
            dto.setRecordSource(recordSource);
            dto.setRecordId(recordId);
            dto.setQcType(qcType);
            list.add(executeQc(dto));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleQc(Long qcId, boolean ignore, String remark) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizQualityControl qc = this.getById(qcId);
        if (qc == null) {
            throw new BusinessException("质控单不存在：" + qcId);
        }
        assertRecordDeptAccessible(qc.getRecordSource(), qc.getRecordId());
        if (qc.getQcStatus() == null || qc.getQcStatus() != RuleCheckStatusEnum.PENDING.getCode()) {
            throw new BusinessException("只有「待处理」的质控单可以处理，当前状态："
                    + RuleCheckStatusEnum.getLabelOrUnknown(qc.getQcStatus()));
        }
        qc.setQcStatus(ignore ? RuleCheckStatusEnum.IGNORED.getCode() : RuleCheckStatusEnum.HANDLED.getCode());
        qc.setRemark(remark);
        qc.setUpdateBy(operatorUser.getRealName());
        qc.setUpdateTime(LocalDateTime.now());
        return this.updateById(qc);
    }

    // 码值中文与等级换算
    private QcSnapshot loadSnapshot(QcRecordSourceEnum source, Long recordId, QcTriggerSourceEnum triggerSource) {
        if (source == QcRecordSourceEnum.OUTPATIENT) {
            BizMedicalRecord record = bizMedicalRecordMapper.selectById(recordId);
            if (record == null) {
                throw new BusinessException("门诊病历不存在或已删除：" + recordId);
            }
            assertLoginDeptIfManual(triggerSource, record.getDeptId());
            return QcSnapshot.ofOutpatient(record);
        }
        InpatientRecordBriefVO record = patientFacadeService.getInpatientRecord(recordId);
        if (record == null) {
            throw new BusinessException("住院文书不存在或已删除：" + recordId);
        }
        assertLoginDeptIfManual(triggerSource, record.getDeptId());
        return QcSnapshot.ofInpatient(record);
    }

    /**
     * 科室数据范围只在人工入口拦：质控工作台的点是「这个岗位能不能看这份病历」。
     *
     * <p>结诊自动质控与定时补扫跑在没有登录态的新线程里，而收口判定链
     * （{@code DeptScopeService#canAccessDept}）拿不到登录态就直接报「未登录，无权查看」——
     * 内部链路本来没有「当前岗位」这个概念，拿登录态的门禁去拦它，等于每一条自动质控都必失败。
     */
    private void assertLoginDeptIfManual(QcTriggerSourceEnum triggerSource, Long deptId) {
        if (triggerSource == QcTriggerSourceEnum.MANUAL) {
            deptScopeService.assertDeptAccessible(deptId);
        }
    }

    /**
     * 列表行补中文。未知码值由各枚举 {@code getText} 渲染成空串，不回落合法值。
     */
    private void enrich(BizQualityControlVO vo) {
        vo.setRecordSourceText(QcRecordSourceEnum.getLabel(vo.getRecordSource()));
        vo.setQcTypeText(RecordQcTypeEnum.getLabel(vo.getQcType()));
        vo.setTriggerSourceText(QcTriggerSourceEnum.getLabel(vo.getTriggerSource()));
        vo.setQcStatusText(RuleCheckStatusEnum.getLabel(vo.getQcStatus()));
        vo.setQcResultText(dictCacheService.getDicDataLabel(DictTypeConst.QC_RESULT, vo.getQcResult()));
        vo.setRecordStatusText(RecordStatusEnum.getLabel(vo.getRecordStatus()));
        vo.setRecordTypeText(vo.getRecordType() == null ? null : InpatientRecordTypeEnum.getLabel(vo.getRecordType()));
        vo.setSeverityMaxText(vo.getSeverityMax() == null ? null : QcSeverityEnum.getLabel(vo.getSeverityMax()));
        vo.setGradeText(grade(vo.getScore(), vo.getSeverityMax()));
    }

    private void enrich(QcCandidateVO vo) {
        vo.setRecordSourceText(QcRecordSourceEnum.getLabel(vo.getRecordSource()));
        vo.setGenderText(SysGenderEnum.getLabel(vo.getGender()));
        vo.setRecordTypeText(vo.getRecordType() == null ? null : InpatientRecordTypeEnum.getLabel(vo.getRecordType()));
        vo.setRecordStatusText(RecordStatusEnum.getLabel(vo.getRecordStatus()));
        vo.setLastGrade(grade(vo.getLastScore(), vo.getLastSeverityMax()));
        vo.setQced(vo.getLastQcId() != null);
    }

    /**
     * 明细补中文与规则依据。basis 不在明细表里（它属于规则定义，会随规则版本变化），
     * 这里按 rule_code 反查枚举补上；反查不到就留 null，不去猜。
     */
    private List<QcIssueVO> enrich(List<QcIssueVO> issues) {
        if (issues == null) {
            return new ArrayList<>();
        }
        for (QcIssueVO issue : issues) {
            issue.setDimensionText(QcDimensionEnum.getLabel(issue.getDimension()));
            issue.setSeverityText(QcSeverityEnum.getLabel(issue.getSeverity()));
            QcRuleEnum rule = QcRuleEnum.fromCode(issue.getRuleCode());
            issue.setBasis(rule == null ? null : rule.getBasis());
        }
        return issues;
    }
}