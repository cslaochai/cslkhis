package com.his.iot.support;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 把「同设备同类型」的一批点拼成一条 TDengine INSERT。
 * <p>
 * 子表首次写入时由 {@code USING ... TAGS} 自动创建，不需要预先建表；表名与列名都按 §356 建表脚本的口径来。
 */
public final class TelemetrySqlBuilder {

    private static final Logger log = LoggerFactory.getLogger(TelemetrySqlBuilder.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private TelemetrySqlBuilder() {
    }

    public static String build(String database, DeviceTypeSchema.Schema schema, String deviceType,
                               String deviceNo, long deptId, List<TelemetryPoint> points) {
        StringBuilder columns = new StringBuilder("ts");
        for (DeviceTypeSchema.Field f : schema.fields()) {
            columns.append(',').append(f.column());
        }

        StringBuilder sql = new StringBuilder(256 + points.size() * 64);
        sql.append("INSERT INTO ").append(database).append('.').append(DeviceTypeSchema.childTable(deviceNo))
                .append(" USING ").append(database).append('.').append(schema.stable())
                .append(" TAGS ('").append(escape(deviceNo)).append("','").append(escape(deviceType))
                .append("',").append(deptId).append(") (")
                .append(columns).append(") VALUES ");

        for (int n = 0; n < points.size(); n++) {
            if (n > 0) {
                sql.append(' ');
            }
            TelemetryPoint p = points.get(n);
            sql.append("('").append(p.ts().format(TS)).append('\'');
            for (DeviceTypeSchema.Field f : schema.fields()) {
                sql.append(',').append(value(p.metrics(), f));
            }
            sql.append(')');
        }
        return sql.toString();
    }

    private static String value(JsonNode metrics, DeviceTypeSchema.Field f) {
        JsonNode node = metrics == null ? null : metrics.get(f.key());
        if (node == null || node.isNull()) {
            return "NULL";
        }
        return switch (f.kind()) {
            case DOUBLE -> node.isNumber() ? trimNumber(node.asDouble()) : "NULL";
            case INT -> node.isNumber() ? String.valueOf(node.asLong()) : "NULL";
            case BOOL -> String.valueOf(toBool(node));
            case STRING -> "'" + escape(node.asText()) + "'";
        };
    }

    private static String trimNumber(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "NULL";
        }
        if (v == Math.rint(v) && Math.abs(v) < 1e15) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }

    /**
     * 厂商侧布尔常写成 "OPEN"/"NORMAL"/"yes"，别只认 true/false
     */
    private static boolean toBool(JsonNode node) {
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isNumber()) {
            return node.asInt() != 0;
        }
        String s = node.asText("").trim().toUpperCase();
        return switch (s) {
            case "TRUE", "YES", "1", "OPEN", "ON", "NORMAL", "RUNNING" -> true;
            case "FALSE", "NO", "0", "CLOSED", "OFF", "ABNORMAL", "STOPPED" -> false;
            default -> {
                log.debug("布尔字段无法识别，按 false 处理：{}", s);
                yield false;
            }
        };
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("'", "''");
    }

    /**
     * 解析设备上报的 ts（yyyy-MM-dd HH:mm:ss）。坏值用本机时间兜底，宁可时间点偏一点，也别整条丢掉。
     */
    public static LocalDateTime parseTs(String ts) {
        if (ts == null || ts.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(ts.replace(' ', 'T'));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
