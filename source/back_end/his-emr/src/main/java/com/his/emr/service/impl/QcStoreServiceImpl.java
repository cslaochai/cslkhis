package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.CheckResultEnum;
import com.his.common.enums.RecordQcTypeEnum;
import com.his.emr.entity.BizQualityControl;
import com.his.emr.entity.BizQualityControlIssue;
import com.his.emr.enums.QcDimensionEnum;
import com.his.emr.enums.RuleCheckStatusEnum;
import com.his.emr.mapper.BizQualityControlIssueMapper;
import com.his.emr.mapper.BizQualityControlMapper;
import com.his.emr.service.QcStoreService;
import com.his.emr.support.QcIssue;
import com.his.emr.support.QcResult;
import com.his.emr.support.QcSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 质控结果落库。
 *
 * <p><b>单独一个 Bean 的唯一理由是"重试必须发生在事务之外"</b>：
 * 单号撞唯一索引时事务已标记回滚，在同一个事务里重试必然再次失败。
 * 所以这里承担事务边界，调用方（{@code QualityControlServiceImpl}）在事务外重试。
 *
 * <p><b>单号生成换掉了原实现</b>：原来用 {@code AtomicInteger} + 秒级时间戳，
 * 进程重启序号归零、同一秒内两条必撞 {@code uk_qc_no}。现在改成
 * 「{@code QC} + yyyyMMdd + 4 位当日序号」，序号来自当日已有单据数的实测值，
 * 并在插入前显式查重；仍有并发窗口，由调用方重试兜底 ——
 * 这不是"理论上可能"，而是必须留的退路。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QcStoreServiceImpl implements QcStoreService {

    /**
     * 单号前缀与既有数据不重叠：AI 内涵质控单号是 {@code QCAI...}，
     * 旧实现是 QC + 14 位时间 + 4 位（共 20 位），本实现是 {@code QC + 8 + 4}（共 14 位）。
     */
    private static final String NO_PREFIX = "QC";

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final DateTimeFormatter FALLBACK_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 问题明细条数上限。一份病历的问题不会超过规则总数（当前 20 条），
     * 这里只是防止未来规则膨胀后一次性写爆明细表。
     */
    private static final int MAX_ISSUES = 200;

    /**
     * error_detail 是 varchar(1000)，拼接摘要必须截断，否则插入直接报错
     */
    private static final int ERROR_DETAIL_MAX_LENGTH = 1000;

    private final BizQualityControlMapper qualityControlMapper;

    private final BizQualityControlIssueMapper qualityControlIssueMapper;

    /**
     * 检查内容：跑的是哪几个维度由 qcType 决定，写成文字供列表页直接读
     */
    private static String describe(Integer qcType, QcResult result) {
        String label = RecordQcTypeEnum.getText(qcType == null ? 0 : qcType);
        String dimensions = result.getDimensions().stream()
                .map(QcDimensionEnum::getText)
                .reduce((a, b) -> a + "+" + b)
                .orElse("（无适用规则）");
        return String.format("%s（%s）", label, dimensions);
    }

    /**
     * 明细的可读摘要，供列表页与 CDR 时间轴使用。
     * 格式固定为「序号.[维度/严重度]字段：描述」，人可读且可枚举。
     */
    private static String joinIssues(List<QcIssue> issues) {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (QcIssue issue : issues) {
            builder.append(index++).append('.')
                    .append('[').append(issue.getDimensionText()).append('/').append(issue.getSeverityText()).append(']')
                    .append(issue.getFieldName()).append('：').append(issue.getErrorDetail()).append('；');
            if (builder.length() >= ERROR_DETAIL_MAX_LENGTH) {
                break;
            }
        }
        return truncate(builder.toString(), ERROR_DETAIL_MAX_LENGTH);
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    /**
     * 落库一张质控单及其问题明细。整体成功或整体回滚 ——
     * 只落主单不落明细，会得到"error_count=8 但一条明细都查不到"这种自相矛盾的数据。
     */
    @Transactional(rollbackFor = Exception.class)
    public BizQualityControl save(QcSnapshot snapshot, QcResult result, Integer qcType, String operator) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        BizQualityControl qc = new BizQualityControl();
        qc.setQcNo(nextQcNo());
        qc.setRecordId(snapshot.getRecordId());
        qc.setRecordSource(snapshot.getSource().getCode());
        qc.setPatientId(snapshot.getPatientId());
        qc.setQcType(qcType == null ? RecordQcTypeEnum.COMPREHENSIVE.getCode() : qcType);
        qc.setQcContent(describe(qcType, result));
        qc.setQcResult(result.isPass() ? CheckResultEnum.PASS.getCode() : CheckResultEnum.FAIL.getCode());
        qc.setErrorCount(result.getIssueCount());
        qc.setErrorDetail(joinIssues(result.getIssues()));
        qc.setScore(result.getScore());
        qc.setSeverityMax(result.getSeverityMax());
        qc.setQcStatus(RuleCheckStatusEnum.PENDING.getCode());
        qc.setQcBy(operator);
        qc.setQcTime(now);
        qc.setCreateBy(operator);
        qc.setCreateTime(now);
        qualityControlMapper.insert(qc);

        List<QcIssue> issues = result.getIssues();
        int written = 0;
        for (QcIssue issue : issues) {
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
            row.setErrorDetail(truncate(issue.getErrorDetail(), 500));
            row.setSuggestion(truncate(issue.getSuggestion(), 500));
            row.setEvidence(truncate(issue.getEvidence(), 200));
            row.setCreateTime(now);
            qualityControlIssueMapper.insert(row);
            written++;
        }
        return qc;
    }

    /**
     * 生成当日唯一单号。先取当日实测条数 +1，再显式查重，最多退 50 次；
     * 仍然撞车就退到毫秒级 + 3 位随机，保证业务不会因为"序号用完了"而落不了库。
     */
    private String nextQcNo() {
        String dayPrefix = NO_PREFIX + LocalDate.now().format(DAY_FORMATTER);
        long base = qualityControlMapper.countByQcNoPrefix(dayPrefix);
        for (int offset = 1; offset <= 50; offset++) {
            String candidate = dayPrefix + String.format("%04d", (base + offset) % 10000);
            if (!exists(candidate)) {
                return candidate;
            }
        }
        for (int attempt = 0; attempt < 20; attempt++) {
            String candidate = NO_PREFIX + LocalDateTime.now().format(FALLBACK_FORMATTER)
                    + String.format("%03d", (int) (Math.random() * 1000));
            if (!exists(candidate)) {
                return candidate;
            }
        }
        // 走到这里说明唯一索引上已经有同秒同随机的记录，交给调用方重试
        throw new IllegalStateException("无法生成不重复的质控单号，请重试");
    }

    private boolean exists(String qcNo) {
        return qualityControlMapper.selectCount(
                new LambdaQueryWrapper<BizQualityControl>().eq(BizQualityControl::getQcNo, qcNo)) > 0;
    }
}
