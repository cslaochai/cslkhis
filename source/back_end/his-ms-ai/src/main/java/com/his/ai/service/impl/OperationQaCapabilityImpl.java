package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.OperationQaAskDTO;
import com.his.ai.dto.OperationQaLlmOutputDTO;
import com.his.ai.dto.OperationQaSummaryLlmOutputDTO;
import com.his.ai.service.*;
import com.his.ai.support.OperationSchemaCatalog;
import com.his.ai.support.OperationSqlGuard;
import com.his.ai.vo.*;
import com.his.common.util.DateFormats;
import com.his.common.util.SensitiveMaskUtil;
import com.his.common.util.TextUtil;
import com.his.common.enums.AiBizTypeEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI 运营问数能力：把管理者的自然语言问题翻译成一条受控 SELECT 并执行。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationQaCapabilityImpl implements OperationQaCapability {

    private static final String TEMPLATE_NAME = "operation-qa";

    private static final String SUMMARY_TEMPLATE_NAME = "operation-qa-summary";

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

    private final OperationTableSelector tableSelector;

    private final OperationQaConfigService operationQaConfigService;

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

        // 白名单按需查库；配置未维护（无启用表）时直接降级：白名单为空 = 什么都不让查，fail-closed
        Set<String> allowedTables = operationQaConfigService.enabledTableNames();
        if (allowedTables.isEmpty()) {
            return degrade(vo, "运营问数数据表配置为空：请先在「AI 管理台 → 问数配置」中维护可查询数据表", start);
        }

        // 第一段：按需加载 —— 先选问题相关的表，只把相关表 schema 与命中语义注入生成段
        OperationTableSelection selection = tableSelector.select(question);
        Set<String> selectedTables = selection.tables().isEmpty()
                ? allTableNames(allowedTables)
                : selection.tables();

        // 第二段：生成 SQL（只查选中表的结构 + 命中语义 + 敏感字段约束 + 表间关系）
        List<OperationSchemaCatalog.SensitiveDef> sensitiveDefs = operationQaConfigService.sensitiveDefs();
        List<OperationSchemaCatalog.RelationDef> relatedRelations = operationQaConfigService.relationDefs().stream()
                .filter(r -> selectedTables.contains(r.sourceTable().toLowerCase(Locale.ROOT))
                        || selectedTables.contains(r.targetTable().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());

        Optional<OperationQaLlmOutputDTO> generated = callGenerate(question,
                OperationSchemaCatalog.schemaText(operationQaConfigService.tableDefs(selectedTables), selectedTables),
                OperationSchemaCatalog.semanticText(selection.semantics()),
                OperationSchemaCatalog.sensitiveRulesText(sensitiveDefs),
                OperationSchemaCatalog.relationsText(relatedRelations));
        if (generated.isEmpty()) {
            return degrade(vo, aiExecutionService.degradeReasonOf(AiCapabilityKeys.OPERATION_QA)
                    + "；本次未能生成查询，请稍后重试", start);
        }
        OperationQaLlmOutputDTO output = generated.get();
        vo.setTitle(output.getTitle());
        if (!TextUtil.hasText(output.getSql())) {
            return degrade(vo, TextUtil.hasText(output.getTitle())
                    ? "未生成查询：" + output.getTitle()
                    : "未生成查询：本功能只回答经营统计类问题", start);
        }

        // 第三段：安全闸门（白名单集合由上面查库结果传入）
        String executableSql;
        try {
            executableSql = OperationSqlGuard.enforce(output.getSql(), allowedTables);
        } catch (IllegalArgumentException ex) {
            return degrade(vo, "生成的查询未通过安全检查：" + ex.getMessage()
                    + "。本功能只允许对指定经营数据表的只读查询", start);
        }

        // 敏感字段硬约束：禁止出库列不得出现在 SELECT 中（提示词约束之外的第二道防线）
        List<String> forbiddenColumns = sensitiveDefs.stream()
                .filter(s -> s.sensitiveType() != null && s.sensitiveType() == 2)
                .map(OperationSchemaCatalog.SensitiveDef::columnName)
                .distinct()
                .toList();
        if (!forbiddenColumns.isEmpty()) {
            String hit = findForbiddenColumnInSql(executableSql, forbiddenColumns);
            if (hit != null) {
                return degrade(vo, "生成的查询涉及不允许出库的字段：" + hit
                        + "（该字段已在「问数配置」中标记为禁止出库）", start);
            }
        }
        vo.setSql(executableSql);

        // 第四段：执行
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

        // 敏感字段出参脱敏：按「结果列名 → 源列」映射统一打码（后端做，前端零遮码实现）
        buildTable(vo, resultRows, resolveMaskedColumns(executableSql, sensitiveDefs));

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
        for (OperationSchemaCatalog.TableDef def : operationQaConfigService.tableDefs(null)) {
            OperationSchemaVO vo = new OperationSchemaVO();
            vo.setTableName(def.tableName());
            vo.setDescription(def.usage());
            list.add(vo);
        }
        return list;
    }

    private Optional<OperationQaLlmOutputDTO> callGenerate(String question, String schemaText, String semanticText,
                                                           String sensitiveRulesText, String relationsText) {
        OperationQaPromptVariablesVO variables = new OperationQaPromptVariablesVO();
        variables.setSchema(schemaText);
        variables.setSemantics(semanticText);
        variables.setSensitiveRules(sensitiveRulesText);
        variables.setRelations(relationsText);
        variables.setQuestion(question);
        variables.setToday(LocalDate.now().format(DateFormats.DATE));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.OPERATION_QA)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(AiBizTypeEnum.OPERATION_QA.getCode())
                .inputDigest(question)
                .maxTokens(SQL_MAX_TOKENS)
                .temperature(0.1D)
                .build();
        return aiExecutionService.call(call, OperationQaLlmOutputDTO.class);
    }

    /**
     * 全部启用表名（选表未选出任何表时的兜底，等价于旧的全量注入行为）。
     */
    private Set<String> allTableNames(Set<String> allowedTables) {
        return allowedTables;
    }

    private String callSummary(String question, List<OperationColumnVO> columns, List<OperationRowVO> rows) {
        OperationQaSummaryPromptVariablesVO variables = new OperationQaSummaryPromptVariablesVO();
        variables.setQuestion(question);
        variables.setTable(renderSummaryTable(columns, rows));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.OPERATION_QA)
                .templateName(SUMMARY_TEMPLATE_NAME)
                .variables(variables)
                .bizType(AiBizTypeEnum.OPERATION_QA.getCode())
                .inputDigest(question)
                .useLiteModel(true)
                .maxTokens(SUMMARY_MAX_TOKENS)
                .temperature(0.3D)
                .build();
        return aiExecutionService.call(call, OperationQaSummaryLlmOutputDTO.class)
                .map(OperationQaSummaryLlmOutputDTO::getSummary)
                .filter(TextUtil::hasText)
                .orElse(null);
    }

    /**
     * 把 JDBC 原始行转成前端用的列定义 + 行数据。
     *
     * <p>入参保留 {@code Map} 的原因见 {@code ask()} 里的说明：列集合由模型现场生成的 SQL 决定，
     * 只能按「第 i 列叫什么、第 i 格是什么值」的动态形态承载，转完即丢。
     *
     * @param maskedColumns 结果列名（小写）→ 脱敏方式；命中列出参统一打码
     */
    private void buildTable(OperationQaResultVO vo, List<Map<String, Object>> resultRows,
                            Map<String, String> maskedColumns) {
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
                Object value = resultRows.get(r).get(label);
                String maskType = maskedColumns.get(label.toLowerCase(Locale.ROOT));
                row.getCells().add(maskType == null ? formatValue(value) : maskValue(maskType, value));
            }
            vo.getRows().add(row);
        }
        vo.setRowCount(displayRows);
        vo.setTruncated(resultRows.size() > displayRows);
    }

    /**
     * 按脱敏方式对出参值打码（统一走 his-common 的 SensitiveMaskUtil，禁止各页面抄副本）。
     */
    private Object maskValue(String maskType, Object value) {
        if (value == null) {
            return null;
        }
        String v = String.valueOf(value);
        return switch (maskType) {
            case "phone" -> SensitiveMaskUtil.maskPhone(v);
            case "idcard" -> SensitiveMaskUtil.maskIdCard(v);
            case "email" -> SensitiveMaskUtil.maskEmail(v);
            case "cardno" -> SensitiveMaskUtil.maskCardNo(v);
            case "middle" -> SensitiveMaskUtil.maskMiddle(v, 1, 1);
            default -> value;
        };
    }

    /**
     * 解析生成 SQL 的 SELECT 列清单，建立「结果列名 → 源列名」映射后，把命中「脱敏」配置的
     * 结果列映射成脱敏方式。支持裸列名与「表前缀.列名 + AS 别名」；函数/表达式包裹的列
     * （COUNT(patient_name)、CASE WHEN…等）不映射——聚合结果不是原始敏感值，不打码。
     *
     * @return 结果列名（小写）→ 脱敏方式
     */
    private Map<String, String> resolveMaskedColumns(String sql,
                                                     List<OperationSchemaCatalog.SensitiveDef> sensitiveDefs) {
        Map<String, String> maskByColumn = new HashMap<>();
        for (OperationSchemaCatalog.SensitiveDef def : sensitiveDefs) {
            if (def.sensitiveType() != null && def.sensitiveType() == 1 && def.maskType() != null) {
                maskByColumn.put(def.columnName().toLowerCase(Locale.ROOT), def.maskType());
            }
        }
        if (maskByColumn.isEmpty()) {
            return Map.of();
        }
        // 结果列名 → 源列名
        Map<String, String> colByResultLabel = new HashMap<>();
        Matcher m = Pattern
                .compile("(?is)\\bselect\\b(.*?)\\bfrom\\b")
                .matcher(sql);
        if (m.find()) {
            for (String rawExpr : m.group(1).split(",")) {
                String expr = rawExpr.trim();
                if (expr.isEmpty()) {
                    continue;
                }
                String[] withAlias = expr.split("\\s+as\\s+", 2);
                String src = withAlias[0].trim();
                String column = null;
                if (src.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    column = src;
                } else {
                    Matcher qualified = Pattern
                            .compile("^[a-zA-Z_][a-zA-Z0-9_]*\\.([a-zA-Z_][a-zA-Z0-9_]*)$")
                            .matcher(src);
                    if (qualified.matches()) {
                        column = qualified.group(1);
                    }
                }
                if (column != null) {
                    // 结果列名：AS 别名优先；无别名时 JDBC 返回裸列名（tbl.col 无别名 → col）
                    String label = withAlias.length > 1 ? withAlias[1].trim() : column;
                    colByResultLabel.put(label.toLowerCase(Locale.ROOT), column.toLowerCase(Locale.ROOT));
                }
            }
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : colByResultLabel.entrySet()) {
            String mask = maskByColumn.get(entry.getValue());
            if (mask != null) {
                result.put(entry.getKey(), mask);
            }
        }
        return result;
    }

    /**
     * 禁止出库列检查：SELECT 与 FROM 之间的列清单里出现禁止列名（任意形态：裸列名 / 表前缀 / 函数包裹）即拦截。
     *
     * @return 命中的禁止列名；无命中返回 null
     */
    private String findForbiddenColumnInSql(String sql, List<String> forbiddenColumns) {
        Matcher m = Pattern
                .compile("(?is)\\bselect\\b(.*?)\\bfrom\\b")
                .matcher(sql);
        if (!m.find()) {
            return null;
        }
        String selectList = m.group(1);
        for (String col : forbiddenColumns) {
            Pattern p = Pattern
                    .compile("\\b" + Pattern.quote(col) + "\\b",
                            Pattern.CASE_INSENSITIVE);
            if (p.matcher(selectList).find()) {
                return col;
            }
        }
        return null;
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
