package com.his.ai.support;

import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 运营问数的安全闸门：模型生成的 SQL 必须逐条通过这里的检查才能执行。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OperationSqlGuard {

    /**
     * 结果行数上限，超出由闸门改写；执行层另有同值兜底
     */
    public static final int ROW_LIMIT = 200;

    private static final Pattern BLACKLIST = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|create|truncate|rename|merge|grant|revoke|call|set|use|lock|unlock"
                    + "|load|handler|prepare|execute|show|outfile|dumpfile)\\b|\\bfor\\s+update",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SCHEMA_QUALIFIED = Pattern.compile(
            "\\b(information_schema|performance_schema|mysql)\\s*\\.", Pattern.CASE_INSENSITIVE);

    private static final Pattern COMMENT_TRACE = Pattern.compile("--|/\\*|#");

    private static final Pattern TABLE_REFERENCE = Pattern.compile(
            "\\b(?:from|join|straight_join)\\s+([a-zA-Z_][a-zA-Z0-9_]*(?:\\s*,\\s*[a-zA-Z_][a-zA-Z0-9_]*)*)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern LIMIT_PAIR = Pattern.compile("\\blimit\\s+(\\d+)\\s*,\\s*(\\d+)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern LIMIT_SINGLE = Pattern.compile("\\blimit\\s+(\\d+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern LIMIT_KEYWORD = Pattern.compile("\\blimit\\b", Pattern.CASE_INSENSITIVE);

    /**
     * 校验并规范化模型生成的 SQL。
     *
     * @return 补齐 LIMIT 后可执行的语句
     * @throws IllegalArgumentException 未通过闸门，message 为面向用户的原因
     */
    public static String enforce(String rawSql) {
        if (!TextUtil.hasText(rawSql)) {
            throw new IllegalArgumentException("没有生成查询语句");
        }
        String sql = rawSql.trim();
        while (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        if (sql.contains(";")) {
            throw new IllegalArgumentException("一次只允许一条查询语句");
        }
        if (COMMENT_TRACE.matcher(sql).find()) {
            throw new IllegalArgumentException("查询语句中不允许包含注释");
        }
        if (!sql.matches("(?is)^select\\b.*")) {
            throw new IllegalArgumentException("只允许只读查询");
        }
        Matcher blacklisted = BLACKLIST.matcher(sql);
        if (blacklisted.find()) {
            throw new IllegalArgumentException("查询包含不允许的关键词：" + blacklisted.group());
        }
        if (SCHEMA_QUALIFIED.matcher(sql).find()) {
            throw new IllegalArgumentException("不允许跨库查询");
        }
        Matcher tables = TABLE_REFERENCE.matcher(sql);
        while (tables.find()) {
            for (String name : tables.group(1).split(",")) {
                String table = name.trim();
                if (!table.isEmpty() && !OperationSchemaCatalog.isAllowed(table)) {
                    throw new IllegalArgumentException("数据表 " + table + " 不在可查询范围内");
                }
            }
        }
        return enforceLimit(sql);
    }

    /**
     * 无 LIMIT 补上；超上限压缩。带 OFFSET 等复杂形态不强改，由执行层行数上限兜底。
     */
    private static String enforceLimit(String sql) {
        Matcher pair = LIMIT_PAIR.matcher(sql);
        if (pair.find()) {
            return clampGroup(sql, pair, 2);
        }
        Matcher single = LIMIT_SINGLE.matcher(sql);
        if (single.find()) {
            return clampGroup(sql, single, 1);
        }
        if (LIMIT_KEYWORD.matcher(sql).find()) {
            return sql;
        }
        return sql + " LIMIT " + ROW_LIMIT;
    }

    private static String clampGroup(String sql, Matcher matcher, int group) {
        int count = Integer.parseInt(matcher.group(group));
        if (count <= ROW_LIMIT) {
            return sql;
        }
        return sql.substring(0, matcher.start(group)) + ROW_LIMIT + sql.substring(matcher.end(group));
    }
}
