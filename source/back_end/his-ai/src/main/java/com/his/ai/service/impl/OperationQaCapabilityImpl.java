package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.OperationQaAskDTO;
import com.his.ai.dto.OperationQaLlmOutputDTO;
import com.his.ai.dto.OperationQaSummaryLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.OperationQaCapability;
import com.his.ai.support.OperationSchemaCatalog;
import com.his.ai.support.OperationSqlGuard;
import com.his.ai.vo.OperationColumnVO;
import com.his.ai.vo.OperationQaResultVO;
import com.his.ai.vo.OperationRowVO;
import com.his.ai.vo.OperationSchemaVO;
import com.his.common.util.DateFormats;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * AI 运营问数能力：把管理者的自然语言问题翻译成一条受控 SELECT 并执行。
 * <p>
 * 「事实层代码算」纪律的<b>受控例外</b> —— 聚合逻辑由模型写进 SQL，
 * 换来的是任意经营问题都能问；代价用两道工程手段偿还：
 * 生成的语句必须过 {@link OperationSqlGuard}（白名单表 + 只读 + LIMIT 规范化），
 * 执行走独立的查询模板（连接超时 + 行数上限 + 只读语义）。
 * 执行、截断、呈现全部由代码完成，返回体原样带回实际执行的 SQL。
 * <p>
 * 明确不做：不答个体患者临床信息（白名单里没有患者主档），不写任何库，
 * 不做跨库访问。降级路径与说明措辞见 docs/AI能力施工手册.md §6。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationQaCapabilityImpl implements OperationQaCapability {

    private static final String TEMPLATE_NAME = "operation-qa";

    private static final String SUMMARY_TEMPLATE_NAME = "operation-qa-summary";

    private static final String BIZ_TYPE = "operation_qa";

    /**
     * 结果只回显前 100 行，超出标 truncated；行数上限 200 由闸门与执行模板双重保证
     */
    private static final int MAX_DISPLAY_ROWS = 100;

    private static final int SQL_MAX_TOKENS = 1024;

    private static final int SUMMARY_MAX_TOKENS = 512;

    /**
     * 结论段只读前 30 行，控制提示词长度
     */
    private static final int SUMMARY_TABLE_ROWS = 30;

    private static final int QUERY_TIMEOUT_SECONDS = 10;

    private static final int EXECUTE_ERROR_MAX_LENGTH = 200;

    private final AiExecutionService aiExecutionService;

    private final DataSource dataSource;

    /**
     * 独立查询模板：只在白名单 SELECT 上使用，超时与行数上限在这里兜底，
     * 不复用全局共享的 JdbcTemplate（避免把限值带到别的业务路径上）
     */
    private JdbcTemplate queryTemplate;

    private static Object formatValue(Object value) {
        if (value == null) {
            return null;
        }
        // DATETIME/DATE 列经 JDBC 出来是时间对象，直接 toString 会变成 ISO 带秒带 T 的形态
        if (value instanceof LocalDateTime dateTime) {
            return DateFormats.DATETIME_MINUTE.format(dateTime);
        }
        if (value instanceof LocalDate date) {
            return DateFormats.DATE.format(date);
        }
        if (value instanceof LocalTime time) {
            return time.toString();
        }
        if (value instanceof Timestamp timestamp) {
            return DateFormats.DATETIME_MINUTE.format(timestamp.toLocalDateTime());
        }
        if (value instanceof java.sql.Date date) {
            return DateFormats.DATE.format(date.toLocalDate());
        }
        return value;
    }

    @PostConstruct
    void initQueryTemplate() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
        template.setMaxRows(OperationSqlGuard.ROW_LIMIT);
        this.queryTemplate = template;
    }

    @Override
    public OperationQaResultVO ask(OperationQaAskDTO dto) {
        long start = System.currentTimeMillis();
        String question = dto.getQuestion().trim();

        OperationQaResultVO vo = new OperationQaResultVO();
        vo.setQuestion(question);

        // 第一段：生成 SQL
        Optional<OperationQaLlmOutputDTO> generated = callGenerate(question);
        if (generated.isEmpty()) {
            return degrade(vo, aiExecutionService.degradeReasonOf(AiCapabilityKeys.OPERATION_QA)
                    + "；本次未能生成查询，请稍后重试", start);
        }
        OperationQaLlmOutputDTO output = generated.get();
        vo.setTitle(output.getTitle());
        if (!StringUtils.hasText(output.getSql())) {
            return degrade(vo, StringUtils.hasText(output.getTitle())
                    ? "未生成查询：" + output.getTitle()
                    : "未生成查询：本功能只回答经营统计类问题", start);
        }

        // 第二段：安全闸门
        String executableSql;
        try {
            executableSql = OperationSqlGuard.enforce(output.getSql());
        } catch (IllegalArgumentException ex) {
            return degrade(vo, "生成的查询未通过安全检查：" + ex.getMessage()
                    + "。本功能只允许对指定经营数据表的只读查询", start);
        }
        vo.setSql(executableSql);

        // 第三段：执行
        List<Map<String, Object>> resultRows;
        try {
            resultRows = queryTemplate.queryForList(executableSql);
        } catch (DataAccessException ex) {
            String cause = ex.getMostSpecificCause().getMessage();
            if (cause != null && cause.length() > EXECUTE_ERROR_MAX_LENGTH) {
                cause = cause.substring(0, EXECUTE_ERROR_MAX_LENGTH);
            }
            log.warn("[AI-运营问数] 查询执行失败: {}", cause);
            return degrade(vo, "查询执行失败：" + cause, start);
        }

        buildTable(vo, resultRows);

        if (Boolean.TRUE.equals(dto.getWithSummary()) && !vo.getRows().isEmpty()) {
            // 结论段失败静默置空，表格照常返回
            vo.setSummary(callSummary(question, vo.getColumns(), vo.getRows()));
        }

        vo.setElapsedMs(System.currentTimeMillis() - start);
        return vo;
    }

    @Override
    public List<OperationSchemaVO> schema() {
        List<OperationSchemaVO> list = new ArrayList<>();
        for (OperationSchemaCatalog.TableDef def : OperationSchemaCatalog.tables()) {
            OperationSchemaVO vo = new OperationSchemaVO();
            vo.setTableName(def.tableName());
            vo.setDescription(def.usage());
            list.add(vo);
        }
        return list;
    }

    private Optional<OperationQaLlmOutputDTO> callGenerate(String question) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("schema", OperationSchemaCatalog.schemaText());
        variables.put("question", question);
        variables.put("today", LocalDate.now().format(DateFormats.DATE));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.OPERATION_QA)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .inputDigest(question)
                .maxTokens(SQL_MAX_TOKENS)
                .temperature(0.1D)
                .build();
        return aiExecutionService.call(call, OperationQaLlmOutputDTO.class);
    }

    private String callSummary(String question, List<OperationColumnVO> columns, List<OperationRowVO> rows) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("question", question);
        variables.put("table", renderSummaryTable(columns, rows));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.OPERATION_QA)
                .templateName(SUMMARY_TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .inputDigest(question)
                .useLiteModel(true)
                .maxTokens(SUMMARY_MAX_TOKENS)
                .temperature(0.3D)
                .build();
        return aiExecutionService.call(call, OperationQaSummaryLlmOutputDTO.class)
                .map(OperationQaSummaryLlmOutputDTO::getSummary)
                .filter(StringUtils::hasText)
                .orElse(null);
    }

    private void buildTable(OperationQaResultVO vo, List<Map<String, Object>> resultRows) {
        if (resultRows.isEmpty()) {
            vo.setRowCount(0);
            return;
        }
        // queryForList 返回 LinkedHashMap，列序即 SELECT 的输出列序
        List<String> labels = new ArrayList<>(resultRows.get(0).keySet());
        for (int i = 0; i < labels.size(); i++) {
            OperationColumnVO column = new OperationColumnVO();
            column.setKey("c" + (i + 1));
            column.setLabel(labels.get(i));
            vo.getColumns().add(column);
        }
        int displayRows = Math.min(resultRows.size(), MAX_DISPLAY_ROWS);
        for (int r = 0; r < displayRows; r++) {
            OperationRowVO row = new OperationRowVO();
            for (String label : labels) {
                row.getCells().add(formatValue(resultRows.get(r).get(label)));
            }
            vo.getRows().add(row);
        }
        vo.setRowCount(displayRows);
        vo.setTruncated(resultRows.size() > displayRows);
    }

    private String renderSummaryTable(List<OperationColumnVO> columns, List<OperationRowVO> rows) {
        StringBuilder text = new StringBuilder();
        List<String> header = new ArrayList<>();
        for (OperationColumnVO column : columns) {
            header.add(column.getLabel());
        }
        text.append(String.join(" | ", header)).append('\n');
        int limit = Math.min(rows.size(), SUMMARY_TABLE_ROWS);
        for (int r = 0; r < limit; r++) {
            List<Object> cells = rows.get(r).getCells();
            List<String> line = new ArrayList<>();
            for (int c = 0; c < columns.size() && c < cells.size(); c++) {
                Object value = cells.get(c);
                line.add(value == null ? "-" : String.valueOf(value));
            }
            text.append(String.join(" | ", line)).append('\n');
        }
        return text.toString().trim();
    }

    private OperationQaResultVO degrade(OperationQaResultVO vo, String reason, long start) {
        vo.setDegraded(true);
        vo.setDegradeReason(reason);
        vo.setElapsedMs(System.currentTimeMillis() - start);
        return vo;
    }
}
