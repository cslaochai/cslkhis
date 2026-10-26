package com.his.clinical.service.impl;

import com.his.common.base.BaseEntity;
import com.his.common.enums.CheckResultEnum;
import com.his.common.enums.QcTriggerSourceEnum;
import com.his.common.enums.RecordQcTypeEnum;
import com.his.spi.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.clinical.entity.BizQualityControl;
import com.his.clinical.entity.BizQualityControlIssue;
import com.his.common.enums.QcDimensionEnum;
import com.his.common.enums.RuleCheckStatusEnum;
import com.his.clinical.mapper.BizQualityControlIssueMapper;
import com.his.clinical.mapper.BizQualityControlMapper;
import com.his.clinical.service.QcStoreService;
import com.his.clinical.vo.QcIssueVO;
import com.his.clinical.vo.QcResultVO;
import com.his.clinical.support.QcOperator;
import com.his.clinical.support.QcSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 质控结果落库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QcStoreServiceImpl implements QcStoreService {

    /**
     * 问题明细条数上限。一份病历的问题不会超过规则总数（当前 20 条），
     * 这里只是防止未来规则膨胀后一次性写爆明细表。
     */
    private static final int MAX_ISSUES = 200;

    /**
     * error_detail 是 varchar(1000)，拼接摘要必须截断，否则插入直接报错
     */
    private static final int ERROR_DETAIL_MAX_LENGTH = 1000;

    private final BizQualityControlMapper bizQualityControlMapper;

    private final BizQualityControlIssueMapper bizQualityControlIssueMapper;

    private final RedisSequenceService redisSequenceService;

    /**
     * 检查内容：跑的是哪几个维度由 qcType 决定，写成文字供列表页直接读
     */
    private static String describe(Integer qcType, QcResultVO result) {
        String label = RecordQcTypeEnum.getLabel(qcType == null ? 0 : qcType);
        String dimensions = result.getDimensions().stream()
                .map(QcDimensionEnum::getLabel)
                .reduce((a, b) -> a + "+" + b)
                .orElse("（无适用规则）");
        return String.format("%s（%s）", label, dimensions);
    }

    /**
     * 明细的可读摘要，供列表页与 CDR 时间轴使用。
     * 格式固定为「序号.[维度/严重度]字段：描述」，人可读且可枚举。
     */
    private static String joinIssues(List<QcIssueVO> issues) {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (QcIssueVO issue : issues) {
            builder.append(index++).append('.')
                    .append('[').append(issue.getDimensionText()).append('/').append(issue.getSeverityText()).append(']')
                    .append(issue.getFieldName()).append('：').append(issue.getErrorDetail()).append('；');
            if (builder.length() >= ERROR_DETAIL_MAX_LENGTH) {
                break;
            }
        }
        return TextUtil.cut(builder.toString(), ERROR_DETAIL_MAX_LENGTH);
    }

    /**
     * 落库一张质控单及其问题明细。整体成功或整体回滚 ——
     * 只落主单不落明细，会得到"error_count=8 但一条明细都查不到"这种自相矛盾的数据。
     */
    @Transactional(rollbackFor = Exception.class)
    public BizQualityControl save(QcSnapshot snapshot, QcResultVO result, Integer qcType,
                                  QcTriggerSourceEnum triggerSource, QcOperator operator) {
        LocalDateTime now = TimeUtil.nowSeconds();

        BizQualityControl qc = new BizQualityControl();
        qc.setQcNo(redisSequenceService.generateQcStoreNo());
        qc.setRecordId(snapshot.getRecordId());
        qc.setRecordSource(snapshot.getSource().getCode());
        qc.setPatientId(snapshot.getPatientId());
        qc.setQcType(qcType == null ? RecordQcTypeEnum.COMPREHENSIVE.getCode() : qcType);
        qc.setTriggerSource(triggerSource.getCode());
        qc.setQcContent(describe(qcType, result));
        qc.setQcResult(result.isPass() ? CheckResultEnum.PASS.getCode() : CheckResultEnum.FAIL.getCode());
        qc.setErrorCount(result.getIssueCount());
        qc.setErrorDetail(joinIssues(result.getIssues()));
        qc.setScore(result.getScore());
        qc.setSeverityMax(result.getSeverityMax());
        qc.setQcStatus(RuleCheckStatusEnum.PENDING.getCode());
        qc.setQcBy(operator.getName());
        qc.setQcTime(now);
        applyAudit(qc, operator, now);
        bizQualityControlMapper.insert(qc);

        List<QcIssueVO> issues = result.getIssues();
        int written = 0;
        for (QcIssueVO issue : issues) {
            if (written >= MAX_ISSUES) {
                log.warn("[病案质控] 问题明细超过 {} 条，已截断（qcNo={}）", MAX_ISSUES, qc.getQcNo());
                break;
            }
            BizQualityControlIssue row = new BizQualityControlIssue();
            row.setQcId(qc.getId());
            row.setQcNo(qc.getQcNo());
            row.setRecordSource(snapshot.getSource().getCode());
            row.setRecordId(snapshot.getRecordId());
            row.setPatientId(snapshot.getPatientId());
            row.setRuleCode(issue.getRuleCode());
            row.setRuleName(issue.getRuleName());
            row.setDimension(issue.getDimension());
            row.setSeverity(issue.getSeverity());
            row.setDeduct(issue.getDeduct());
            row.setFieldName(issue.getFieldName());
            row.setErrorDetail(TextUtil.cut(issue.getErrorDetail(), 500));
            row.setSuggestion(TextUtil.cut(issue.getSuggestion(), 500));
            row.setEvidence(TextUtil.cut(issue.getEvidence(), 200));
            applyAudit(row, operator, now);
            bizQualityControlIssueMapper.insert(row);
            written++;
        }
        return qc;
    }

    /**
     * 显式写审计四列与建单时间。
     *
     * <p>为什么不能只靠全局自动填充：填充器取的是登录态，而结诊收尾与定时补扫都跑在没有登录态的
     * 新线程里，填充器直接返回；这两类行的创建人不允许为空也没有默认值，漏填整条写入被数据库拦下，
     * 表现出来是「质控单一条都没生成」却没有任何接口报错。
     */
    private static void applyAudit(BaseEntity row, QcOperator operator, LocalDateTime now) {
        row.setCreateBy(operator.getName());
        row.setCreateById(operator.getEmployeeId());
        row.setUpdateBy(operator.getName());
        row.setUpdateById(operator.getEmployeeId());
        row.setCreateTime(now);
        row.setUpdateTime(now);
    }

}
