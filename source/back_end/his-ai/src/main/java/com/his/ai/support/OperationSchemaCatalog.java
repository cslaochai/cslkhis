package com.his.ai.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 运营问数的表目录文本组装工具（无状态，不持有任何内存数据）。
 *
 * <p>运行时表/列/语义数据一律按需查库（{@code OperationQaConfigService} 提供查询），
 * 本类只负责把查询结果组装成提示词文本（schema / catalog / semantics），以及承载
 * 跨层传递的 record 定义。启动零加载、变更零刷新 —— 配置表即唯一事实来源。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OperationSchemaCatalog {

    /**
     * 列定义
     *
     * @param dataType   列类型（来自 information_schema data_type，可为空）
     * @param filterable 可否用于过滤条件（WHERE）
     * @param groupable  可否用于分组统计（GROUP BY）
     */
    public record ColumnDef(String columnName, String description, String dataType,
                            boolean filterable, boolean groupable) {
    }

    /**
     * 表定义
     */
    public record TableDef(String tableName, String usage, List<ColumnDef> columns) {
    }

    /**
     * 语义映射（语义词 → 表/列 + 权威口径）
     *
     * @param matchMode 匹配方式（1-包含 2-精确）
     * @param priority  优先级（大者先）
     */
    public record SemanticDef(String term, String tableName, String columnName, String scopeDesc,
                              Integer matchMode, Integer priority) {
    }

    /**
     * 敏感列配置（集中维护在列级配置）
     *
     * @param sensitiveType 敏感类型（1-脱敏 2-禁止出库）
     * @param maskType      脱敏方式（phone-手机号 idcard-身份证 email-邮箱 middle-姓名/中间打码）
     */
    public record SensitiveDef(String tableName, String columnName, Integer sensitiveType, String maskType) {
    }

    /**
     * 表间关系（join 图谱边）：源表.源列 → 目标表.目标列。
     *
     * @param relationType 关系类型（1-业务关联 2-维度关联）
     * @param description  关联说明（喂给模型：何时用此关联）
     */
    public record RelationDef(String sourceTable, String sourceColumn, String targetTable, String targetColumn,
                              Integer relationType, String description) {
    }

    /**
     * 提示词用的表结构文本（{{schema}} 变量），只渲染选中的表。
     *
     * @param tables     已查询的表定义（含启用列）
     * @param tableNames 选中表集合（空 = 全量）
     */
    public static String schemaText(List<TableDef> tables, Collection<String> tableNames) {
        Set<String> selected = tableNames == null || tableNames.isEmpty()
                ? null
                : tableNames.stream().map(t -> t.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        StringBuilder text = new StringBuilder();
        for (TableDef table : tables) {
            if (selected != null && !selected.contains(table.tableName().toLowerCase(Locale.ROOT))) {
                continue;
            }
            text.append(table.tableName()).append("（").append(table.usage()).append("）可用列：");
            String cols = table.columns().stream()
                    .map(c -> c.columnName() + "(" + c.description() + ")"
                            + (c.dataType() != null && !c.dataType().isBlank() ? "[" + c.dataType() + "]" : "")
                            + (c.filterable() ? "[可过滤]" : ""))
                    .collect(Collectors.joining(", "));
            text.append(cols.isEmpty() ? "（无启用列）" : cols).append('\n');
        }
        return text.toString().trim();
    }

    /**
     * 路由模板用的表目录文本（只有表名+用途，轻量，用于模型兜底选表）。
     */
    public static String catalogText(List<TableDef> tables) {
        return tables.stream()
                .map(t -> t.tableName() + "（" + t.usage() + "）")
                .collect(Collectors.joining("\n"));
    }

    /**
     * 提示词用的业务口径文本（{{semantics}} 变量），只渲染命中的语义条目。
     */
    public static String semanticText(List<SemanticDef> semantics) {
        if (semantics == null || semantics.isEmpty()) {
            return "（没有预设业务口径，按下方表结构与你的判断生成）";
        }
        return semantics.stream()
                .map(s -> "- " + s.term() + " → " + s.tableName()
                        + (s.columnName() != null ? "." + s.columnName() : "")
                        + "：" + s.scopeDesc())
                .collect(Collectors.joining("\n"));
    }

    /**
     * 提示词用的敏感字段约束文本（{{sensitiveRules}} 变量）：禁止出库 + 出参脱敏。
     */
    public static String sensitiveRulesText(List<SensitiveDef> sensitiveDefs) {
        if (sensitiveDefs == null || sensitiveDefs.isEmpty()) {
            return "（当前无敏感字段约束）";
        }
        String forbidden = sensitiveDefs.stream()
                .filter(s -> s.sensitiveType() != null && s.sensitiveType() == 2)
                .map(s -> s.tableName() + "." + s.columnName())
                .collect(Collectors.joining("、"));
        String masked = sensitiveDefs.stream()
                .filter(s -> s.sensitiveType() != null && s.sensitiveType() == 1)
                .map(s -> s.tableName() + "." + s.columnName()
                        + (s.maskType() != null ? "（" + s.maskType() + "）" : ""))
                .collect(Collectors.joining("、"));
        StringBuilder text = new StringBuilder();
        if (!forbidden.isBlank()) {
            text.append("【禁止出库】以下字段严禁出现在 SELECT 中：").append(forbidden).append('。');
        }
        if (!masked.isBlank()) {
            text.append("【出参脱敏】以下字段可查询，但 SELECT 列名必须原样保留（不要改别名/不要用函数包裹），"
                    + "返回结果会按脱敏方式自动打码：").append(masked).append('。');
        }
        return text.toString();
    }

    /**
     * 提示词用的表间关系文本（{{relations}} 变量）：权威 join 键，模型写 JOIN 时直接使用。
     *
     * @param relations 与选中表相关的启用关系边（已在图遍历后收窄）
     */
    public static String relationsText(List<RelationDef> relations) {
        if (relations == null || relations.isEmpty()) {
            return "（当前无预设表间关联，需要 join 时先确认列名真实存在，禁止臆造关联列）";
        }
        return relations.stream()
                .map(r -> r.sourceTable() + "." + r.sourceColumn() + " = " + r.targetTable() + "." + r.targetColumn()
                        + "（" + (r.relationType() != null && r.relationType() == 2 ? "维度关联" : "业务关联")
                        + "：" + r.description() + "）")
                .collect(Collectors.joining("\n"));
    }
}
