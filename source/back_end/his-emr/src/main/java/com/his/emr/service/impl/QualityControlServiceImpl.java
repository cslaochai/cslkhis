package com.his.emr.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.DictTypeConst;
import com.his.common.enums.RecordQcTypeEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.emr.dto.QcCandidateQueryPageDTO;
import com.his.emr.dto.QcExecuteDTO;
import com.his.emr.dto.QcQueryPageDTO;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.entity.BizQualityControl;
import com.his.emr.enums.*;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.emr.mapper.BizQualityControlMapper;
import com.his.emr.service.QcStoreService;
import com.his.emr.service.QualityControlService;
import com.his.emr.vo.QcIssueVO;
import com.his.emr.vo.QcResultVO;
import com.his.emr.support.QcRuleEngine;
import com.his.emr.support.QcSnapshot;
import com.his.emr.vo.*;
import com.his.patient.entity.BizInpatientRecord;
import com.his.patient.enums.InpatientRecordTypeEnum;
import com.his.patient.mapper.BizInpatientRecordMapper;
import com.his.system.entity.CurrentUser;
import com.his.system.enums.BizTypeEnum;
import com.his.system.provider.DeptScopeService;
import com.his.system.service.DictCacheService;
import com.his.system.service.SysMessageService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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

    private final BizMedicalRecordMapper bizMedicalRecordMapper;

    private final BizInpatientRecordMapper bizInpatientRecordMapper;

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
        if (QcDimensionEnum.ofCode(qcType) == null) {
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
        if (QcRecordSourceEnum.parse(recordSource) == QcRecordSourceEnum.OUTPATIENT) {
            BizMedicalRecord record = bizMedicalRecordMapper.selectById(recordId);
            deptScopeService.assertDeptAccessible(record == null ? null : record.getDeptId());
            return;
        }
        BizInpatientRecord record = bizInpatientRecordMapper.selectById(recordId);
        deptScopeService.assertDeptAccessible(record == null ? null : record.getDeptId());
    }

    @Override
    public PageResult<BizQualityControlVO> selectQcPage(QcQueryPageDTO query) {
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
            stat.setDimensionText(dimension.getText());
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
        QcDimensionEnum only = dimension == null ? null : QcDimensionEnum.ofCode(dimension);
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
            vo.setDimensionText(rule.getDimension().getText());
            vo.setSeverity(rule.getSeverity().getCode());
            vo.setSeverityText(rule.getSeverity().getText());
            vo.setDeduct(rule.getSeverity().getDeduct());
            vo.setScopeText(rule.getScope().getText());
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
    public PageResult<QcCandidateVO> listCandidatePage(QcCandidateQueryPageDTO query) {
        QcRecordSourceEnum source = QcRecordSourceEnum.parse(query.getRecordSource());
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
            item.setText(dimension.getText());
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
            item.setText(RecordQcTypeEnum.getText(code));
            list.add(item);
        }
        return list;
    }

    @Override
    public BizQualityControlVO executeQc(QcExecuteDTO dto) {
        // C-非 web 入参：除 QualityControlController 的 HTTP 入口外，还被 EmrServiceImpl 病历保存链路
        // new QcExecuteDTO() 后直调，Bean Validation 不覆盖内部调用，保留
        if (dto == null || dto.getRecordId() == null) {
            throw new BusinessException("病历ID不能为空");
        }
        Integer qcType = normalizeQcType(dto.getQcType());
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        QcSnapshot snapshot = loadSnapshot(QcRecordSourceEnum.parse(dto.getRecordSource()), dto.getRecordId());
        QcResultVO result = qcRuleEngine.inspect(snapshot, qcType);

        String operator = TextUtil.hasText(dto.getQcBy()) ? dto.getQcBy().trim() : operatorUser.getRealName();
        BizQualityControl saved = null;
        for (int attempt = 1; attempt <= MAX_NO_RETRY && saved == null; attempt++) {
            try {
                saved = qcStoreService.save(snapshot, result, qcType, operator);
            } catch (DuplicateKeyException ex) {
                // 单号撞唯一索引：事务已回滚，必须在事务外重试（这也是把落库单独拆成 Bean 的原因）
                log.warn("[病案质控] 第 {} 次生成质控单号冲突，重试（recordId={}）", attempt, dto.getRecordId());
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
        if (qc.getQcStatus() == null || qc.getQcStatus() != 1) {
            throw new BusinessException("只有「待处理」的质控单可以处理，当前状态："
                    + dictCacheService.getDicDataLabel(DictTypeConst.QC_STATUS, qc.getQcStatus()));
        }
        qc.setQcStatus(ignore ? RuleCheckStatusEnum.IGNORED.getCode() : RuleCheckStatusEnum.HANDLED.getCode());
        qc.setRemark(remark);
        qc.setUpdateBy(operatorUser.getRealName());
        qc.setUpdateTime(LocalDateTime.now());
        return this.updateById(qc);
    }

    // 码值中文与等级换算
    private QcSnapshot loadSnapshot(QcRecordSourceEnum source, Long recordId) {
        if (source == QcRecordSourceEnum.OUTPATIENT) {
            BizMedicalRecord record = bizMedicalRecordMapper.selectById(recordId);
            if (record == null) {
                throw new BusinessException("门诊病历不存在或已删除：" + recordId);
            }
            // 执行质控是写操作：受限岗位只能质控本科室病历（病历保存链路的自动质控同样生效，
            // 该链路外层有兜底日志，不会中断归档流程）
            deptScopeService.assertDeptAccessible(record.getDeptId());
            return QcSnapshot.ofOutpatient(record);
        }
        BizInpatientRecord record = bizInpatientRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("住院文书不存在或已删除：" + recordId);
        }
        deptScopeService.assertDeptAccessible(record.getDeptId());
        return QcSnapshot.ofInpatient(record);
    }

    /**
     * 列表行补中文。未知码值由各枚举 {@code getText} 渲染成空串，不回落合法值。
     */
    private void enrich(BizQualityControlVO vo) {
        vo.setRecordSourceText(QcRecordSourceEnum.getText(vo.getRecordSource()));
        vo.setQcTypeText(RecordQcTypeEnum.getText(vo.getQcType()));
        vo.setQcStatusText(dictCacheService.getDicDataLabel(DictTypeConst.QC_STATUS, vo.getQcStatus()));
        vo.setQcResultText(dictCacheService.getDicDataLabel(DictTypeConst.QC_RESULT, vo.getQcResult()));
        vo.setRecordStatusText(RecordStatusEnum.getText(vo.getRecordStatus()));
        vo.setRecordTypeText(vo.getRecordType() == null ? null : InpatientRecordTypeEnum.getText(vo.getRecordType()));
        vo.setSeverityMaxText(vo.getSeverityMax() == null ? null : QcSeverityEnum.textOf(vo.getSeverityMax()));
        vo.setGradeText(grade(vo.getScore(), vo.getSeverityMax()));
    }

    private void enrich(QcCandidateVO vo) {
        vo.setRecordSourceText(QcRecordSourceEnum.getText(vo.getRecordSource()));
        vo.setGenderText(SysGenderEnum.getText(vo.getGender()));
        vo.setRecordTypeText(vo.getRecordType() == null ? null : InpatientRecordTypeEnum.getText(vo.getRecordType()));
        vo.setRecordStatusText(RecordStatusEnum.getText(vo.getRecordStatus()));
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
            issue.setDimensionText(QcDimensionEnum.getText(issue.getDimension()));
            issue.setSeverityText(QcSeverityEnum.textOf(issue.getSeverity()));
            QcRuleEnum rule = QcRuleEnum.ofCode(issue.getRuleCode());
            issue.setBasis(rule == null ? null : rule.getBasis());
        }
        return issues;
    }
}