package com.his.emr.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
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
import com.his.emr.support.*;
import com.his.emr.vo.*;
import com.his.patient.entity.BizInpatientRecord;
import com.his.patient.mapper.BizInpatientRecordMapper;
import com.his.security.entity.CurrentUser;
import com.his.security.UserUtils;
import com.his.system.enums.BizTypeEnum;
import com.his.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 病案质控服务实现（P5.4）。
 *
 * <p>替换掉的旧实现是**假质控**：不读病历、质控结果恒为 1、{@code error_count} 恒 0，
 * 单号用 {@code AtomicInteger} + 秒级时间戳（重启归零、同秒必撞唯一索引），
 * 患者ID（NOT NULL）根本没填 —— 那条分支实际跑不通。
 *
 * <h3>四条口径</h3>
 * <ol>
 *   <li><b>质控单是留痕</b>：同一份病历可反复质控，每次新建一张单，旧单不改不删，
 *       这样才能回答"整改前后各扣了多少分"。</li>
 *   <li><b>规则结论与 AI 结论并存</b>：本服务只跑规则（qc_type 0~3）；
 *       AI 内涵质控（qc_type=4）在 his-ai，两者写在同一张表里、各自留痕，互不覆盖。</li>
 *   <li><b>返回的问题明细从库里读回来</b>，不是把内存对象直接回显 ——
 *       落库出错时接口就该跟着错，而不是让界面显示一份漂亮但没存下来的结论。</li>
 *   <li><b>事务边界在 {@link QcStoreService}</b>，本类在事务外重试单号冲突。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityControlServiceImpl extends ServiceImpl<BizQualityControlMapper, BizQualityControl>
        implements QualityControlService {

    /**
     * 单号冲突重试次数
     */
    private static final int MAX_NO_RETRY = 3;

    /**
     * 批量质控单次上限，防止前端误传全表 ID
     */
    private static final int MAX_BATCH_SIZE = 200;

    private static final String OPERATOR_FALLBACK = "system";

    private final BizMedicalRecordMapper medicalRecordMapper;

    private final BizInpatientRecordMapper inpatientRecordMapper;

    private final QcRuleEngine qcRuleEngine;

    private final QcStoreService qcStoreService;

    /**
     * 站内信（emr-qc 发送方）：质控发现问题 → 通知病历书写医生。
     * 发送失败只记日志——质控留痕是主流程，通知是副产品，不能让一条消息把质控单回滚掉。
     */
    private final SysMessageService sysMessageService;

    // 查询

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

    private static String currentOperator() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user != null) {
                if (StringUtils.hasText(user.getRealName())) {
                    return user.getRealName();
                }
                if (StringUtils.hasText(user.getUsername())) {
                    return user.getUsername();
                }
            }
        } catch (Exception ignored) {
            // 非请求线程（定时任务 / 归档流程内的调用）
        }
        return OPERATOR_FALLBACK;
    }

    @Override
    public PageResult<BizQualityControlVO> selectQcPage(QcQueryPageDTO query) {
        Page<BizQualityControlVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        var result = baseMapper.selectQcPage(page, query);
        result.getRecords().forEach(this::enrich);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(),
                result.getRecords());
    }

    @Override
    public BizQualityControlVO getQcDetail(Long qcId) {
        // C 类保留：入参是 Long（GET 直传），无 DTO 承载注解，此处是直调兜底
        if (qcId == null) {
            throw new BusinessException("质控单ID不能为空");
        }
        BizQualityControlVO vo = baseMapper.selectQcById(qcId);
        if (vo == null) {
            throw new BusinessException("质控单不存在：" + qcId);
        }
        enrich(vo);
        vo.setIssues(enrich(baseMapper.listIssueByQc(qcId)));
        return vo;
    }

    @Override
    public List<QcIssue> listIssueByQc(Long qcId) {
        // C 类保留：入参是 Long（GET 直传 + 内部复用），无 DTO 承载注解，此处是直调兜底
        if (qcId == null) {
            throw new BusinessException("质控单ID不能为空");
        }
        return enrich(baseMapper.listIssueByQc(qcId));
    }

    @Override
    public QcOverviewVO getOverview() {
        QcOverviewVO overview = baseMapper.selectOverview();
        QcOverviewVO issueTotals = baseMapper.selectIssueTotals();
        overview.setIssueCount(issueTotals.getIssueCount());
        overview.setIssueRecordCount(issueTotals.getIssueRecordCount());
        // 分母为 0 时给 null 而不是 0：0 看起来像"甲级率 0%"，而事实是"还没有质控数据"
        long scored = overview.getScoredCount();
        overview.setGradeARate(scored == 0 ? null
                : Math.round(overview.getGradeACount() * 1000.0 / scored) / 10.0);

        Map<Integer, QcRuleMetricVO> statByDimension = new HashMap<>();
        baseMapper.selectDimensionStat().forEach(stat -> statByDimension.put(stat.getDimension(), stat));
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

    @Override
    public PageResult<QcCandidateVO> listCandidatePage(QcCandidateQueryPageDTO query) {
        QcRecordSourceEnum source = QcRecordSourceEnum.parse(query.getRecordSource());
        Page<QcCandidateVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        var result = source == QcRecordSourceEnum.OUTPATIENT
                ? baseMapper.selectOutpatientCandidatePage(page, query)
                : baseMapper.selectInpatientCandidatePage(page, query);
        result.getRecords().forEach(this::enrich);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(),
                result.getRecords());
    }

    // 执行质控

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
            item.setText(QcTexts.qcType(code));
            list.add(item);
        }
        return list;
    }

    @Override
    public BizQualityControlVO executeQc(QcExecuteDTO dto) {
        // C 类保留：除 HTTP 入口外还被病历服务内部循环调用，Bean Validation 不经过内部调用
        if (dto == null || dto.getRecordId() == null) {
            throw new BusinessException("病历ID不能为空");
        }
        Integer qcType = normalizeQcType(dto.getQcType());
        QcSnapshot snapshot = loadSnapshot(QcRecordSourceEnum.parse(dto.getRecordSource()), dto.getRecordId());
        QcResult result = qcRuleEngine.inspect(snapshot, qcType);

        String operator = StringUtils.hasText(dto.getQcBy()) ? dto.getQcBy().trim() : currentOperator();
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

        // 回读一次，而不是把刚落库的实体搬成 VO：实体里没有病历号 / 患者 / 科室这些跨表快照，
        // 直接搬的结果是"刚执行完的这张单患者姓名是空的，刷新一下又有"。
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
     *
     * <p><b>通知型（handle_status=null），不是待办</b>：病历整改在系统内没有闭环动作
     * （改病历重新质控即可），质控单的「处理」流转在质控员侧（handleQc）。
     * 给医生挂一个永远关不掉的待办，只会让收件箱失去可信度。
     *
     * <p>严重度一律 warning：质控问题是管理提醒，urgent 是危急值专属（全站唯一），
     * 不允许第二类消息把危急值的红顶掉。
     */
    private void notifyDoctorOfQcIssues(QcSnapshot snapshot, QcResult result, BizQualityControl saved) {
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
            String payload = cn.hutool.json.JSONUtil.toJsonStr(new LinkedHashMap<String, Object>() {{
                put("patientName", snapshot.getPatientName());
                put("recordNo", snapshot.getRecordNo());
                put("qcNo", saved.getQcNo());
                put("issueCount", result.getIssueCount());
                put("score", result.getScore());
                put("grade", result.getGrade());
            }});
            sysMessageService.sendSystemMessage(snapshot.getDoctorId(), snapshot.getDoctorName(),
                    "病历质控问题：" + snapshot.getRecordNo(), content,
                    BizTypeEnum.EMR_QC.getType(), saved.getId(), "warning", payload, null);
        } catch (Exception ex) {
            log.warn("[病案质控] 质控问题通知发送失败 qcNo={} doctorId={}", saved.getQcNo(), snapshot.getDoctorId(), ex);
        }
    }

    @Override
    public List<BizQualityControlVO> executeQcBatch(String recordSource, List<Long> recordIds, Integer qcType) {
        if (recordIds == null || recordIds.isEmpty()) {
            throw new BusinessException("请至少选择一份病历");
        }
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
        BizQualityControl qc = this.getById(qcId);
        if (qc == null) {
            throw new BusinessException("质控单不存在：" + qcId);
        }
        if (qc.getQcStatus() == null || qc.getQcStatus() != 1) {
            throw new BusinessException("只有「待处理」的质控单可以处理，当前状态："
                    + QcTexts.qcStatus(qc.getQcStatus()));
        }
        qc.setQcStatus(ignore ? RuleCheckStatusEnum.IGNORED.getCode() : RuleCheckStatusEnum.HANDLED.getCode());
        qc.setRemark(remark);
        qc.setUpdateBy(currentOperator());
        qc.setUpdateTime(LocalDateTime.now());
        return this.updateById(qc);
    }

    private QcSnapshot loadSnapshot(QcRecordSourceEnum source, Long recordId) {
        if (source == QcRecordSourceEnum.OUTPATIENT) {
            BizMedicalRecord record = medicalRecordMapper.selectById(recordId);
            if (record == null) {
                throw new BusinessException("门诊病历不存在或已删除：" + recordId);
            }
            return QcSnapshot.ofOutpatient(record);
        }
        BizInpatientRecord record = inpatientRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("住院文书不存在或已删除：" + recordId);
        }
        return QcSnapshot.ofInpatient(record);
    }

    // 码值中文与等级换算

    /**
     * 列表行补中文。未知码值由 {@link QcTexts} 渲染成「未知(n)」，不回落合法值。
     * score 为空的旧版质控单 gradeText 保持 null —— 不猜等级。
     */
    private void enrich(BizQualityControlVO vo) {
        vo.setRecordSourceText(QcTexts.recordSource(vo.getRecordSource()));
        vo.setQcTypeText(QcTexts.qcType(vo.getQcType()));
        vo.setQcStatusText(QcTexts.qcStatus(vo.getQcStatus()));
        vo.setQcResultText(QcTexts.qcResult(vo.getQcResult()));
        vo.setRecordStatusText(QcTexts.recordStatus(vo.getRecordStatus()));
        vo.setRecordTypeText(vo.getRecordType() == null ? null : QcTexts.recordType(vo.getRecordType()));
        vo.setSeverityMaxText(vo.getSeverityMax() == null ? null : QcSeverityEnum.textOf(vo.getSeverityMax()));
        vo.setGradeText(QcTexts.grade(vo.getScore(), vo.getSeverityMax()));
    }

    private void enrich(QcCandidateVO vo) {
        vo.setRecordSourceText(QcTexts.recordSource(vo.getRecordSource()));
        vo.setGenderText(QcTexts.gender(vo.getGender()));
        vo.setRecordTypeText(vo.getRecordType() == null ? null : QcTexts.recordType(vo.getRecordType()));
        vo.setRecordStatusText(QcTexts.recordStatus(vo.getRecordStatus()));
        vo.setLastGrade(QcTexts.grade(vo.getLastScore(), vo.getLastSeverityMax()));
        vo.setQced(vo.getLastQcId() != null);
    }

    /**
     * 明细补中文与规则依据。basis 不在明细表里（它属于规则定义，会随规则版本变化），
     * 这里按 rule_code 反查枚举补上；反查不到就留 null，不去猜。
     */
    private List<QcIssue> enrich(List<QcIssue> issues) {
        if (issues == null) {
            return new ArrayList<>();
        }
        for (QcIssue issue : issues) {
            QcDimensionEnum dimension = QcDimensionEnum.ofCode(issue.getDimension());
            issue.setDimensionText(dimension == null ? "未知(" + issue.getDimension() + ")" : dimension.getText());
            issue.setSeverityText(QcSeverityEnum.textOf(issue.getSeverity()));
            QcRuleEnum rule = QcRuleEnum.ofCode(issue.getRuleCode());
            issue.setBasis(rule == null ? null : rule.getBasis());
        }
        return issues;
    }
}
